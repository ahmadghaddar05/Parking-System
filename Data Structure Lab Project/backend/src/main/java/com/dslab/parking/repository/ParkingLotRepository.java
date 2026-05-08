package com.dslab.parking.repository;

import com.dslab.parking.model.ParkingLot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ParkingLotRepository {

    @Autowired private JdbcTemplate jdbc;

    private static final RowMapper<ParkingLot> MAPPER = (rs, i) -> {
        ParkingLot p = new ParkingLot();
        p.setLotId(rs.getInt("lot_id"));
        p.setLotName(rs.getString("lot_name"));
        p.setLocation(rs.getString("location"));
        p.setOpeningHours(rs.getString("opening_hours"));
        p.setEntryFee(rs.getBigDecimal("entry_fee"));
        p.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        p.setSpotCount(rs.getInt("spot_count"));
        p.setLat(rs.getBigDecimal("lat"));
        p.setLng(rs.getBigDecimal("lng"));
        p.setCurrency(rs.getString("currency"));
        return p;
    };

    public List<ParkingLot> findAll() {
        return jdbc.query(
            "SELECT lot_id, lot_name, location, opening_hours, entry_fee, hourly_rate, " +
            "spot_count, lat, lng, currency FROM parking_lot ORDER BY lot_id",
            MAPPER);
    }

    /** Returns rows joined with their camera id, formatted to match the frontend shape. */
    public List<Map<String, Object>> findAllWithCamera() {
        return jdbc.queryForList(
            "SELECT p.lot_id, c.camera_id, p.lot_name, p.location, p.opening_hours, " +
            "       p.entry_fee, p.hourly_rate, p.spot_count, p.lat, p.lng, p.currency " +
            "FROM parking_lot p JOIN camera c ON c.lot_id = p.lot_id ORDER BY p.lot_id");
    }

    /** Returns the camera_id linked to a lot, or null if none. */
    public Integer findCameraIdByLot(int lotId) {
        List<Integer> ids = jdbc.queryForList(
            "SELECT camera_id FROM camera WHERE lot_id = ? LIMIT 1",
            Integer.class, lotId);
        return ids.isEmpty() ? null : ids.get(0);
    }
}
