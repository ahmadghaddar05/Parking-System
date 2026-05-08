package com.dslab.parking.controller;

import com.dslab.parking.dto.ApiResponse;
import com.dslab.parking.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportsController {

    @Autowired private LogRepository logRepo;

    @GetMapping("/lot_summary")
    public Object lotSummary() {
        try { return logRepo.reportLotSummary(); }
        catch (Exception e) { return ApiResponse.error(e.getMessage()); }
    }

    @GetMapping("/unpaid_above_average")
    public Object unpaidAboveAverage() {
        try { return logRepo.reportUnpaidAboveAverage(); }
        catch (Exception e) { return ApiResponse.error(e.getMessage()); }
    }

    @GetMapping("/plates_union")
    public Object platesUnion() {
        try { return logRepo.reportPlatesUnion(); }
        catch (Exception e) { return ApiResponse.error(e.getMessage()); }
    }
}
