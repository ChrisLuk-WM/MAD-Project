package com.example.mad_project.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mad_project.database.entities.HikingStatisticsEntity;

import java.util.List;

@Dao
public interface HikingStatisticsDao {
    @Insert
    long insert(HikingStatisticsEntity statistics);

    @Query("SELECT * FROM hiking_statistics WHERE sessionId = :sessionId ORDER BY dateTime ASC")
    List<HikingStatisticsEntity> getAllStatisticsForSession(long sessionId);

    @Query("SELECT * FROM hiking_statistics WHERE sessionId = :sessionId AND dateTime > :fromTime ORDER BY dateTime ASC")
    List<HikingStatisticsEntity> getStatisticsAfterTime(long sessionId, long fromTime);

    @Query("DELETE FROM hiking_statistics WHERE sessionId = :sessionId")
    void deleteAllForSession(long sessionId);

}