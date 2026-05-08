package com.dslab.parking.repository;

import com.dslab.parking.model.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class DriverRepository {

    @Autowired private JdbcTemplate jdbc;

    private static final RowMapper<Driver> MAPPER = (rs, i) -> {
        Driver d = new Driver();
        d.setDriverId(rs.getInt("driver_id"));
        d.setFullName(rs.getString("full_name"));
        d.setEmail(rs.getString("email"));
        d.setPhoneNumber(rs.getString("phone_number"));
        d.setPasswordHash(rs.getString("password_hash"));
        if (rs.getTimestamp("created_at") != null) {
            d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return d;
    };

    public Optional<Driver> findByEmail(String email) {
        List<Driver> list = jdbc.query(
            "SELECT driver_id, full_name, email, phone_number, password_hash, created_at " +
            "FROM driver WHERE email = ? LIMIT 1",
            MAPPER, email);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM driver WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    public int insert(String fullName, String email, String phoneNumber, String passwordHash) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO driver (full_name, email, phone_number, password_hash) " +
                "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phoneNumber);
            ps.setString(4, passwordHash);
            return ps;
        }, kh);
        return kh.getKey() == null ? -1 : kh.getKey().intValue();
    }

    public List<Driver> findAllOrderByCreatedDesc() {
        return jdbc.query(
            "SELECT driver_id, full_name, email, phone_number, password_hash, created_at " +
            "FROM driver ORDER BY created_at DESC",
            MAPPER);
    }

    public int countAll() {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM driver", Integer.class);
        return c == null ? 0 : c;
    }
}
