package com.example.mad_project.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HourlyRainfall {
    public static class RainfallData {
        @SerializedName("automaticWeatherStation")
        private String stationName;

        @SerializedName("automaticWeatherStationID")
        private String stationId;

        @SerializedName("value")
        private String value; // String because it can be "M" for maintenance

        @SerializedName("unit")
        private String unit;

        public String getStationName() { return stationName; }
        public void setStationName(String stationName) { this.stationName = stationName; }
        public String getStationId() { return stationId; }
        public void setStationId(String stationId) { this.stationId = stationId; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    @SerializedName("obsTime")
    private String observationTime;

    @SerializedName("hourlyRainfall")
    private List<RainfallData> rainfallData;

    public String getObservationTime() { return observationTime; }
    public void setObservationTime(String observationTime) { this.observationTime = observationTime; }
    public List<RainfallData> getRainfallData() { return rainfallData; }
    public void setRainfallData(List<RainfallData> rainfallData) { this.rainfallData = rainfallData; }
}

