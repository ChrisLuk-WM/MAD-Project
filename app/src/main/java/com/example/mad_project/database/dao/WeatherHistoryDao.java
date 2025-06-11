package com.example.mad_project.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mad_project.database.entities.WeatherHistoryEntity;

import java.util.List;

@Dao
public interface WeatherHistoryDao {
    @Insert
    long insert(WeatherHistoryEntity weather);

    @Query("SELECT * FROM weather_history ORDER BY id DESC LIMIT 1")
    WeatherHistoryEntity getLatestWeather();

    @Query("SELECT * FROM weather_history WHERE rainfallData IS NOT NULL ORDER BY id DESC LIMIT 1")
    WeatherHistoryEntity getLatestRainfall();

    @Query("SELECT * FROM weather_history WHERE lightningOccur = 1 ORDER BY id DESC LIMIT 1")
    WeatherHistoryEntity getLatestLightning();

    @Query("SELECT * FROM weather_history WHERE datetime(updateTime) >= datetime('now', '-24 hours')")
    List<WeatherHistoryEntity> getLast24Hours();

    @Query("DELETE FROM weather_history WHERE id NOT IN (SELECT id FROM weather_history ORDER BY id DESC LIMIT 24)")
    void deleteOldRecords();
}