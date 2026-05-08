package com.dslab.parking.datastructures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

/**
 * <h2>SpotOccupancyIndex</h2>
 *
 * <p>An in-memory occupancy index mapping each parking lot to the set of spot
 * labels currently ACTIVE in that lot.
 *
 * <p>The internal structure is a {@code HashMap<Integer, HashSet<String>>}:
 * <ul>
 *   <li>Outer {@link HashMap}: lot_id -&gt; set of occupied spots. O(1) lookup
 *       per lot.</li>
 *   <li>Inner {@link HashSet}: occupied spot labels for that lot. O(1) for
 *       {@code add}, {@code remove}, and {@code contains}.</li>
 * </ul>
 *
 * <p>Without this structure we would need to issue a {@code SELECT} on the
 * {@code log} table for every spot click on the frontend. With it,
 * the "is spot already taken?" check becomes a constant-time hash probe.
 *
 * <p>This class is thread-safe (synchronized) because multiple drivers can
 * reserve / release in parallel.
 */
public class SpotOccupancyIndex {

    private final Map<Integer, Set<String>> lotToSpots = new HashMap<>();

    /** Replace the occupancy snapshot for a lot (used when refreshing from DB). */
    public synchronized void replaceLot(int lotId, Set<String> occupied) {
        lotToSpots.put(lotId, new HashSet<>(occupied));
    }

    /** Mark a spot as taken. Returns false if it was already taken. */
    public synchronized boolean reserve(int lotId, String spotLabel) {
        if (spotLabel == null) return true; // null spot = not tracked
        Set<String> set = lotToSpots.computeIfAbsent(lotId, k -> new HashSet<>());
        return set.add(spotLabel); // HashSet.add returns false if duplicate
    }

    /** Free a spot when its session ends. */
    public synchronized void release(int lotId, String spotLabel) {
        if (spotLabel == null) return;
        Set<String> set = lotToSpots.get(lotId);
        if (set != null) set.remove(spotLabel);
    }

    /** O(1) "is this spot occupied?" check. */
    public synchronized boolean isOccupied(int lotId, String spotLabel) {
        if (spotLabel == null) return false;
        Set<String> set = lotToSpots.get(lotId);
        return set != null && set.contains(spotLabel);
    }

    /** Read-only view of occupied spots for a given lot. */
    public synchronized Set<String> occupiedSpots(int lotId) {
        Set<String> set = lotToSpots.get(lotId);
        if (set == null) return Collections.emptySet();
        return new HashSet<>(set); // defensive copy
    }
}
