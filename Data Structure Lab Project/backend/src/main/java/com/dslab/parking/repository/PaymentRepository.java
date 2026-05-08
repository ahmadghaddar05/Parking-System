package com.dslab.parking.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class PaymentRepository {

    @Autowired private JdbcTemplate jdbc;

    public int insert(int driverId, long logId, String cardNumber, String cvv,
                      String expiry, BigDecimal amount) {
        return jdbc.update(
            "INSERT INTO payment (driver_id, log_id, credit_card_no, ccv_cvc, cc_expiry, amount, payment_status) " +
            "VALUES (?, ?, ?, ?, ?, ?, 'PAID')",
            driverId, logId, cardNumber, cvv, expiry, amount);
    }
}
