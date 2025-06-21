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

    public LiveData<List<HikingSessionEntity>> getAllSessions() {
        return repository.getAllSessions();
    }

    public HikingSessionEntity getActiveSessionSync() {
        return repository.getActiveSessionSync();
    }

    public LiveData<HikingSessionEntity> getActiveSession() {
        return repository.getActiveSession();
    }


    public LiveData<List<HikingSessionEntity>> getPlannedSessions() {
        return repository.getPlannedSessions();
    }

    public LiveData<Long> endCurrentSession() {
        return repository.endCurrentSession();
    }

    public void updateSession(HikingSessionEntity session) {
        repository.update(session);
    }

    public void refreshData() {
        // Trigger a refresh of the data if needed
        // This might be needed if your repository caches data
    }
}