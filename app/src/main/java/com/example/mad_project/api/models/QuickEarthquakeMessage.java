package com.example.mad_project.api.models;

import com.google.gson.annotations.SerializedName;

public class QuickEarthquakeMessage {
    @SerializedName("lat")
    private Double latitude;

    @SerializedName("lon")
    private Double longitude;

    @SerializedName("mag")
    private Double magnitude;

    @SerializedName("region")
    private String region;

    @SerializedName("ptime")
    private String earthquakeTime;

    @SerializedName("updateTime")
    private String updateTime;

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

    public String getEarthquakeTime() {
        return earthquakeTime;
    }

    public void setEarthquakeTime(String earthquakeTime) {
        this.earthquakeTime = earthquakeTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
