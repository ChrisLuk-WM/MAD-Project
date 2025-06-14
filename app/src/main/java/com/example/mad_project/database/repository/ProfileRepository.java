package com.example.mad_project.database.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.EmergencyContactDao;
import com.example.mad_project.database.dao.ProfileDao;
import com.example.mad_project.database.entities.EmergencyContactEntity;
import com.example.mad_project.database.entities.ProfileEntity;
import com.example.mad_project.services.HikingRecommendationHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

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

    public LiveData<List<EmergencyContactEntity>> getEmergencyContacts(long profileId) {
        return emergencyContactDao.getContactsForProfile(profileId);
    }

    public LiveData<EmergencyContactEntity> getPrimaryContact(long profileId) {
        return emergencyContactDao.getPrimaryContact(profileId);
    }

    public void insertEmergencyContact(EmergencyContactEntity contact) {
        executorService.execute(() -> {
            if (contact.isPrimaryContact()) {
                // Remove primary status from other contacts
                updatePrimaryContactStatus(contact.getProfileId(), false);
            }
            emergencyContactDao.insert(contact);
        });
    }

    public interface ProfileCallback {
        void onProfileSaved(long profileId);
    }

    public void insert(ProfileEntity profile, ProfileCallback callback) {
        executorService.execute(() -> {
            long id = profileDao.insert(profile);
            new Handler(Looper.getMainLooper()).post(() -> callback.onProfileSaved(id));
        });
    }

    public void updateEmergencyContact(EmergencyContactEntity contact) {
        executorService.execute(() -> {
            if (contact.isPrimaryContact()) {
                // Remove primary status from other contacts
                updatePrimaryContactStatus(contact.getProfileId(), false);
            }
            emergencyContactDao.update(contact);
        });
    }

    public void deleteEmergencyContact(EmergencyContactEntity contact) {
        executorService.execute(() -> emergencyContactDao.delete(contact));
    }

    private void updatePrimaryContactStatus(long profileId, boolean isPrimary) {
        executorService.execute(() -> {
            // Add this query to EmergencyContactDao
            emergencyContactDao.updateAllContactsPrimaryStatus(profileId, isPrimary);
        });
    }

    public LiveData<ProfileEntity> getProfileById(long profileId) {
        return profileDao.getProfileById(profileId);
    }
    // Add more methods for CRUD operations
}