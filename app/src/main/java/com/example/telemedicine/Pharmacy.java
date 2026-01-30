package com.example.telemedicine;

public class Pharmacy {
    private String name;
    private String address;
    private String distance;
    private String hours;
    private String phone;
    private double latitude;
    private double longitude;
    private boolean isOpen;

    public Pharmacy(String name, String address, String distance, String hours, String phone, double latitude, double longitude, boolean isOpen) {
        this.name = name;
        this.address = address;
        this.distance = distance;
        this.hours = hours;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isOpen = isOpen;
    }

    // Getters
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDistance() { return distance; }
    public String getHours() { return hours; }
    public String getPhone() { return phone; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isOpen() { return isOpen; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setDistance(String distance) { this.distance = distance; }
    public void setHours(String hours) { this.hours = hours; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setOpen(boolean open) { isOpen = open; }
}