package com.example.telemedicine;

import java.util.Date;
import java.util.List;

public class PharmacyLocator {
    private String id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String phoneNumber;
    private String website;
    private String latitude;
    private String longitude;
    private String operatingHours; // JSON format with days and hours
    private boolean is24Hour;
    private boolean acceptsInsurance;
    private List<String> services; // "prescription", "vaccines", "compounding", etc.
    private String rating;
    private int reviewCount;
    private String distance; // Distance from current location
    private String status; // "active", "closed", "temporarily_closed"
    private Date createdAt;
    private long updatedAt;

    // Empty constructor required for Firestore
    public PharmacyLocator() {}

    public PharmacyLocator(String name, String address, String city, String state,
                          String zipCode, String phoneNumber, String website,
                          String latitude, String longitude, String operatingHours,
                          boolean is24Hour, boolean acceptsInsurance,
                          List<String> services, String rating, int reviewCount) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.phoneNumber = phoneNumber;
        this.website = website;
        this.latitude = latitude;
        this.longitude = longitude;
        this.operatingHours = operatingHours;
        this.is24Hour = is24Hour;
        this.acceptsInsurance = acceptsInsurance;
        this.services = services;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.status = "active";
        this.createdAt = new Date();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    public String getOperatingHours() { return operatingHours; }
    public void setOperatingHours(String operatingHours) { this.operatingHours = operatingHours; }

    public boolean is24Hour() { return is24Hour; }
    public void set24Hour(boolean is24Hour) { this.is24Hour = is24Hour; }

    public boolean acceptsInsurance() { return acceptsInsurance; }
    public void setAcceptsInsurance(boolean acceptsInsurance) { this.acceptsInsurance = acceptsInsurance; }

    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}