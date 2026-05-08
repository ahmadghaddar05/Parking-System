package com.dslab.parking.config;

import com.dslab.parking.datastructures.DriverActionStack;
import com.dslab.parking.datastructures.LRUCache;
import com.dslab.parking.datastructures.SpotOccupancyIndex;
import com.dslab.parking.datastructures.UnpaidPaymentQueue;
import com.dslab.parking.model.ParkingLot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.TreeMap;

/**
 * Central place that creates and exposes the in-memory data structures used by
 * the application. Putting them all here makes it obvious which DS we are
 * using and lets every service inject just the ones it needs.
 *
 * <p>Data structures registered:
 * <ul>
 *   <li>{@link LRUCache} of parking lots (capacity 16) - O(1) lookup of
 *       frequently viewed lots.</li>
 *   <li>{@link SpotOccupancyIndex} - HashMap of HashSets, O(1) reservation
 *       and release per spot.</li>
 *   <li>{@link UnpaidPaymentQueue} - per-driver min-heap so the oldest unpaid
 *       log is paid first.</li>
 *   <li>{@link DriverActionStack} - bounded stack of recent actions per
 *       driver (capacity 20).</li>
 *   <li>{@link TreeMap} of all parking lots keyed by lot_id - O(log n) sorted
 *       traversal for the /lots/nearby endpoint.</li>
 * </ul>
 */
@Configuration
public class DataStructureRegistry {

    /** LRU cache of recently fetched parking lots, capacity 16. */
    @Bean
    public LRUCache<Integer, ParkingLot> lotLRUCache() {
        return new LRUCache<>(16);
    }

    @Bean
    public SpotOccupancyIndex spotOccupancyIndex() {
        return new SpotOccupancyIndex();
    }

    @Bean
    public UnpaidPaymentQueue unpaidPaymentQueue() {
        return new UnpaidPaymentQueue();
    }

    /** Bounded stack of last 20 actions per driver. */
    @Bean
    public DriverActionStack driverActionStack() {
        return new DriverActionStack(20);
    }

    /**
     * Sorted map of all parking lots, refreshed at startup.
     * TreeMap gives us in-order traversal in O(n) and lookup by lot_id in
     * O(log n) - useful when listing lots in the UI.
     */
    @Bean
    public TreeMap<Integer, ParkingLot> sortedLotMap() {
        return new TreeMap<>();
    }
}
