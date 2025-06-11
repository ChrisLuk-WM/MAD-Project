package com.example.mad_project.api.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CurrentWeather {
    public static class RainfallData {
        @SerializedName("data")
        private List<RainfallRecord> data;

        @SerializedName("startTime")
        private String startTime;

        @SerializedName("endTime")
        private String endTime;

        public List<RainfallRecord> getData() { return data; }
        public void setData(List<RainfallRecord> data) { this.data = data; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
    }

    public static class RainfallRecord {
        @SerializedName("unit")
        private String unit;

        @SerializedName("place")
        private String place;

        @SerializedName("max")
        private Integer max;

        @SerializedName("min")
        private Integer min;

        @SerializedName("main")
        private String main;

        // Getters and setters
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getPlace() { return place; }
        public void setPlace(String place) { this.place = place; }
        public Integer getMax() { return max; }
        public void setMax(Integer max) { this.max = max; }
        public Integer getMin() { return min; }
        public void setMin(Integer min) { this.min = min; }
        public String getMain() { return main; }
        public void setMain(String main) { this.main = main; }
    }

    public static class UVIndex {
        @SerializedName("data")
        private List<UVIndexRecord> data;

        @SerializedName("recordDesc")
        private String recordDesc;

        public List<UVIndexRecord> getData() { return data; }
        public void setData(List<UVIndexRecord> data) { this.data = data; }
        public String getRecordDesc() { return recordDesc; }
        public void setRecordDesc(String recordDesc) { this.recordDesc = recordDesc; }
    }

    public static class UVIndexRecord {
        @SerializedName("place")
        private String place;

        @SerializedName("value")
        private Integer value;

        @SerializedName("desc")
        private String desc;

        public String getPlace() { return place; }
        public void setPlace(String place) { this.place = place; }
        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
    }

    public static class TemperatureData {
        @SerializedName("data")
        private List<TemperatureRecord> data;

        @SerializedName("recordTime")
        private String recordTime;

        public List<TemperatureRecord> getData() { return data; }
        public void setData(List<TemperatureRecord> data) { this.data = data; }
        public String getRecordTime() { return recordTime; }
        public void setRecordTime(String recordTime) { this.recordTime = recordTime; }
    }

    public static class TemperatureRecord {
        @SerializedName("place")
        private String place;

        @SerializedName("value")
        private Integer value;

        @SerializedName("unit")
        private String unit;

        public String getPlace() { return place; }
        public void setPlace(String place) { this.place = place; }
        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    public static class HumidityData {
        @SerializedName("data")
        private List<HumidityRecord> data;

        @SerializedName("recordTime")
        private String recordTime;

        public List<HumidityRecord> getData() { return data; }
        public void setData(List<HumidityRecord> data) { this.data = data; }
        public String getRecordTime() { return recordTime; }
        public void setRecordTime(String recordTime) { this.recordTime = recordTime; }
    }

    public static class HumidityRecord {
        @SerializedName("unit")
        private String unit;

        @SerializedName("value")
        private Integer value;

        @SerializedName("place")
        private String place;

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }
        public String getPlace() { return place; }
        public void setPlace(String place) { this.place = place; }
    }

    @SerializedName("rainfall")
    @Nullable
    private RainfallData rainfall;

    @SerializedName("icon")
    @NonNull
    private List<Integer> icon = new ArrayList<>();

    @SerializedName("iconUpdateTime")
    @NonNull
    private String iconUpdateTime = "";

    @SerializedName("uvindex")
    @Nullable
    private UVIndex uvindex;

    @SerializedName("temperature")
    @NonNull
    private TemperatureData temperature = new TemperatureData();

    @SerializedName("humidity")
    @NonNull
    private HumidityData humidity = new HumidityData();

    @SerializedName("updateTime")
    @NonNull
    private String updateTime = "";

    @SerializedName("warningMessage")
    @Nullable
    private List<String> warningMessage;

    @SerializedName("mintempFrom00To09")
    @Nullable
    private String mintempFrom00To09;

    @SerializedName("rainfallFrom00To12")
    @Nullable
    private String rainfallFrom00To12;

    @SerializedName("rainfallLastMonth")
    @Nullable
    private String rainfallLastMonth;

    @SerializedName("rainfallJanuaryToLastMonth")
    @Nullable
    private String rainfallJanuaryToLastMonth;

    @SerializedName("tcmessage")
    @Nullable
    private List<String> tcmessage;

    // Modify getters to handle nullable fields
    @Nullable
    public RainfallData getRainfall() {
        return rainfall;
    }

    @NonNull
    public List<Integer> getIcon() {
        return icon != null ? icon : new ArrayList<>();
    }

    @NonNull
    public String getIconUpdateTime() {
        return iconUpdateTime != null ? iconUpdateTime : "";
    }

    @Nullable
    public UVIndex getUvindex() {
        return uvindex;
    }

    @NonNull
    public TemperatureData getTemperature() {
        return temperature != null ? temperature : new TemperatureData();
    }

    @NonNull
    public HumidityData getHumidity() {
        return humidity != null ? humidity : new HumidityData();
    }

    @NonNull
    public String getUpdateTime() {
        return updateTime != null ? updateTime : "";
    }

    @Nullable
    public List<String> getWarningMessage() {
        return warningMessage;
    }

    @Nullable
    public String getMintempFrom00To09() {
        return mintempFrom00To09;
    }

    @Nullable
    public String getRainfallFrom00To12() {
        return rainfallFrom00To12;
    }

    @Nullable
    public String getRainfallLastMonth() {
        return rainfallLastMonth;
    }

    @Nullable
    public String getRainfallJanuaryToLastMonth() {
        return rainfallJanuaryToLastMonth;
    }

    @Nullable
    public List<String> getTcmessage() {
        return tcmessage;
    }

    public void setWarningMessage(@Nullable List<String> warningMessage) {
        this.warningMessage = warningMessage;
    }

    public void setRainfall(@Nullable RainfallData rainfall) {
        this.rainfall = rainfall;
    }

    public void setIcon(@NonNull List<Integer> icon) {
        this.icon = icon;
    }

    public void setIconUpdateTime(@NonNull String iconUpdateTime) {
        this.iconUpdateTime = iconUpdateTime;
    }

    public void setUvindex(@Nullable UVIndex uvindex) {
        this.uvindex = uvindex;
    }

    public void setTemperature(@NonNull TemperatureData temperature) {
        this.temperature = temperature;
    }

    public void setHumidity(@NonNull HumidityData humidity) {
        this.humidity = humidity;
    }

    public void setUpdateTime(@NonNull String updateTime) {
        this.updateTime = updateTime;
    }

    public void setMintempFrom00To09(@Nullable String mintempFrom00To09) {
        this.mintempFrom00To09 = mintempFrom00To09;
    }

    public void setRainfallFrom00To12(@Nullable String rainfallFrom00To12) {
        this.rainfallFrom00To12 = rainfallFrom00To12;
    }

    public void setRainfallLastMonth(@Nullable String rainfallLastMonth) {
        this.rainfallLastMonth = rainfallLastMonth;
    }

    public void setRainfallJanuaryToLastMonth(@Nullable String rainfallJanuaryToLastMonth) {
        this.rainfallJanuaryToLastMonth = rainfallJanuaryToLastMonth;
    }

    public void setTcmessage(@Nullable List<String> tcmessage) {
        this.tcmessage = tcmessage;
    }
}