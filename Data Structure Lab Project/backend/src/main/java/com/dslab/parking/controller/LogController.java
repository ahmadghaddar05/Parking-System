package com.dslab.parking.controller;

import com.dslab.parking.dto.ApiResponse;
import com.dslab.parking.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logs")
public class LogController {

    @Autowired private LogRepository logRepo;

    @GetMapping("/driver/{driverId}")
    public Object historyForDriver(@PathVariable int driverId) {
        try {
            return logRepo.findHistoryByDriver(driverId);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
