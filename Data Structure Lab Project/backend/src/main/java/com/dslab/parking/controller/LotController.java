package com.dslab.parking.controller;

import com.dslab.parking.dto.ApiResponse;
import com.dslab.parking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LotController {

    @Autowired private ParkingService parkingService;

    @GetMapping("/lots/nearby")
    public Object listNearby() {
        try {
            return parkingService.listLotsForFrontend();
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
