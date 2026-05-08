package com.dslab.parking.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
public class CreditCardRepository {

    @Autowired private JdbcTemplate jdbc;

    public List<Map<String, Object>> findByDriverId(int driverId) {
        return jdbc.queryForList(
            "SELECT card_id, card_nickname, card_number, card_expiry, card_cvv, " +
            "       card_type, is_default " +
            "FROM credit_card WHERE driver_id = ? " +
            "ORDER BY is_default DESC, card_id DESC",
            driverId);
    }

    public int unsetAllDefaults(int driverId) {
        return jdbc.update("UPDATE credit_card SET is_default = 0 WHERE driver_id = ?", driverId);
    }

    public int setDefault(int driverId, int cardId) {
        return jdbc.update(
            "UPDATE credit_card SET is_default = 1 WHERE card_id = ? AND driver_id = ?",
            cardId, driverId);
    }

    public int insert(int driverId, String nickname, String cardNumber, String expiry,
                      String cvv, String cardType, boolean isDefault) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO credit_card (driver_id, card_nickname, card_number, card_expiry, " +
                " card_cvv, card_type, is_default) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, driverId);
            ps.setString(2, nickname);
            ps.setString(3, cardNumber);
            ps.setString(4, expiry);
            ps.setString(5, cvv);
            ps.setString(6, cardType == null ? "VISA" : cardType);
            ps.setInt(7, isDefault ? 1 : 0);
            return ps;
        }, kh);
        return kh.getKey() == null ? -1 : kh.getKey().intValue();
    }

    public int delete(int driverId, int cardId) {
        return jdbc.update("DELETE FROM credit_card WHERE card_id = ? AND driver_id = ?",
                cardId, driverId);
    }
}
