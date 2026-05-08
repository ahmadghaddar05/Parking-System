package com.dslab.parking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Data Structure Lab Project - Main Entry Point.
 *
 * <p>This Spring Boot application backs a Smart Parking system. It is intentionally
 * built around classic Java Collections / Data Structures (HashMap, HashSet,
 * PriorityQueue, ArrayDeque used as a Stack, LinkedList used as a Queue, TreeMap,
 * and a custom LRU cache) so the project demonstrates how data structures speed up
 * common backend operations such as lookups, occupancy checks, payment ordering,
 * and history tracking.
 */
@SpringBootApplication
public class DataStructureLabApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataStructureLabApplication.class, args);
        System.out.println("============================================");
        System.out.println("  Data Structure Lab Project");
        System.out.println("  Backend running on http://localhost:3000");
        System.out.println("============================================");
    }
}
