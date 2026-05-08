package com.dslab.parking.controller;

import com.dslab.parking.dto.ApiResponse;
import com.dslab.parking.repository.CreditCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cards")
public class CardController {

    @Autowired private CreditCardRepository cardRepo;

    @GetMapping("/{driverId}")
    public Object listForDriver(@PathVariable int driverId) {
        try {
            return cardRepo.findByDriverId(driverId);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/add")
    @Transactional
    public Map<String, Object> add(@RequestBody Map<String, Object> body) {
        try {
            Integer driverId = num(body, "driver_id");
            String cardNumber = str(body, "card_number");
            String expiry = str(body, "card_expiry");
            String cvv = str(body, "card_cvv");

            if (driverId == null || cardNumber == null || expiry == null || cvv == null) {
                return ApiResponse.error(
                    "driver_id, card_number, card_expiry, and card_cvv are required");
            }

            String nickname = str(body, "card_nickname");
            String cardType = str(body, "card_type");
            boolean isDefault = bool(body, "is_default");

            if (isDefault) cardRepo.unsetAllDefaults(driverId);
            int id = cardRepo.insert(driverId, nickname, cardNumber, expiry, cvv, cardType, isDefault);

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("message", "Card added");
            out.put("card_id", id);
            return out;
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/set-default")
    @Transactional
    public Map<String, Object> setDefault(@RequestBody Map<String, Object> body) {
        try {
            Integer driverId = num(body, "driver_id");
            Integer cardId = num(body, "card_id");
            if (driverId == null || cardId == null) {
                return ApiResponse.error("driver_id and card_id required");
            }
            cardRepo.unsetAllDefaults(driverId);
            cardRepo.setDefault(driverId, cardId);
            return ApiResponse.ok("Default card updated");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{cardId}")
    public Map<String, Object> delete(@PathVariable int cardId,
                                      @RequestParam(value = "driver_id", required = false) Integer driverId) {
        try {
            if (driverId == null) return ApiResponse.error("driver_id query param required");
            cardRepo.delete(driverId, cardId);
            return ApiResponse.ok("Card deleted");
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
    private static boolean bool(Map<String, Object> m, String k) {
        Object v = m == null ? null : m.get(k);
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof Number) return ((Number) v).intValue() != 0;
        return Boolean.parseBoolean(String.valueOf(v));
    }
}
