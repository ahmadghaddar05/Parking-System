package com.dslab.parking.controller;

import com.dslab.parking.dto.ApiResponse;
import com.dslab.parking.repository.LogRepository;
import com.dslab.parking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class PaymentController {

    @Autowired private ParkingService parkingService;
    @Autowired private LogRepository logRepo;

    @GetMapping("/payments/due/{driverId}")
    public Object due(@PathVariable int driverId) {
        try {
            return logRepo.findUnpaidByDriver(driverId);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/payment/pay")
    public Map<String, Object> pay(@RequestBody Map<String, Object> body) {
        try {
            Integer driverId = num(body, "driver_id");
            Long logId = lng(body, "log_id");
            if (driverId == null || logId == null) {
                return ApiResponse.error("driver_id and log_id required");
            }
            String card = str(body, "credit_card_no");
            String cvv = str(body, "ccv_cvc");
            String exp = str(body, "cc_expiry");

            BigDecimal amt = parkingService.payOne(driverId, logId, card, cvv, exp);

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("message", "Payment processed. You paid $" + amt.toPlainString());
            out.put("amount", amt);
            return out;
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/payment/pay_all")
    public Map<String, Object> payAll(@RequestBody Map<String, Object> body) {
        try {
            Integer driverId = num(body, "driver_id");
            if (driverId == null) return ApiResponse.error("driver_id required");

            String card = str(body, "credit_card_no");
            String cvv = str(body, "ccv_cvc");
            String exp = str(body, "cc_expiry");

            int count = parkingService.payAll(driverId, card, cvv, exp);
            if (count == 0) return ApiResponse.ok("No unpaid logs");
            return ApiResponse.ok("All due paid (" + count + " logs).");
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
    private static Long lng(Map<String, Object> m, String k) {
        Object v = m == null ? null : m.get(k);
        if (v == null) return null;
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return null; }
    }
}
