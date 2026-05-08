package com.dslab.parking.service;

import com.dslab.parking.datastructures.DriverActionStack;
import com.dslab.parking.datastructures.LRUCache;
import com.dslab.parking.datastructures.SpotOccupancyIndex;
import com.dslab.parking.datastructures.UnpaidPaymentQueue;
import com.dslab.parking.model.ParkingLot;
import com.dslab.parking.repository.LogRepository;
import com.dslab.parking.repository.ParkingLotRepository;
import com.dslab.parking.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedHashMap;

/**
 * Heart of the application. This service shows how each data structure plugs
 * into a real backend operation:
 * <ul>
 *   <li>{@link LRUCache} - cache for the most recently fetched lots.</li>
 *   <li>{@link TreeMap} - sorted map of all lots, refreshed at startup.</li>
 *   <li>{@link SpotOccupancyIndex} - HashMap of HashSets, O(1) reservation.</li>
 *   <li>{@link UnpaidPaymentQueue} - per-driver min-heap, oldest debt first.</li>
 *   <li>{@link DriverActionStack} - bounded LIFO stack of recent actions.</li>
 * </ul>
 */
@Service
public class ParkingService {

    @Autowired private ParkingLotRepository lotRepo;
    @Autowired private LogRepository logRepo;
    @Autowired private PaymentRepository paymentRepo;
    @Autowired private FeeCalculator feeCalculator;

    @Autowired private LRUCache<Integer, ParkingLot> lotCache;
    @Autowired private SpotOccupancyIndex occupancy;
    @Autowired private UnpaidPaymentQueue unpaidQueue;
    @Autowired private DriverActionStack actionStack;
    @Autowired private TreeMap<Integer, ParkingLot> sortedLotMap;

    /**
     * On startup, populate:
     *  - the TreeMap of all lots (sorted by id)
     *  - the in-memory occupancy index from existing ACTIVE rows in DB
     *  - the LRU cache (warm with all known lots; LRU semantics kick in later)
     */
    @PostConstruct
    public void warmUpDataStructures() {
        // 1. Load all lots into the TreeMap and seed the LRU cache.
        List<ParkingLot> lots = lotRepo.findAll();
        sortedLotMap.clear();
        for (ParkingLot p : lots) {
            sortedLotMap.put(p.getLotId(), p);
            lotCache.put(p.getLotId(), p);
        }

        // 2. Refresh the occupancy index from the DB (in case the server restarts
        //    while there are ACTIVE sessions).
        for (ParkingLot p : lots) {
            List<String> active = logRepo.findActiveSpotsByLot(p.getLotId());
            occupancy.replaceLot(p.getLotId(), new HashSet<>(active));
        }

        System.out.println("[DS-Init] loaded " + lots.size() + " lots into TreeMap + LRUCache");
        System.out.println("[DS-Init] occupancy index seeded for " + lots.size() + " lots");
    }

    // -------------------------------------------------------------------------
    // LOTS - read with LRU cache fallback to DB
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> listLotsForFrontend() {
        // We want to deliver the same shape as the original endpoint but,
        // demonstrate the LRU cache.
        List<Map<String, Object>> rows = lotRepo.findAllWithCamera();

        // Touch the cache for each lot read (this rotates them as MRU).
        for (Map<String, Object> r : rows) {
            int lotId = ((Number) r.get("lot_id")).intValue();
            ParkingLot p = lotCache.get(lotId);
            if (p == null && sortedLotMap.containsKey(lotId)) {
                lotCache.put(lotId, sortedLotMap.get(lotId));
            }
        }

        // Reshape to match frontend (name/address/total_spots).
        return rows.stream().map(r -> {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("lot_id", r.get("lot_id"));
            out.put("camera_id", r.get("camera_id"));
            out.put("name", r.get("lot_name"));
            out.put("address", r.get("location"));
            out.put("opening_hours", r.get("opening_hours"));
            out.put("entry_fee", r.get("entry_fee"));
            out.put("hourly_rate", r.get("hourly_rate"));
            out.put("total_spots", r.get("spot_count"));
            out.put("lat", r.get("lat"));
            out.put("lng", r.get("lng"));
            return out;
        }).toList();
    }

    public List<String> activeSpotsByLot(int lotId) {
        // Source of truth = DB, but we also keep the in-memory set fresh.
        List<String> active = logRepo.findActiveSpotsByLot(lotId);
        occupancy.replaceLot(lotId, new HashSet<>(active));
        return active;
    }

    // -------------------------------------------------------------------------
    // SESSIONS
    // -------------------------------------------------------------------------

