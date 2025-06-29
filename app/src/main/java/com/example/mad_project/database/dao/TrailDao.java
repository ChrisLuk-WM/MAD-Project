package com.example.mad_project.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mad_project.database.entities.TrailEntity;

import java.util.List;

@Dao
public interface TrailDao {
    @Insert
    long insert(TrailEntity trail);

    @Update
    void update(TrailEntity trail);

    @Delete
    void delete(TrailEntity trail);

    @Query("SELECT * FROM trails WHERE id != 1")
    LiveData<List<TrailEntity>> getAllTrails();

    @Query("SELECT * FROM trails WHERE id = :trailId")
    LiveData<TrailEntity> getTrailById(long trailId);


    @Query("SELECT * FROM trails WHERE id != 1")
    List<TrailEntity> getAllTrailsList();

    @Query("SELECT * FROM trails WHERE id = :trailId LIMIT 1")
    TrailEntity getTrailByIdSync(long trailId);
}