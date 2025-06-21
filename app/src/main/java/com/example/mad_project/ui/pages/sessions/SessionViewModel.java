package com.example.mad_project.ui.pages.sessions;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.database.entities.HikingStatisticsEntity;
import com.example.mad_project.database.repository.HikingSessionRepository;
import com.example.mad_project.database.repository.HikingStatisticsRepository;

import java.util.List;

public class SessionViewModel extends AndroidViewModel {
    private final HikingSessionRepository sessionRepository;
    private final HikingStatisticsRepository statisticsRepository;
    private final MutableLiveData<List<HikingStatisticsEntity>> sessionStatistics = new MutableLiveData<>();

    public SessionViewModel(Application application) {
        super(application);
        sessionRepository = new HikingSessionRepository(application);
        statisticsRepository = new HikingStatisticsRepository(application);
    }

    public LiveData<HikingSessionEntity> getSession(long sessionId) {
        return sessionRepository.getSessionById(sessionId);
    }

    public LiveData<List<HikingSessionEntity>> getAllSessions() {
        return sessionRepository.getAllSessions();
    }

    public HikingSessionEntity getActiveSessionSync() {
        return sessionRepository.getActiveSessionSync();
    }

    public LiveData<HikingSessionEntity> getActiveSession() {
        return sessionRepository.getActiveSession();
    }


    public LiveData<List<HikingSessionEntity>> getPlannedSessions() {
        return sessionRepository.getPlannedSessions();
    }

    public LiveData<Long> endCurrentSession() {
        return sessionRepository.endCurrentSession();
    }

    public void updateSession(HikingSessionEntity session) {
        sessionRepository.update(session);
    }

    public LiveData<List<HikingStatisticsEntity>> getSessionStatistics(long sessionId) {
        // Load statistics using callback
        statisticsRepository.getAllStatisticsForSession(sessionId,
                statistics -> sessionStatistics.postValue(statistics));
        return sessionStatistics;
    }
}