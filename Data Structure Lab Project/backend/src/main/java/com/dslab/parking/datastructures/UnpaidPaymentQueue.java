package com.dslab.parking.datastructures;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>UnpaidPaymentQueue</h2>
 *
 * <p>A min-heap (Java's {@link PriorityQueue}) that orders unpaid parking logs
 * so the oldest debt is paid first when a driver taps "Pay All".
 *
 * <p>Operations and complexity:
 * <ul>
 *   <li>{@code enqueue}: O(log n) - heap insert</li>
 *   <li>{@code peek}:    O(1)     - the root is the oldest unpaid log</li>
 *   <li>{@code poll}:    O(log n) - extract min and re-heapify</li>
 * </ul>
 *
 * <p>One queue is maintained per driver. We pair it with a {@code HashMap}
 * for O(1) lookup of "this driver's queue".
 *
 * <p>This is a cache layer; the database remains the source of truth, but the
 * priority queue lets us answer "which bill should the user clear first?"
 * without re-sorting the full list every time.
 */
public class UnpaidPaymentQueue {

    /** A single unpaid debt entry. Comparable by log_id (smaller = older). */
    public static class UnpaidEntry {
        public final long logId;
        public final double fee;

        public UnpaidEntry(long logId, double fee) {
            this.logId = logId;
            this.fee = fee;
        }
    }

    /** Smaller log_id = older = higher priority (paid first). */
    private static final Comparator<UnpaidEntry> OLDEST_FIRST =
            Comparator.comparingLong(e -> e.logId);

    private final Map<Integer, PriorityQueue<UnpaidEntry>> perDriver = new HashMap<>();

    public synchronized void enqueue(int driverId, UnpaidEntry e) {
        perDriver.computeIfAbsent(driverId, k -> new PriorityQueue<>(OLDEST_FIRST))
                 .offer(e);
    }

    /** Peek without removing. */
    public synchronized UnpaidEntry peek(int driverId) {
        PriorityQueue<UnpaidEntry> q = perDriver.get(driverId);
        return (q == null) ? null : q.peek();
    }

    /** Drain the queue for this driver, oldest first. */
    public synchronized List<UnpaidEntry> drain(int driverId) {
        PriorityQueue<UnpaidEntry> q = perDriver.remove(driverId);
        if (q == null) return new ArrayList<>();
        List<UnpaidEntry> out = new ArrayList<>(q.size());
        while (!q.isEmpty()) out.add(q.poll());
        return out;
    }

    /** Remove a specific log when it gets paid individually. */
    public synchronized void removeLog(int driverId, long logId) {
        PriorityQueue<UnpaidEntry> q = perDriver.get(driverId);
        if (q != null) {
            q.removeIf(e -> e.logId == logId);
            if (q.isEmpty()) perDriver.remove(driverId);
        }
    }

    public synchronized int size(int driverId) {
        PriorityQueue<UnpaidEntry> q = perDriver.get(driverId);
        return (q == null) ? 0 : q.size();
    }

    public synchronized void clear(int driverId) {
        perDriver.remove(driverId);
    }
}
