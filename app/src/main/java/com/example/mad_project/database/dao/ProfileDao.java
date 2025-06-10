package com.example.mad_project.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.mad_project.database.entities.ProfileEntity;
import com.example.mad_project.database.entities.EmergencyContactEntity;

import java.util.List;

@Dao
public interface ProfileDao {
    @Insert
    long insert(ProfileEntity profile);

    @Update
    void update(ProfileEntity profile);

    @Delete
    void delete(ProfileEntity profile);

    @Query("SELECT * FROM profiles WHERE id = :profileId")
    LiveData<ProfileEntity> getProfile(long profileId);

    @Query("SELECT * FROM profiles LIMIT 1")
    LiveData<ProfileEntity> getCurrentProfile();

    @Query("UPDATE profiles SET " +
            "experienceLevel = :experienceLevel, " +
            "averageHikingDistance = :avgDistance, " +
            "hikingFrequency = :frequency " +
            "WHERE id = :profileId")
    void updateHikingExperience(long profileId, String experienceLevel,
                                float avgDistance, int frequency);

    @Query("SELECT * FROM profiles WHERE name LIKE :searchQuery")
    LiveData<List<ProfileEntity>> searchProfiles(String searchQuery);
}