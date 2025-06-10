package com.example.mad_project.api.models;

import com.google.gson.annotations.SerializedName;

public class FeltEarthquake {
    @SerializedName("updateTime")
    private String updateTime;

    @SerializedName("mag")
    private Double magnitude;

    @SerializedName("region")
    private String region;

    @SerializedName("intensity")
    private String intensity;

    @SerializedName("lat")
    private Double latitude;

    @SerializedName("lon")
    private Double longitude;

    @SerializedName("details")
    private String details;

    @SerializedName("ptime")
    private String earthquakeTime;

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public Double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(Double magnitude) {
        this.magnitude = magnitude;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getEarthquakeTime() {
        return earthquakeTime;
    }

    public void setEarthquakeTime(String earthquakeTime) {
        this.earthquakeTime = earthquakeTime;
    }
}