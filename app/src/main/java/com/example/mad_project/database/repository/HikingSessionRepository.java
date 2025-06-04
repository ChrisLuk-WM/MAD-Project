package com.example.mad_project.database.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.HikingSessionDao;
import com.example.mad_project.database.entities.HikingSessionEntity;

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

    // Get all sessions
    public LiveData<List<HikingSessionEntity>> getAllSessions() {
        return hikingSessionDao.getAllSessions();
    }

    // Get session by ID
    public LiveData<HikingSessionEntity> getSessionById(long sessionId) {
        return hikingSessionDao.getSessionById(sessionId);
    }

    // Get sessions for specific trail
    public LiveData<List<HikingSessionEntity>> getSessionsForTrail(long trailId) {
        return hikingSessionDao.getSessionsForTrail(trailId);
    }

    // Insert session
    public void insert(HikingSessionEntity session) {
        executorService.execute(() -> hikingSessionDao.insert(session));
    }

    // Update session
    public void update(HikingSessionEntity session) {
        executorService.execute(() -> hikingSessionDao.update(session));
    }
}