package com.example.mad_project.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.mad_project.database.converters.WeatherConverters;

import java.util.List;
import java.util.Map;

@Entity(tableName = "weather_history")
@TypeConverters(WeatherConverters.class)
public class WeatherHistoryEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String updateTime;
    private List<Integer> icon;
    private String iconUpdateTime;

    // Temperature data
    private double temperature;
    private String temperatureUnit;
    private String temperatureRecordTime;

    // Humidity data
    private double humidity;
    private String humidityUnit;
    private String humidityRecordTime;

    // Rainfall data
    private Map<String, Double> rainfallData; // Map of place to max rainfall
    private String rainfallStartTime;
    private String rainfallEndTime;
    private String rainfallUnit;

    // Lightning data (if available)
    private Boolean lightningOccur;
    private String lightningPlace;
    private String lightningStartTime;
    private String lightningEndTime;

    private List<String> warningMessages;
    private String uvIndexValue;
    private String uvIndexDesc;

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(@NonNull String updateTime) { this.updateTime = updateTime; }

    public List<Integer> getIcon() { return icon; }
    public void setIcon(List<Integer> icon) { this.icon = icon; }

    public String getIconUpdateTime() { return iconUpdateTime; }
    public void setIconUpdateTime(String iconUpdateTime) { this.iconUpdateTime = iconUpdateTime; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public String getTemperatureUnit() { return temperatureUnit; }
    public void setTemperatureUnit(String temperatureUnit) { this.temperatureUnit = temperatureUnit; }

    public String getTemperatureRecordTime() { return temperatureRecordTime; }
    public void setTemperatureRecordTime(String temperatureRecordTime) { this.temperatureRecordTime = temperatureRecordTime; }

    public double getHumidity() { return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }

    public String getHumidityUnit() { return humidityUnit; }
    public void setHumidityUnit(String humidityUnit) { this.humidityUnit = humidityUnit; }

    public String getHumidityRecordTime() { return humidityRecordTime; }
    public void setHumidityRecordTime(String humidityRecordTime) { this.humidityRecordTime = humidityRecordTime; }

    public List<String> getWarningMessages() { return warningMessages; }
    public void setWarningMessages(List<String> warningMessages) { this.warningMessages = warningMessages; }

    public String getUvIndexValue() { return uvIndexValue; }
    public void setUvIndexValue(String uvIndexValue) { this.uvIndexValue = uvIndexValue; }

    public String getUvIndexDesc() { return uvIndexDesc; }
    public void setUvIndexDesc(String uvIndexDesc) { this.uvIndexDesc = uvIndexDesc; }

    // New getters and setters for rainfall
    public Map<String, Double> getRainfallData() { return rainfallData; }
    public void setRainfallData(Map<String, Double> rainfallData) { this.rainfallData = rainfallData; }

    public String getRainfallStartTime() { return rainfallStartTime; }
    public void setRainfallStartTime(String rainfallStartTime) { this.rainfallStartTime = rainfallStartTime; }

    public String getRainfallEndTime() { return rainfallEndTime; }
    public void setRainfallEndTime(String rainfallEndTime) { this.rainfallEndTime = rainfallEndTime; }

    public String getRainfallUnit() { return rainfallUnit; }
    public void setRainfallUnit(String rainfallUnit) { this.rainfallUnit = rainfallUnit; }

    // New getters and setters for lightning
    public Boolean getLightningOccur() { return lightningOccur; }
    public void setLightningOccur(Boolean lightningOccur) { this.lightningOccur = lightningOccur; }

    public String getLightningPlace() { return lightningPlace; }
    public void setLightningPlace(String lightningPlace) { this.lightningPlace = lightningPlace; }

    public String getLightningStartTime() { return lightningStartTime; }
    public void setLightningStartTime(String lightningStartTime) { this.lightningStartTime = lightningStartTime; }

    public String getLightningEndTime() { return lightningEndTime; }
    public void setLightningEndTime(String lightningEndTime) { this.lightningEndTime = lightningEndTime; }
}