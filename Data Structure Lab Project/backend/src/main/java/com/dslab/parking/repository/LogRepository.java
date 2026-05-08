package com.dslab.parking.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class LogRepository {

    @Autowired private JdbcTemplate jdbc;

    public int countUnpaidByDriver(int driverId) {
        Integer c = jdbc.queryForObject(
            "SELECT COUNT(*) FROM `log` WHERE driver_id = ? AND status = 'UNPAID'",
            Integer.class, driverId);
        return c == null ? 0 : c;
    }

    public boolean hasActiveSession(int driverId) {
        Integer c = jdbc.queryForObject(
            "SELECT COUNT(*) FROM `log` WHERE driver_id = ? AND status = 'ACTIVE'",
            Integer.class, driverId);
        return c != null && c > 0;
    }

    public boolean isSpotActive(int lotId, String spotLabel) {
        Integer c = jdbc.queryForObject(
            "SELECT COUNT(*) FROM `log` WHERE lot_id = ? AND spot_label = ? AND status = 'ACTIVE'",
            Integer.class, lotId, spotLabel);
        return c != null && c > 0;
    }

    public long insertActiveSession(int driverId, String plateNo, int lotId, int cameraId,
                                    String spotId, String spotLabel) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO `log` (driver_id, plate_no, lot_id, camera_id, spot_id, spot_label, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, driverId);
            ps.setString(2, plateNo);
            ps.setInt(3, lotId);
            ps.setInt(4, cameraId);
            ps.setString(5, spotId);
            ps.setString(6, spotLabel);
            return ps;
        }, kh);
        return kh.getKey() == null ? -1 : kh.getKey().longValue();
    }

    /** Returns the active session row for a plate, joined with the lot pricing. */
    public Map<String, Object> findActiveSessionByPlateWithPricing(String plateNo) {
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT l.log_id, l.entry_time, l.lot_id, l.spot_label, p.entry_fee, p.hourly_rate " +
            "FROM `log` l JOIN parking_lot p ON p.lot_id = l.lot_id " +
            "WHERE l.plate_no = ? AND l.status = 'ACTIVE' " +
            "ORDER BY l.log_id DESC LIMIT 1",
            plateNo);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int closeSession(long logId, LocalDateTime exitTime, BigDecimal fee) {
        return jdbc.update(
            "UPDATE `log` SET exit_time = ?, fee = ?, status = 'UNPAID' WHERE log_id = ?",
            Timestamp.valueOf(exitTime), fee, logId);
    }

    public Map<String, Object> findActiveSessionForDriverPlate(int driverId, String plateNo) {
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT log_id, lot_id, camera_id, spot_id, spot_label, entry_time " +
            "FROM `log` WHERE driver_id = ? AND plate_no = ? AND status = 'ACTIVE' " +
            "ORDER BY log_id DESC LIMIT 1",
            driverId, plateNo);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<String> findActiveSpotsByLot(int lotId) {
        return jdbc.queryForList(
            "SELECT spot_label FROM `log` " +
            "WHERE lot_id = ? AND status = 'ACTIVE' AND spot_label IS NOT NULL",
            String.class, lotId);
    }

    public List<Map<String, Object>> findHistoryByDriver(int driverId) {
        return jdbc.queryForList(
            "SELECT l.log_id, l.plate_no, l.lot_id, p.lot_name, l.spot_label, " +
            "       l.entry_time, l.exit_time, l.fee, l.status " +
            "FROM `log` l JOIN parking_lot p ON p.lot_id = l.lot_id " +
            "WHERE l.driver_id = ? ORDER BY l.log_id DESC",
            driverId);
    }

    public List<Map<String, Object>> findUnpaidByDriver(int driverId) {
        return jdbc.queryForList(
            "SELECT log_id, fee FROM `log` " +
            "WHERE driver_id = ? AND status = 'UNPAID' ORDER BY log_id DESC",
            driverId);
    }

    public Map<String, Object> findOneLog(int driverId, long logId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT fee, status FROM `log` WHERE log_id = ? AND driver_id = ? LIMIT 1",
            logId, driverId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int markPaid(long logId) {
        return jdbc.update("UPDATE `log` SET status = 'PAID' WHERE log_id = ?", logId);
    }

    public int countAll() {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM `log`", Integer.class);
        return c == null ? 0 : c;
    }

    public int countActive() {
        Integer c = jdbc.queryForObject(
            "SELECT COUNT(*) FROM `log` WHERE status = 'ACTIVE'", Integer.class);
        return c == null ? 0 : c;
    }

    // ----- REPORTS -----
    public List<Map<String, Object>> reportLotSummary() {
        return jdbc.queryForList(
            "SELECT p.lot_id, p.lot_name, " +
            "       COUNT(l.log_id) AS total_sessions, " +
            "       SUM(CASE WHEN l.status='ACTIVE' THEN 1 ELSE 0 END) AS active_sessions, " +
            "       SUM(CASE WHEN l.status IN ('UNPAID','PAID') THEN 1 ELSE 0 END) AS completed_sessions, " +
            "       COALESCE(SUM(CASE WHEN l.status IN ('UNPAID','PAID') THEN l.fee ELSE 0 END),0) AS total_revenue " +
            "FROM parking_lot p " +
            "LEFT JOIN `log` l ON l.lot_id = p.lot_id " +
            "GROUP BY p.lot_id, p.lot_name ORDER BY p.lot_id");
    }

    public List<Map<String, Object>> reportUnpaidAboveAverage() {
        return jdbc.queryForList(
            "SELECT d.driver_id, d.full_name, t.unpaid_total " +
            "FROM driver d " +
            "JOIN ( SELECT driver_id, COALESCE(SUM(fee),0) AS unpaid_total " +
            "       FROM `log` WHERE status='UNPAID' GROUP BY driver_id ) t " +
            "  ON t.driver_id = d.driver_id " +
            "WHERE t.unpaid_total > ( " +
            "  SELECT AVG(x.unpaid_total) FROM ( " +
            "    SELECT COALESCE(SUM(fee),0) AS unpaid_total FROM `log` " +
            "    WHERE status='UNPAID' GROUP BY driver_id ) x ) " +
            "ORDER BY t.unpaid_total DESC");
    }

    public List<Map<String, Object>> reportPlatesUnion() {
        return jdbc.queryForList(
            "(SELECT DISTINCT plate_no AS plate, 'EVER_PARKED' AS source FROM `log`) " +
            "UNION " +
            "(SELECT DISTINCT plate_no AS plate, 'UNPAID' AS source FROM `log` WHERE status='UNPAID') " +
            "ORDER BY plate");
    }
}
