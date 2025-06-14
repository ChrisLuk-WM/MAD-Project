package com.example.mad_project.ui.pages.profile;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.database.entities.ProfileEntity;
import com.example.mad_project.database.repository.HikingSessionRepository;
import com.example.mad_project.database.repository.ProfileRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {
    private final ProfileRepository profileRepository;
    private final HikingSessionRepository hikingSessionRepository;
    private final LiveData<ProfileEntity> currentProfile;
    private final LiveData<List<HikingSessionEntity>> allSessions;
    private final MediatorLiveData<HikingSessionEntity> lastSession;

    public DashboardViewModel(Application application) {
        super(application);
        profileRepository = new ProfileRepository(application);
        hikingSessionRepository = new HikingSessionRepository(application);
        currentProfile = profileRepository.getCurrentProfile();
        allSessions = hikingSessionRepository.getAllSessions();
        lastSession = new MediatorLiveData<>();

        // Get the last session from all sessions
        lastSession.addSource(allSessions, sessions -> {
            if (sessions != null && !sessions.isEmpty()) {
                lastSession.setValue(sessions.get(0));
            } else {
                lastSession.setValue(null);
            }
        });
    }

    public LiveData<ProfileEntity> getCurrentProfile() {
        return currentProfile;
    }

    public LiveData<HikingSessionEntity> getLastSession() {
        return lastSession;
    }
}