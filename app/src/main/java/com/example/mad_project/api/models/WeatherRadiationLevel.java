package com.example.mad_project.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class WeatherRadiationLevel {
    public static class StationReading {
        @SerializedName("LocationName")
        private String locationName;

        @SerializedName("MaxTemp")
        private String maxTemp;

        @SerializedName("MinTemp")
        private String minTemp;

        @SerializedName("MaxRH")
        private String maxRelativeHumidity;

        @SerializedName("MinRH")
        private String minRelativeHumidity;

        @SerializedName("MaxUVIndex")
        private String maxUVIndex;

        @SerializedName("MeanUVIndex")
        private String meanUVIndex;

        @SerializedName("Rainfall")
        private String rainfall;

        @SerializedName("SunShine")
        private String sunshineHours;

        @SerializedName("Microsieverts")
        private String radiationLevel;

        // Getters and setters
        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }
        public String getMaxTemp() { return maxTemp; }
        public void setMaxTemp(String maxTemp) { this.maxTemp = maxTemp; }
        public String getMinTemp() { return minTemp; }
        public void setMinTemp(String minTemp) { this.minTemp = minTemp; }
        public String getMaxRelativeHumidity() { return maxRelativeHumidity; }
        public void setMaxRelativeHumidity(String maxRelativeHumidity) { this.maxRelativeHumidity = maxRelativeHumidity; }
        public String getMinRelativeHumidity() { return minRelativeHumidity; }
        public void setMinRelativeHumidity(String minRelativeHumidity) { this.minRelativeHumidity = minRelativeHumidity; }
        public String getMaxUVIndex() { return maxUVIndex; }
        public void setMaxUVIndex(String maxUVIndex) { this.maxUVIndex = maxUVIndex; }
        public String getMeanUVIndex() { return meanUVIndex; }
        public void setMeanUVIndex(String meanUVIndex) { this.meanUVIndex = meanUVIndex; }
        public String getRainfall() { return rainfall; }
        public void setRainfall(String rainfall) { this.rainfall = rainfall; }
        public String getSunshineHours() { return sunshineHours; }
        public void setSunshineHours(String sunshineHours) { this.sunshineHours = sunshineHours; }
        public String getRadiationLevel() { return radiationLevel; }
        public void setRadiationLevel(String radiationLevel) { this.radiationLevel = radiationLevel; }
    }

    @SerializedName("BulletinDate")
    private String bulletinDate;

    @SerializedName("BulletinTime")
    private String bulletinTime;

    @SerializedName("ReportTimeInfoDate")
    private String reportDate;

    @SerializedName("HongKongDesc")
    private String hongKongDescription;

    @SerializedName("NoteDesc")
    private String note;

    @SerializedName("NoteDesc1")
    private String note1;

    @SerializedName("NoteDesc2")
    private String note2;

    @SerializedName("NoteDesc3")
    private String note3;

    private Map<String, StationReading> stationReadings;

    // Getters and setters
    public String getBulletinDate() { return bulletinDate; }
    public void setBulletinDate(String bulletinDate) { this.bulletinDate = bulletinDate; }
    public String getBulletinTime() { return bulletinTime; }
    public void setBulletinTime(String bulletinTime) { this.bulletinTime = bulletinTime; }
    public String getReportDate() { return reportDate; }
    public void setReportDate(String reportDate) { this.reportDate = reportDate; }
    public String getHongKongDescription() { return hongKongDescription; }
    public void setHongKongDescription(String hongKongDescription) { this.hongKongDescription = hongKongDescription; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getNote1() { return note1; }
    public void setNote1(String note1) { this.note1 = note1; }
    public String getNote2() { return note2; }
    public void setNote2(String note2) { this.note2 = note2; }
    public String getNote3() { return note3; }
    public void setNote3(String note3) { this.note3 = note3; }
    public Map<String, StationReading> getStationReadings() { return stationReadings; }
    public void setStationReadings(Map<String, StationReading> stationReadings) { this.stationReadings = stationReadings; }
}
