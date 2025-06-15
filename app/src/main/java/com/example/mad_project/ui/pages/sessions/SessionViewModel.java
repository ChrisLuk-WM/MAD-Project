package com.example.mad_project.ui.pages.sessions;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.database.repository.HikingSessionRepository;

import java.util.List;

public class SessionViewModel extends AndroidViewModel {
    private final HikingSessionRepository repository;

    public SessionViewModel(Application application) {
        super(application);
        repository = new HikingSessionRepository(application);
    }

    public LiveData<HikingSessionEntity> getSession(long sessionId) {
        return repository.getSessionById(sessionId);
    }

    // Add method to get all sessions
    public LiveData<List<HikingSessionEntity>> getAllSessions() {
        return repository.getAllSessions();
    }
}