package com.example.mad_project.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.mad_project.database.entities.ProfileEntity;
import com.example.mad_project.database.entities.EmergencyContactEntity;
import com.example.mad_project.services.HikingRecommendationHelper;

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

    @Query("UPDATE profiles SET " +
            "weeklyExerciseHours = :hours, " +
            "fitnessLevel = :fitness " +
            "WHERE id = :profileId")
    void updateFitnessInfo(long profileId, float hours, int fitness);

    @Query("UPDATE profiles SET " +
            "maxAltitudeClimbed = CASE " +
            "WHEN maxAltitudeClimbed < :altitude THEN :altitude " +
            "ELSE maxAltitudeClimbed END, " +
            "longestHikeKm = CASE " +
            "WHEN longestHikeKm < :distance THEN :distance " +
            "ELSE longestHikeKm END " +
            "WHERE id = :profileId")
    void updateHikingAchievements(long profileId, float altitude, float distance);

    @Query("SELECT " +
            "CAST(age AS FLOAT) as age, " +
            "weight, " +
            "height, " +
            "CAST(fitnessLevel AS FLOAT) / 10.0 + " +
            "    MIN(CAST(yearsOfExperience AS FLOAT) / 10.0, 0.2) + " +
            "    MIN(CAST(hikingFrequency AS FLOAT) / 20.0, 0.15) + " +
            "    MIN(weeklyExerciseHours / 20.0, 0.15) as fitnessLevel, " +
            "CAST(yearsOfExperience AS FLOAT) as experienceYears, " +  // Fixed: Added alias
            "weeklyExerciseHours, " +
            "maxAltitudeClimbed, " +
            "longestHikeKm " +
            "FROM profiles WHERE id = :profileId")
    LiveData<HikerProfileTuple> getHikerProfileData(long profileId);

    @Query("SELECT * FROM profiles WHERE id = :profileId")
    LiveData<ProfileEntity> getProfileById(long profileId);

    // Tuple class with Column annotations
    class HikerProfileTuple {
        @ColumnInfo(name = "age")
        public float age;

        @ColumnInfo(name = "weight")
        public float weight;

        @ColumnInfo(name = "height")
        public float height;

        @ColumnInfo(name = "fitnessLevel")
        public float fitnessLevel;

        @ColumnInfo(name = "experienceYears")
        public float experienceYears;

        @ColumnInfo(name = "weeklyExerciseHours")
        public float weeklyExerciseHours;

        @ColumnInfo(name = "maxAltitudeClimbed")
        public float maxAltitudeClimbed;

        @ColumnInfo(name = "longestHikeKm")
        public float longestHikeKm;

        public HikingRecommendationHelper.HikerProfile toHikerProfile() {
            return new HikingRecommendationHelper.HikerProfile(
                    age,
                    weight,
                    height,
                    fitnessLevel,
                    experienceYears,
                    weeklyExerciseHours,
                    maxAltitudeClimbed,
                    longestHikeKm
            );
        }
    }
}