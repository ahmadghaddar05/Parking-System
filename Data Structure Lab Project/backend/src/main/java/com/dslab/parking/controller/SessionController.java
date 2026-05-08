package com.dslab.parking.controller;

import com.dslab.parking.dto.ApiResponse;
import com.dslab.parking.repository.LogRepository;
import com.dslab.parking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/session")
public class SessionController {

    @Autowired private ParkingService parkingService;
    @Autowired private LogRepository logRepo;

    @PostMapping("/start")
    public Map<String, Object> start(@RequestBody Map<String, Object> body) {
        try {
            Integer driverId = num(body, "driver_id");
            String plate = str(body, "plate_no");
            Integer lotId = num(body, "lot_id");
            String spotId = str(body, "spot_id");
            String spotLabel = str(body, "spot_label");

            if (driverId == null || plate == null || lotId == null) {
                return ApiResponse.error("Missing fields");
            }
            parkingService.startSession(driverId, plate, lotId, spotId, spotLabel);
            return ApiResponse.ok("Session started");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/end")
    public Object end(@RequestBody Map<String, Object> body) {
        try {
            String plate = str(body, "plate_no");
            if (plate == null || plate.isBlank()) return ApiResponse.error("plate_no required");
            return parkingService.endSession(plate);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/active_spots")
    public Object activeSpots(@RequestParam("lot_id") int lotId) {
        try {
            return parkingService.activeSpotsByLot(lotId);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/has_unpaid")
    public Map<String, Object> hasUnpaid(@RequestParam(value = "driver_id", required = false) Integer driverId) {
        try {
            if (driverId == null) return ApiResponse.error("driver_id required");
            int cnt = logRepo.countUnpaidByDriver(driverId);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("has_unpaid", cnt > 0);
            out.put("unpaid_count", cnt);
            return out;
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/active")
    public Object active(@RequestParam("driver_id") int driverId,
                         @RequestParam("plate_no") String plate) {
        try {
            Map<String, Object> row = logRepo.findActiveSessionForDriverPlate(driverId, plate);
            if (row == null) return ApiResponse.error("No ACTIVE session found");
            return row;
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    private static String str(Map<String, Object> m, String k) {
        Object v = m == null ? null : m.get(k);
        return v == null ? null : String.valueOf(v);
    }
    private static Integer num(Map<String, Object> m, String k) {
        Object v = m == null ? null : m.get(k);
        if (v == null) return null;
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return null; }
    }
}
