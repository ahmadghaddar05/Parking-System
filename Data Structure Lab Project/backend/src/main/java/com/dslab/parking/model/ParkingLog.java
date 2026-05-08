package com.dslab.parking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ParkingLog {
    private long logId;
    private int driverId;
    private String plateNo;
    private int lotId;
    private int cameraId;
    private String spotId;
    private String spotLabel;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private BigDecimal fee;
    private String status; // ACTIVE | UNPAID | PAID

    public ParkingLog() {}

    public long getLogId() { return logId; }
    public void setLogId(long logId) { this.logId = logId; }

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }

    public int getLotId() { return lotId; }
    public void setLotId(int lotId) { this.lotId = lotId; }

    public int getCameraId() { return cameraId; }
    public void setCameraId(int cameraId) { this.cameraId = cameraId; }

    public String getSpotId() { return spotId; }
    public void setSpotId(String spotId) { this.spotId = spotId; }

    public String getSpotLabel() { return spotLabel; }
    public void setSpotLabel(String spotLabel) { this.spotLabel = spotLabel; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