    /**
     * Start a new parking session.
     * Demonstrates:
     *  - O(1) "is spot taken?" via the {@link SpotOccupancyIndex}.
     *  - Action push on the {@link DriverActionStack}.
     */
    @Transactional
    public long startSession(int driverId, String plateNo, int lotId,
                             String spotId, String spotLabel) {
        // Block if driver has any UNPAID logs
        if (logRepo.countUnpaidByDriver(driverId) > 0) {
            throw new IllegalStateException(
                "Cannot reserve: you have unpaid parking fees. Please pay first.");
        }
        // Block if driver already has an ACTIVE session
        if (logRepo.hasActiveSession(driverId)) {
            throw new IllegalStateException("Driver already has an ACTIVE session");
        }
        // O(1) occupancy check (also re-checked in DB to stay safe across restarts)
        if (spotLabel != null) {
            if (occupancy.isOccupied(lotId, spotLabel) ||
                logRepo.isSpotActive(lotId, spotLabel)) {
                throw new IllegalStateException("Spot already taken (ACTIVE)");
            }
        }
        Integer cameraId = lotRepo.findCameraIdByLot(lotId);
        if (cameraId == null) throw new IllegalStateException("Lot has no camera");

        long logId = logRepo.insertActiveSession(
            driverId, plateNo, lotId, cameraId, spotId, spotLabel);

        // Reflect the reservation in the in-memory index immediately
        occupancy.reserve(lotId, spotLabel);

        // Push to action history stack
        actionStack.push(driverId, "START_SESSION",
            "Started session " + logId + " on plate " + plateNo +
            (spotLabel != null ? " spot " + spotLabel : ""));

        return logId;
    }

    /**
     * End a session for a plate. Computes the fee using {@link FeeCalculator},
     * frees the spot in the occupancy index, and enqueues the new debt to the
     * driver's {@link UnpaidPaymentQueue}.
     */
    @Transactional
    public Map<String, Object> endSession(String plateNo) {
        Map<String, Object> session = logRepo.findActiveSessionByPlateWithPricing(plateNo);
        if (session == null) {
            throw new IllegalStateException("No ACTIVE session for this plate");
        }

        long logId = ((Number) session.get("log_id")).longValue();
        int lotId = ((Number) session.get("lot_id")).intValue();
        String spotLabel = (String) session.get("spot_label");
        Object value = session.get("entry_time");

LocalDateTime entry;

if (value instanceof LocalDateTime) {
    entry = (LocalDateTime) value;
} else {
    entry = ((java.sql.Timestamp) value).toLocalDateTime();
}
        BigDecimal entryFee = (BigDecimal) session.get("entry_fee");
        BigDecimal hourlyRate = (BigDecimal) session.get("hourly_rate");

        LocalDateTime exit = LocalDateTime.now();
        BigDecimal fee = feeCalculator.calculate(entry, exit, entryFee, hourlyRate);

        logRepo.closeSession(logId, exit, fee);

        // Free the spot in the in-memory index
        occupancy.release(lotId, spotLabel);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Exit processed");
        result.put("log_id", logId);
        result.put("fee", fee);
        return result;
    }

    // -------------------------------------------------------------------------
    // PAYMENTS using PriorityQueue
    // -------------------------------------------------------------------------

    @Transactional
    public BigDecimal payOne(int driverId, long logId, String cardNumber,
                             String cvv, String expiry) {
        Map<String, Object> row = logRepo.findOneLog(driverId, logId);
        if (row == null) throw new IllegalStateException("Log not found");
        if (!"UNPAID".equals(row.get("status"))) {
            throw new IllegalStateException("This log is not UNPAID");
        }
        BigDecimal amt = (BigDecimal) row.get("fee");
        if (amt == null) amt = BigDecimal.ZERO;

        logRepo.markPaid(logId);
        paymentRepo.insert(driverId, logId, cardNumber, cvv, expiry, amt);

        unpaidQueue.removeLog(driverId, logId);
        actionStack.push(driverId, "PAY", "Paid $" + amt + " for log " + logId);
        return amt;
    }

    /**
     * Pay every unpaid log for a driver, oldest first (heap order).
     * Returns the number of logs cleared.
     */
    @Transactional
    public int payAll(int driverId, String cardNumber, String cvv, String expiry) {
        // Refresh queue from DB to be authoritative
        unpaidQueue.clear(driverId);
        List<Map<String, Object>> rows = logRepo.findUnpaidByDriver(driverId);
        for (Map<String, Object> r : rows) {
            long logId = ((Number) r.get("log_id")).longValue();
            BigDecimal fee = (BigDecimal) r.get("fee");
            unpaidQueue.enqueue(driverId,
                new UnpaidPaymentQueue.UnpaidEntry(logId, fee == null ? 0.0 : fee.doubleValue()));
        }

        // Drain and pay each one
        List<UnpaidPaymentQueue.UnpaidEntry> drained = unpaidQueue.drain(driverId);
        for (UnpaidPaymentQueue.UnpaidEntry e : drained) {
            BigDecimal amt = BigDecimal.valueOf(e.fee);
            logRepo.markPaid(e.logId);
            paymentRepo.insert(driverId, e.logId, cardNumber, cvv, expiry, amt);
        }

        if (!drained.isEmpty()) {
            actionStack.push(driverId, "PAY_ALL",
                "Paid " + drained.size() + " unpaid logs (oldest first)");
        }
        return drained.size();
    }

    // -------------------------------------------------------------------------
    // ACTION HISTORY accessor
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> getRecentActions(int driverId) {
        return actionStack.recent(driverId).stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", a.type);
            m.put("description", a.description);
            m.put("at", a.at.toString());
            return m;
        }).toList();
    }

    public Map<String, Object> dataStructureStats() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("lru_cache_size", lotCache.size());
        m.put("lru_cache_capacity", lotCache.capacity());
        m.put("sorted_lot_map_size", sortedLotMap.size());
        return m;
    }
}
