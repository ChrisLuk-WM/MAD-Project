package com.example.mad_project.database.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.EmergencyContactDao;
import com.example.mad_project.database.dao.ProfileDao;
import com.example.mad_project.database.entities.ProfileEntity;
import com.example.mad_project.services.HikingRecommendationHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileRepository {
    private final ProfileDao profileDao;
    private final EmergencyContactDao emergencyContactDao;
    private final LiveData<ProfileEntity> currentProfile;
    private final ExecutorService executorService;

    public ProfileRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        profileDao = db.profileDao();
        emergencyContactDao = db.emergencyContactDao();
        currentProfile = profileDao.getCurrentProfile();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<ProfileEntity> getCurrentProfile() {
        return currentProfile;
    }

    public void insert(ProfileEntity profile) {
        executorService.execute(() -> {
            profileDao.insert(profile);
        });
    }

    public void update(ProfileEntity profile) {
        executorService.execute(() -> {
            profileDao.update(profile);
        });
    }


    public void updateFitnessInfo(long profileId, float weeklyHours, int fitnessLevel) {
        executorService.execute(() ->
                profileDao.updateFitnessInfo(profileId, weeklyHours, fitnessLevel));
    }

    public void updateHikingAchievements(long profileId, float altitude, float distance) {
        executorService.execute(() ->
                profileDao.updateHikingAchievements(profileId, altitude, distance));
    }

    public LiveData<HikingRecommendationHelper.HikerProfile> getHikerProfile(long profileId) {
        return Transformations.map(
                profileDao.getHikerProfileData(profileId),
                ProfileDao.HikerProfileTuple::toHikerProfile
        );
    }

    // Fallback method for when no profile exists
    public HikingRecommendationHelper.HikerProfile getDefaultHikerProfile() {
        return new HikingRecommendationHelper.HikerProfile(
                35f,   // average age
                75f,   // average weight in kg
                170f,  // average height in cm
                0.5f,  // medium fitness level
                2f,    // 2 years experience
                5f,    // 5 hours weekly exercise
                500f,  // 500m max altitude
                10f    // 10km longest hike
        );
    }

    // Add more methods for CRUD operations
}