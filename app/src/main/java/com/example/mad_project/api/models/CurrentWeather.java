package com.example.mad_project.api.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        public List<UVIndexRecord> getData() {
            return data != null ? data : new ArrayList<>();
        }
    }

    public static class UVIndexRecord {
        @SerializedName("value")
        private double value;
        @SerializedName("desc")
        private String desc;

        public double getValue() { return value; }
        public String getDesc() { return desc; }
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

    // Add fields for handling unknown properties
    @SerializedName("rainfall")
    @Nullable
    private RainfallData rainfall;

    @SerializedName("icon")
    @NonNull
    private List<Integer> icon = new ArrayList<>();

    @SerializedName("temperature")
    @NonNull
    private TemperatureData temperature = new TemperatureData();

    @SerializedName("humidity")
    @NonNull
    private HumidityData humidity = new HumidityData();

    @SerializedName("updateTime")
    @NonNull
    private String updateTime = "";

    @SerializedName("uvindex")
    @Nullable
    private String uvindex;  // Changed to String since it's often empty

    @SerializedName("warningMessage")
    @Nullable
    private List<String> warningMessage;

    @SerializedName("tcmessage")
    @Nullable
    private Object tcmessageRaw;

    // Updated getters with safe defaults
    @NonNull
    public List<Integer> getIcon() {
        return icon != null ? icon : new ArrayList<>();
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
        if (tcmessageRaw == null) {
            return new ArrayList<>();
        }

        if (tcmessageRaw instanceof String) {
            String message = (String) tcmessageRaw;
            return message.isEmpty() ? new ArrayList<>() : Collections.singletonList(message);
        }

        try {
            return (List<String>) tcmessageRaw;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Nullable
    public RainfallData getRainfall() {
        return rainfall;
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

    // Simple setters
    public void setRainfall(RainfallData rainfall) {
        this.rainfall = rainfall;
    }

    public void setIcon(List<Integer> icon) {
        this.icon = icon;
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

    @Nullable
    public UVIndex getUvindex() {
        if (uvindex == null || uvindex instanceof String) {
            return null;
        }
        try {
            Gson gson = new Gson();
            String jsonStr = gson.toJson(uvindex);
            return gson.fromJson(jsonStr, UVIndex.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Helper method for UV index value
    public double getUVIndexValue(double defaultValue) {
        UVIndex uv = getUvindex();
        if (uv != null && !uv.getData().isEmpty()) {
            try {
                return uv.getData().get(0).getValue();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    // Helper method for UV index description
    public String getUVIndexDescription(String defaultValue) {
        UVIndex uv = getUvindex();
        if (uv != null && !uv.getData().isEmpty()) {
            try {
                return uv.getData().get(0).getDesc() != null ?
                        uv.getData().get(0).getDesc() : defaultValue;
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}