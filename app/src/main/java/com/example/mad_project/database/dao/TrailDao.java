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

    @Query("SELECT * FROM trails")
    LiveData<List<TrailEntity>> getAllTrails();

    @Query("SELECT * FROM trails WHERE id = :trailId")
    LiveData<TrailEntity> getTrailById(long trailId);


    @Query("SELECT * FROM trails")
    List<TrailEntity> getAllTrailsList();
}