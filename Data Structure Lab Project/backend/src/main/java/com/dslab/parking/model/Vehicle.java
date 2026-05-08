package com.dslab.parking.model;

public class Vehicle {
    private String plateNo;
    private int driverId;
    private String vehicleType;
    private String color;
    private String model;
    private Integer year;

    public Vehicle() {}

    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
}
