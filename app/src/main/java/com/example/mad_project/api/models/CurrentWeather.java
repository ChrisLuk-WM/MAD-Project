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
        private double value;

        @SerializedName("desc")
        private String desc;

        public String getPlace() { return place; }
        public void setPlace(String place) { this.place = place; }
        public double getValue() { return value; }
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
    @Nullable
    private List<Integer> icon;

    @SerializedName("iconUpdateTime")
    @Nullable
    private String iconUpdateTime;

    @SerializedName("specialWxTips")
    @Nullable
    private List<String> specialWxTips;

    @SerializedName("uvindex")
    @Nullable
    private Object uvindex;

    @SerializedName("temperature")
    @Nullable
    private TemperatureData temperature;

    @SerializedName("humidity")
    @Nullable
    private HumidityData humidity;

    @SerializedName("updateTime")
    @Nullable
    private String updateTime;

    @SerializedName("warningMessage")
    @Nullable
    private List<String> warningMessage;

    @SerializedName("tcmessage")
    @Nullable
    private List<String> tcmessage;

    // Updated getters with safe defaults
    @NonNull
    public List<Integer> getIcon() {
        return icon != null ? icon : new ArrayList<>();
    }

    @NonNull
    public String getIconUpdateTime() {
        return iconUpdateTime != null ? iconUpdateTime : "";
    }

    @NonNull
    public List<String> getSpecialWxTips() {
        return specialWxTips != null ? specialWxTips : new ArrayList<>();
    }

    @NonNull
    public TemperatureData getTemperature() {
        if (temperature == null) {
            temperature = new TemperatureData();
            temperature.setData(new ArrayList<>());
        }
        return temperature;
    }

    @NonNull
    public HumidityData getHumidity() {
        if (humidity == null) {
            humidity = new HumidityData();
            humidity.setData(new ArrayList<>());
        }
        return humidity;
    }

    @NonNull
    public String getUpdateTime() {
        return updateTime != null ? updateTime : "";
    }

    @NonNull
    public List<String> getWarningMessage() {
        return warningMessage != null ? warningMessage : new ArrayList<>();
    }

    @NonNull
    public List<String> getTcmessage() {
        return tcmessage != null ? tcmessage : new ArrayList<>();
    }

    @Nullable
    public RainfallData getRainfall() {
        return rainfall;
    }

    @Nullable
    public UVIndex getUvindex() {
        if (uvindex instanceof String || uvindex == null) {
            return null;
        }
        try {
            // If it's a JSON object, Gson will handle the conversion
            return (UVIndex) uvindex;
        } catch (Exception e) {
            return null;
        }
    }

    // Helper methods for easy access to common data
    public int getTemperatureForStation(String station, int defaultValue) {
        if (temperature != null && temperature.getData() != null) {
            return temperature.getData().stream()
                    .filter(t -> t.getPlace().equals(station))
                    .findFirst()
                    .map(TemperatureRecord::getValue)
                    .orElse(defaultValue);
        }
        return defaultValue;
    }

    public int getHumidityValue(int defaultValue) {
        if (humidity != null && humidity.getData() != null && !humidity.getData().isEmpty()) {
            return humidity.getData().get(0).getValue() != null ?
                    humidity.getData().get(0).getValue() : defaultValue;
        }
        return defaultValue;
    }

    public double getUVIndexValue(double defaultValue) {
        UVIndex uv = getUvindex();
        if (uv != null && uv.getData() != null && !uv.getData().isEmpty()) {
            try {
                return uv.getData().get(0).getValue();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public String getUVIndexDescription(String defaultValue) {
        UVIndex uv = getUvindex();
        if (uv != null && uv.getData() != null && !uv.getData().isEmpty()) {
            try {
                return uv.getData().get(0).getDesc() != null ?
                        uv.getData().get(0).getDesc() : defaultValue;
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    // Simple setters
    public void setRainfall(RainfallData rainfall) {
        this.rainfall = rainfall;
    }

    public void setIcon(List<Integer> icon) {
        this.icon = icon;
    }

    public void setIconUpdateTime(String iconUpdateTime) {
        this.iconUpdateTime = iconUpdateTime;
    }

    public void setSpecialWxTips(List<String> specialWxTips) {
        this.specialWxTips = specialWxTips;
    }

    public void setUvindex(Object uvindex) {
        this.uvindex = uvindex;
    }

    public void setTemperature(TemperatureData temperature) {
        this.temperature = temperature;
    }

    public void setHumidity(HumidityData humidity) {
        this.humidity = humidity;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public void setWarningMessage(List<String> warningMessage) {
        this.warningMessage = warningMessage;
    }

    public void setTcmessage(List<String> tcmessage) {
        this.tcmessage = tcmessage;
    }
}