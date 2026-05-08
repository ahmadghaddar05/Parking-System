package com.dslab.parking.controller;

import com.dslab.parking.dto.ApiResponse;
import com.dslab.parking.model.Driver;
import com.dslab.parking.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthService authService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> body) {
        try {
            String name = str(body, "full_name");
            String email = str(body, "email");
            String phone = str(body, "phone_number");
            String password = str(body, "password");

            int id = authService.register(name, email, phone, password);

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("message", "Account created");
            out.put("driver_id", id);
            return out;
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> body) {
        try {
            String email = str(body, "email");
            String password = str(body, "password");

            if (email == null || password == null || email.isBlank() || password.isBlank()) {
                return ApiResponse.error("Email and password required");
            }

            Optional<Driver> opt = authService.login(email, password);
            if (opt.isEmpty()) return ApiResponse.error("Invalid credentials");

            Driver d = opt.get();
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("driver_id", d.getDriverId());
            out.put("full_name", d.getFullName());
            out.put("email", d.getEmail());
            return out;
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    private static String str(Map<String, Object> m, String k) {
        Object v = m == null ? null : m.get(k);
        return v == null ? null : String.valueOf(v);
    }
}
