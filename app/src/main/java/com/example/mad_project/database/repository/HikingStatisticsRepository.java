package com.example.mad_project.database.repository;

import android.app.Application;

import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.HikingStatisticsDao;
import com.example.mad_project.database.entities.HikingStatisticsEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HikingStatisticsRepository {
    private final HikingStatisticsDao hikingStatisticsDao;
    private final ExecutorService executorService;

    public HikingStatisticsRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        hikingStatisticsDao = db.hikingStatisticsDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(HikingStatisticsEntity statistics) {
        executorService.execute(() -> hikingStatisticsDao.insert(statistics));
    }

    public void deleteAllForSession(long sessionId) {
        executorService.execute(() -> hikingStatisticsDao.deleteAllForSession(sessionId));
    }

    public interface StatisticsCallback {
        void onStatisticsLoaded(List<HikingStatisticsEntity> statistics);
    }

    public void getAllStatisticsForSession(long sessionId, StatisticsCallback callback) {
        executorService.execute(() -> {
            List<HikingStatisticsEntity> statistics =
                    hikingStatisticsDao.getAllStatisticsForSession(sessionId);
            callback.onStatisticsLoaded(statistics);
        });
    }
}