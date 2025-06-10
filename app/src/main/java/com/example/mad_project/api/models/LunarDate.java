package com.example.mad_project.api.models;

import com.google.gson.annotations.SerializedName;

public class LunarDate {
    @SerializedName("LunarYear")
    private String lunarYear; // Example: "癸卯年，兔"

    @SerializedName("LunarDate")
    private String lunarDate; // Example: "二月初十"

    // Getters and setters
    public String getLunarYear() { return lunarYear; }
    public void setLunarYear(String lunarYear) { this.lunarYear = lunarYear; }
    public String getLunarDate() { return lunarDate; }
    public void setLunarDate(String lunarDate) { this.lunarDate = lunarDate; }
}
