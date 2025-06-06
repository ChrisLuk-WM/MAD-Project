package com.example.mad_project.statistics;

import android.content.Context;
import android.location.Location;

import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.HikingSessionDao;
import com.example.mad_project.database.dao.HikingStatisticsDao;
import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.database.entities.HikingStatisticsEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

public class StatisticsCalculator extends Thread {
    private static volatile StatisticsCalculator instance;
    private final StatisticsManager statisticsManager;
    private final AtomicBoolean isRunning;
    private final HikingSessionDao sessionDao;
    private final HikingStatisticsDao statisticsDao;
    private HikingSessionEntity currentSession;
    private Location previousLocation;

    private StatisticsCalculator(Context context) {
        super("StatisticsCalculator-Thread");
        AppDatabase db = AppDatabase.getDatabase(context);
        this.sessionDao = db.hikingSessionDao();
        this.statisticsDao = db.hikingStatisticsDao();
        this.isRunning = new AtomicBoolean(false);
        this.statisticsManager = StatisticsManager.getInstance();
    }

    public static StatisticsCalculator getInstance(Context context) {
        if (instance == null) {
            synchronized (StatisticsCalculator.class) {
                if (instance == null) {
                    instance = new StatisticsCalculator(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void startSession(HikingSessionEntity session) {
        currentSession = session;
        isRunning.set(true);
        if (!isAlive()) {
            start();
        }
    }

    public void stopSession() {
        isRunning.set(false);
        // Final update
        saveStatistics();
    }

    @Override
    public void run() {
        while (isRunning.get()) {
            try {
                saveStatistics();
                Thread.sleep(5000); // 5 seconds interval
            } catch (InterruptedException e) {
                if (!isRunning.get()) break;
            } catch (Exception e) {
            }
        }
    }

    private void saveStatistics() {
        if (currentSession == null) return;

        Location location = statisticsManager.getValue(StatisticsType.LOCATION);
        if (location != null) {
            HikingStatisticsEntity statistics = new HikingStatisticsEntity();
            statistics.setSessionId(currentSession.getId());
            statistics.setTimestamp(System.currentTimeMillis());
            statistics.setLatitude(location.getLatitude());
            statistics.setLongitude(location.getLongitude());
            statistics.setAltitude(statisticsManager.getValue(StatisticsType.ALTITUDE));
            statistics.setSpeed(statisticsManager.getValue(StatisticsType.SPEED));
            statistics.setAccuracy(statisticsManager.getValue(StatisticsType.ACCURACY));
            statistics.setSteps(statisticsManager.getValue(StatisticsType.STEPS));
            statistics.setBearing(statisticsManager.getValue(StatisticsType.BEARING));

            statisticsDao.insert(statistics);
            updateAccumulatedValues(location);
            updateSession();
        }
    }


    private void updateAccumulatedValues(Location location) {
        if (previousLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(
                    previousLocation.getLatitude(), previousLocation.getLongitude(),
                    location.getLatitude(), location.getLongitude(),
                    results
            );

            Double currentDistance = statisticsManager.getValue(StatisticsType.TOTAL_DISTANCE);
            statisticsManager.setValue(StatisticsType.TOTAL_DISTANCE, currentDistance + results[0]);

            double elevDiff = location.getAltitude() - previousLocation.getAltitude();
            if (elevDiff > 0) {
                Double currentGain = statisticsManager.getValue(StatisticsType.TOTAL_ELEVATION_GAIN);
                statisticsManager.setValue(StatisticsType.TOTAL_ELEVATION_GAIN, currentGain + elevDiff);
            }
        }
        previousLocation = location;
    }

    private void updateSession() {
        currentSession.setDistance(statisticsManager.getValue(StatisticsType.TOTAL_DISTANCE));
        currentSession.setSteps(statisticsManager.getValue(StatisticsType.STEPS));
        currentSession.setAverageSpeed(statisticsManager.getValue(StatisticsType.SPEED));
        currentSession.setTotalElevationGain((int) statisticsManager.getValue(StatisticsType.TOTAL_ELEVATION_GAIN));

        // Update tracked path
        try {
            JSONArray pathArray = new JSONArray(currentSession.getTrackedPath());
            JSONObject point = new JSONObject();
            Location location = statisticsManager.getValue(StatisticsType.LOCATION);
            point.put("lat", location.getLatitude());
            point.put("lng", location.getLongitude());
            point.put("alt", statisticsManager.getValue(StatisticsType.ALTITUDE));
            point.put("time", System.currentTimeMillis());
            pathArray.put(point);
            currentSession.setTrackedPath(pathArray.toString());
        } catch (Exception e) {

        }

        sessionDao.update(currentSession);
    }
}