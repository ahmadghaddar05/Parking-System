package com.dslab.parking.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tiny helper for building flat JSON response maps that match the shape the
 * original Node.js backend produced, e.g. {"message": "..."} or {"error": "..."}.
 *
 * Using a {@link LinkedHashMap} preserves insertion order so JSON keys come out
 * in a predictable order during demos.
 */
public final class ApiResponse {
    private ApiResponse() {}

    public static Map<String, Object> ok(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("message", message);
        return m;
    }

    public static Map<String, Object> error(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", message);
        return m;
    }

    public static Map<String, Object> map() {
        return new LinkedHashMap<>();
    }
}
