package com.dslab.parking.datastructures;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * <h2>DriverActionStack</h2>
 *
 * <p>A bounded LIFO stack (per driver) that remembers the most recent backend
 * actions: REGISTER, LOGIN, ADD_VEHICLE, START_SESSION, END_SESSION, PAY, etc.
 *
 * <p>Stack is the natural data structure here because we only ever care about
 * the most recent activity ("what did I just do?"). When the stack reaches its
 * cap we drop the oldest entry from the bottom.
 *
 * <p>Implementation: {@link ArrayDeque} - the JDK's recommended replacement
 * for the legacy {@code java.util.Stack} class. Push and pop are O(1).
 */
public class DriverActionStack {

    public static class Action {
        public final String type;
        public final String description;
        public final LocalDateTime at;

        public Action(String type, String description) {
            this.type = type;
            this.description = description;
            this.at = LocalDateTime.now();
        }
    }

    private final int capacity;
    private final Map<Integer, Deque<Action>> stacks = new HashMap<>();

    public DriverActionStack(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void push(int driverId, String type, String description) {
        Deque<Action> stack = stacks.computeIfAbsent(driverId, k -> new ArrayDeque<>());
        stack.push(new Action(type, description));
        // Trim from the bottom if we exceed the cap
        while (stack.size() > capacity) {
            stack.pollLast();
        }
    }

    /** Returns the actions newest-first without removing them. */
    public synchronized List<Action> recent(int driverId) {
        Deque<Action> stack = stacks.get(driverId);
        if (stack == null) return new ArrayList<>();
        List<Action> out = new ArrayList<>(stack.size());
        for (Iterator<Action> it = stack.iterator(); it.hasNext(); ) {
            out.add(it.next());
        }
        return out;
    }

    /** Pop the most recent action (used by /admin/undo style endpoints). */
    public synchronized Action pop(int driverId) {
        Deque<Action> stack = stacks.get(driverId);
        return (stack == null || stack.isEmpty()) ? null : stack.pop();
    }
}
