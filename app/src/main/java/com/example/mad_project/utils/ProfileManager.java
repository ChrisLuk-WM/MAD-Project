package com.example.mad_project.utils;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.ProfileDao;
import com.example.mad_project.database.entities.ProfileEntity;
import com.example.mad_project.database.repository.ProfileRepository;
import com.example.mad_project.services.HikingRecommendationHelper;

public class ProfileManager {
    private static final String TAG = "ProfileManager";
    private static ProfileManager instance;
    private final ProfileRepository repository;
    private final LiveData<ProfileEntity> currentProfile;
    private final AppDatabase database;

    private ProfileManager(@NonNull Context context) {
        if (!(context.getApplicationContext() instanceof Application)) {
            throw new IllegalArgumentException("Context must be from an Application");
        }
        Application app = (Application) context.getApplicationContext();
        repository = new ProfileRepository(app);
        database = AppDatabase.getDatabase(app);
        currentProfile = repository.getCurrentProfile();
    }

    public static synchronized ProfileManager getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new ProfileManager(context.getApplicationContext());
        }
        return instance;
    }

    // Synchronous method to get fresh HikerProfile data
    @NonNull
    public HikingRecommendationHelper.HikerProfile getHikerProfileSync(long profileId) {
        try {
            // Check if we have any profile data
            ProfileEntity profile = database.profileDao()
                    .getProfileById(profileId)
                    .getValue();

            if (profile == null) {
                Log.d(TAG, "No profile found, returning default profile");
                return repository.getDefaultHikerProfile();
            }

            // Create a HikerProfile from the ProfileEntity
            return new HikingRecommendationHelper.HikerProfile(
                    profile.getAge(),
                    profile.getWeight(),
                    profile.getHeight(),
                    profile.getFitnessLevel() / 10.0f, // Normalize fitness level
                    profile.getYearsOfExperience(),
                    profile.getWeeklyExerciseHours(),
                    profile.getMaxAltitudeClimbed(),
                    profile.getLongestHikeKm()
            );
        } catch (Exception e) {
            Log.e(TAG, "Error getting hiker profile: " + e.getMessage());
            return repository.getDefaultHikerProfile();
        }
    }

    // Original LiveData methods remain unchanged
    public LiveData<ProfileEntity> getCurrentProfile() {
        return currentProfile;
    }

    public void updateProfile(ProfileEntity profile) {
        repository.update(profile);
    }

    public void insertProfile(ProfileEntity profile) {
        repository.insert(profile);
    }

    public LiveData<ProfileEntity> getProfileById(long profileId) {
        return repository.getProfileById(profileId);
    }

    public void updateFitnessInfo(long profileId, float weeklyHours, int fitnessLevel) {
        repository.updateFitnessInfo(profileId, weeklyHours, fitnessLevel);
    }

    public void updateHikingAchievements(long profileId, float altitude, float distance) {
        repository.updateHikingAchievements(profileId, altitude, distance);
    }
}