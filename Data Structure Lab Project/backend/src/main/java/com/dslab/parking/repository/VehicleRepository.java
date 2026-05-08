package com.dslab.parking.repository;

import com.dslab.parking.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VehicleRepository {

    @Autowired private JdbcTemplate jdbc;

    private static final RowMapper<Vehicle> MAPPER = (rs, i) -> {
        Vehicle v = new Vehicle();
        v.setPlateNo(rs.getString("plate_no"));
        v.setVehicleType(rs.getString("vehicle_type"));
        v.setModel(rs.getString("model"));
        int yr = rs.getInt("year");
        v.setYear(rs.wasNull() ? null : yr);
        v.setColor(rs.getString("color"));
        return v;
    };

    public List<Vehicle> findByDriverId(int driverId) {
        return jdbc.query(
            "SELECT plate_no, vehicle_type, model, year, color " +
            "FROM vehicle WHERE driver_id = ? ORDER BY plate_no",
            MAPPER, driverId);
    }

    public int insert(int driverId, String plateNo, String vehicleType,
                      String model, Integer year, String color) {
        return jdbc.update(
            "INSERT INTO vehicle (plate_no, driver_id, vehicle_type, model, year, color) " +
            "VALUES (?, ?, ?, ?, ?, ?)",
            plateNo, driverId, vehicleType, model, year, color);
    }

    public int delete(int driverId, String plateNo) {
        return jdbc.update(
            "DELETE FROM vehicle WHERE plate_no = ? AND driver_id = ?",
            plateNo, driverId);
    }

    public int countAll() {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM vehicle", Integer.class);
        return c == null ? 0 : c;
    }
}
