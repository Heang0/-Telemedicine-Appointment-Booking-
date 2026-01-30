package com.example.telemedicine;

public class Provider {
    private String name;
    private String specialty;
    private String location;
    private float rating;
    private String language;
    private boolean availableNow;
    private String imageUrl;

    public Provider(String name, String specialty, String location, float rating, String language, boolean availableNow, String imageUrl) {
        this.name = name;
        this.specialty = specialty;
        this.location = location;
        this.rating = rating;
        this.language = language;
        this.availableNow = availableNow;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getName() { return name; }
    public String getSpecialty() { return specialty; }
    public String getLocation() { return location; }
    public float getRating() { return rating; }
    public String getLanguage() { return language; }
    public boolean isAvailableNow() { return availableNow; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public void setLocation(String location) { this.location = location; }
    public void setRating(float rating) { this.rating = rating; }
    public void setLanguage(String language) { this.language = language; }
    public void setAvailableNow(boolean availableNow) { this.availableNow = availableNow; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}