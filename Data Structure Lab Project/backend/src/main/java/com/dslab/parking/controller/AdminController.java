package com.dslab.parking.controller;

import com.dslab.parking.dto.ApiResponse;
import com.dslab.parking.model.Driver;
import com.dslab.parking.repository.DriverRepository;
import com.dslab.parking.repository.LogRepository;
import com.dslab.parking.repository.VehicleRepository;
import com.dslab.parking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired private DriverRepository driverRepo;
    @Autowired private VehicleRepository vehicleRepo;
    @Autowired private LogRepository logRepo;
    @Autowired private ParkingService parkingService;

    @GetMapping("/drivers")
    public Object drivers() {
        try {
            List<Driver> rows = driverRepo.findAllOrderByCreatedDesc();
            return rows.stream().map(d -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("driver_id", d.getDriverId());
                m.put("full_name", d.getFullName());
                m.put("email", d.getEmail());
                m.put("phone_number", d.getPhoneNumber());
                m.put("created_at", d.getCreatedAt() == null ? null : d.getCreatedAt().toString());
                return m;
            }).toList();
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        try {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("drivers", driverRepo.countAll());
            out.put("vehicles", vehicleRepo.countAll());
            out.put("total_sessions", logRepo.countAll());
            out.put("active_sessions", logRepo.countActive());
            return out;
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /** Recent driver actions stored on the in-memory action stack. */
    @GetMapping("/actions/{driverId}")
    public Object actions(@PathVariable int driverId) {
        try {
            return parkingService.getRecentActions(driverId);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /** Diagnostic endpoint that reports sizes of in-memory data structures. */
    @GetMapping("/ds/stats")
    public Map<String, Object> dsStats() {
        try {
            return parkingService.dataStructureStats();
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
