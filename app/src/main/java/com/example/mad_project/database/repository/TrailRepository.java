package com.example.mad_project.database.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.TrailDao;
import com.example.mad_project.database.entities.TrailEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrailRepository {
    private final TrailDao trailDao;
    private final ExecutorService executorService;

    public TrailRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        trailDao = db.trailDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Get all trails
    public LiveData<List<TrailEntity>> getAllTrails() {
        return trailDao.getAllTrails();
    }

    // Get trail by ID
    public LiveData<TrailEntity> getTrailById(long trailId) {
        return trailDao.getTrailById(trailId);
    }


    // Insert trail
    public void insert(TrailEntity trail) {
        executorService.execute(() -> trailDao.insert(trail));
    }

    // Update trail
    public void update(TrailEntity trail) {
        executorService.execute(() -> trailDao.update(trail));
    }

    // Delete trail
    public void delete(TrailEntity trail) {
        executorService.execute(() -> trailDao.delete(trail));
    }
}