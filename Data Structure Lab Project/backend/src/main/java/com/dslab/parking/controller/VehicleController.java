package com.dslab.parking.controller;

import com.dslab.parking.dto.ApiResponse;
import com.dslab.parking.datastructures.DriverActionStack;
import com.dslab.parking.model.Vehicle;
import com.dslab.parking.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class VehicleController {

    @Autowired private VehicleRepository vehicleRepo;
    @Autowired private DriverActionStack actionStack;

    @GetMapping("/vehicle/{driverId}")
    public Object listForDriver(@PathVariable int driverId) {
        try {
            List<Vehicle> rows = vehicleRepo.findByDriverId(driverId);
            // emit as plain maps to match the original snake_case fields exactly
            return rows.stream().map(v -> Map.of(
                "plate_no",     nz(v.getPlateNo()),
                "vehicle_type", nz(v.getVehicleType()),
                "model",        nz(v.getModel()),
                "year",         v.getYear() == null ? "" : v.getYear(),
                "color",        nz(v.getColor())
            )).toList();
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/vehicle/add")
    public Map<String, Object> add(@RequestBody Map<String, Object> body) {
        try {
            Integer driverId = num(body, "driver_id");
            String plateNo = str(body, "plate_no");
            if (driverId == null || plateNo == null || plateNo.isBlank()) {
                return ApiResponse.error("driver_id and plate_no required");
            }
            String type = str(body, "vehicle_type");
            String model = str(body, "model");
            Integer year = num(body, "year");
            String color = str(body, "color");

            vehicleRepo.insert(driverId, plateNo, type, model, year, color);
            actionStack.push(driverId, "ADD_VEHICLE", "Added plate " + plateNo);
            return ApiResponse.ok("Vehicle added");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/vehicle/{plate}")
    public Map<String, Object> deleteSingular(@PathVariable("plate") String plate,
                                              @RequestParam("driver_id") int driverId) {
        return doDelete(driverId, plate);
    }

    @DeleteMapping("/vehicles/{plate}")
    public Map<String, Object> deletePlural(@PathVariable("plate") String plate,
                                            @RequestParam("driver_id") int driverId) {
        return doDelete(driverId, plate);
    }

    private Map<String, Object> doDelete(int driverId, String plate) {
        try {
            if (plate == null || plate.isBlank()) {
                return ApiResponse.error("Missing driver or plate");
            }
            int rows = vehicleRepo.delete(driverId, plate);
            if (rows == 0) return ApiResponse.error("Vehicle not found");
            actionStack.push(driverId, "DELETE_VEHICLE", "Removed plate " + plate);
            return ApiResponse.ok("Vehicle removed");
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
    private static String nz(String s) { return s == null ? "" : s; }
}
