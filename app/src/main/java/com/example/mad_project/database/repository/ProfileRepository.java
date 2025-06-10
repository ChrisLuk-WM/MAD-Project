package com.example.mad_project.database.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.EmergencyContactDao;
import com.example.mad_project.database.dao.ProfileDao;
import com.example.mad_project.database.entities.ProfileEntity;

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

    // Add more methods for CRUD operations
}