package com.example.mad_project.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mad_project.database.entities.HikingSessionEntity;

import java.util.List;

@Dao
public interface HikingSessionDao {
    @Insert
    long insert(HikingSessionEntity session);

    @Update
    void update(HikingSessionEntity session);

    @Query("SELECT * FROM hiking_sessions WHERE trailId = :trailId")
    LiveData<List<HikingSessionEntity>> getSessionsForTrail(long trailId);

    @Query("SELECT * FROM hiking_sessions ORDER BY startTime DESC")
    LiveData<List<HikingSessionEntity>> getAllSessions();

    @Query("SELECT * FROM hiking_sessions WHERE id = :sessionId")
    LiveData<HikingSessionEntity> getSessionById(long sessionId);
}