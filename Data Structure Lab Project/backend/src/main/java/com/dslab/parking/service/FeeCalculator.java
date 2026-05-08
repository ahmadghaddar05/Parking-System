package com.dslab.parking.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Fee rule (kept identical to the original Node backend):
 * <ul>
 *   <li>entry_fee covers the first 60 minutes (any second past entry counts).</li>
 *   <li>After 60 minutes: per-minute charge based on hourly_rate / 60.</li>
 * </ul>
 */
@Component
public class FeeCalculator {

    public BigDecimal calculate(LocalDateTime entry, LocalDateTime exit,
                                BigDecimal entryFee, BigDecimal hourlyRate) {
        long seconds = Duration.between(entry, exit).getSeconds();
        if (seconds < 0) seconds = 0;
        // ceil to nearest minute (start of a minute counts as a full minute)
        long minutes = (seconds + 59) / 60;

        BigDecimal fee;
        if (minutes <= 60) {
            fee = entryFee == null ? BigDecimal.ZERO : entryFee;
        } else {
            long overtime = minutes - 60;
            BigDecimal perMin = hourlyRate.divide(BigDecimal.valueOf(60), 6, RoundingMode.HALF_UP);
            fee = entryFee.add(perMin.multiply(BigDecimal.valueOf(overtime)));
        }
        return fee.setScale(2, RoundingMode.HALF_UP);
    }
}
