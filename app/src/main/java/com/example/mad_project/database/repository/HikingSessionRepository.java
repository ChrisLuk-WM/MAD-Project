package com.example.mad_project.database.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.HikingSessionDao;
import com.example.mad_project.database.entities.HikingSessionEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HikingSessionRepository {
    private final HikingSessionDao hikingSessionDao;
    private final ExecutorService executorService;

    public HikingSessionRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        hikingSessionDao = db.hikingSessionDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Get active session (has start time but no end time)
    public HikingSessionEntity getActiveSessionSync() {
        return hikingSessionDao.getActiveSessionSync();
    }

    public LiveData<HikingSessionEntity> getActiveSession() {
        return hikingSessionDao.getActiveSession();
    }

    // Get planned sessions (no start time and no end time)
    public LiveData<List<HikingSessionEntity>> getPlannedSessions() {
        return hikingSessionDao.getPlannedSessions();
    }

    // End current session
    public LiveData<Long> endCurrentSession() {
        MutableLiveData<Long> result = new MutableLiveData<>();

        executorService.execute(() -> {
            try {
                // Get the active session
                HikingSessionEntity activeSession = hikingSessionDao.getActiveSessionSync();
                if (activeSession != null) {
                    // Set end time to now
                    activeSession.setEndTime(LocalDateTime.now());
                    // Update session
                    hikingSessionDao.update(activeSession);
                    // Return the session ID
                    result.postValue(activeSession.getId());
                } else {
                    result.postValue(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.postValue(null);
            }
        });

        return result;
    }

    // Existing methods...
    public LiveData<List<HikingSessionEntity>> getAllSessions() {
        return hikingSessionDao.getAllSessions();
    }

    public LiveData<HikingSessionEntity> getSessionById(long sessionId) {
        return hikingSessionDao.getSessionById(sessionId);
    }

    public LiveData<List<HikingSessionEntity>> getSessionsForTrail(long trailId) {
        return hikingSessionDao.getSessionsForTrail(trailId);
    }

    public void insert(HikingSessionEntity session) {
        executorService.execute(() -> hikingSessionDao.insert(session));
    }

    public void update(HikingSessionEntity session) {
        executorService.execute(() -> hikingSessionDao.update(session));
    }
}