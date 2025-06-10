package com.example.mad_project.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LatestVisibility {
    public static class Fields {
        @SerializedName("station_name_en")
        private String stationNameEn;

        @SerializedName("station_name_tc")
        private String stationNameTc;

        @SerializedName("station_name_sc")
        private String stationNameSc;

        @SerializedName("visibility")
        private String visibility;

        @SerializedName("record_time")
        private String recordTime;

        // Getters and setters
        public String getStationNameEn() { return stationNameEn; }
        public void setStationNameEn(String stationNameEn) { this.stationNameEn = stationNameEn; }
        public String getStationNameTc() { return stationNameTc; }
        public void setStationNameTc(String stationNameTc) { this.stationNameTc = stationNameTc; }
        public String getStationNameSc() { return stationNameSc; }
        public void setStationNameSc(String stationNameSc) { this.stationNameSc = stationNameSc; }
        public String getVisibility() { return visibility; }
        public void setVisibility(String visibility) { this.visibility = visibility; }
        public String getRecordTime() { return recordTime; }
        public void setRecordTime(String recordTime) { this.recordTime = recordTime; }
    }

    @SerializedName("fields")
    private List<String> fields;

    @SerializedName("data")
    private List<List<String>> data;

    // Getters and setters
    public List<String> getFields() { return fields; }
    public void setFields(List<String> fields) { this.fields = fields; }
    public List<List<String>> getData() { return data; }
    public void setData(List<List<String>> data) { this.data = data; }
}
