package com.example.mad_project.ui.pages.sessions.fragments;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.mad_project.database.entities.HikingStatisticsEntity;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.statistics.StatisticsType;
import com.example.mad_project.ui.pages.sessions.SessionViewModel;
import com.example.mad_project.utils.Common;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SharedSessionViewModel extends ViewModel {
    private final MutableLiveData<List<HikingStatisticsEntity>> sessionStatistics = new MutableLiveData<>();
    private final MutableLiveData<LocalDateTime> sessionStartTime = new MutableLiveData<>();
    private boolean isRealTimeTracking = false;
    private long currentSessionId = -1;
    private final Handler updateHandler = new Handler(Looper.getMainLooper());
    private static final int UPDATE_INTERVAL = 5000;

    private final SessionViewModel sessionViewModel;
    private final StatisticsManager statisticsManager;

    private List<HikingStatisticsEntity> currentStats = new ArrayList<>();

    public SharedSessionViewModel(SessionViewModel sessionViewModel) {
        this.sessionViewModel = sessionViewModel;
        this.statisticsManager = StatisticsManager.getInstance();
    }

    public void initializeSession(long sessionId, boolean isRealTime) {
        if (currentSessionId == sessionId && isRealTimeTracking == isRealTime) {
            return;
        }

        cleanup(); // Clean up any existing session

        currentSessionId = sessionId;
        isRealTimeTracking = isRealTime;
        currentStats.clear();

        // Load historical data first
        loadHistoricalData();

        // If real-time tracking is enabled, start updates after historical data is loaded
        if (isRealTime) {
            startRealTimeUpdates();
        }
    }

    private void loadHistoricalData() {
        if (currentSessionId != -1) {
            sessionViewModel.getSessionStatistics(currentSessionId).observeForever(statistics -> {
                if (statistics != null && !statistics.isEmpty()) {
                    currentStats = new ArrayList<>(statistics);
                    sessionStartTime.setValue(statistics.get(0).getDateTime());
                    sessionStatistics.setValue(currentStats);
                } else if (isRealTimeTracking) {
                    // If no historical data for real-time session, initialize with current time
                    sessionStartTime.setValue(LocalDateTime.now());
                }
            });
        }
    }

    private void startRealTimeUpdates() {
        updateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRealTimeTracking) {
                    updateCurrentStatistics();
                    updateHandler.postDelayed(this, UPDATE_INTERVAL);
                }
            }
        }, UPDATE_INTERVAL);
    }

    private void updateCurrentStatistics() {
        HikingStatisticsEntity newStat = createStatisticsFromCurrent();
        if (newStat != null) {
            currentStats.add(newStat);
            sessionStatistics.setValue(new ArrayList<>(currentStats));
        }
    }

    private HikingStatisticsEntity createStatisticsFromCurrent() {
        Location location = statisticsManager.getValue(StatisticsType.LOCATION);
        Double speed = statisticsManager.getValue(StatisticsType.SPEED);
        Double altitude = statisticsManager.getValue(StatisticsType.ALTITUDE);
        Integer steps = statisticsManager.getValue(StatisticsType.STEPS);
        Float bearing = Common.convertToFloat(statisticsManager.getValue(StatisticsType.BEARING));
        Float accuracy = location != null ? Common.convertToFloat(location.getAccuracy()) : 0f;

        if (location == null) return null;

        HikingStatisticsEntity entity = new HikingStatisticsEntity();
        entity.setSessionId(currentSessionId);
        entity.setDateTime(LocalDateTime.now());
        entity.setLatitude(location.getLatitude());
        entity.setLongitude(location.getLongitude());
        entity.setSpeed(speed != null ? speed : 0.0);
        entity.setAltitude(altitude != null ? altitude : 0.0);
        entity.setSteps(steps != null ? steps : 0);
        entity.setBearing(bearing != null ? bearing : 0f);
        entity.setAccuracy(accuracy);

        return entity;
    }

    public LiveData<List<HikingStatisticsEntity>> getSessionStatistics() {
        return sessionStatistics;
    }

    public LiveData<LocalDateTime> getSessionStartTime() {
        return sessionStartTime;
    }

    public void cleanup() {
        isRealTimeTracking = false;
        updateHandler.removeCallbacksAndMessages(null);
        currentStats.clear();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cleanup();
    }

    // Factory for creating this ViewModel
    public static class Factory implements ViewModelProvider.Factory {
        private final SessionViewModel sessionViewModel;

        public Factory(SessionViewModel sessionViewModel) {
            this.sessionViewModel = sessionViewModel;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SharedSessionViewModel(sessionViewModel);
        }
    }
}