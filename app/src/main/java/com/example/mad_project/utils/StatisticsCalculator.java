package com.example.mad_project.utils;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.HikingSessionDao;
import com.example.mad_project.database.dao.HikingStatisticsDao;
import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.database.entities.HikingStatisticsEntity;
import com.example.mad_project.database.repository.HikingSessionRepository;
import com.example.mad_project.database.repository.HikingStatisticsRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class StatisticsCalculator extends Thread {
    private static final String TAG = "StatisticsCalculator";
    private static volatile StatisticsCalculator instance;

    private final AtomicBoolean isRunning;
    private final HikingSessionDao sessionDao;
    private final HikingStatisticsDao statisticsDao;
    private HikingSessionEntity currentSession;

    // Current statistics (thread-safe)
    private final AtomicReference<Location> lastLocation = new AtomicReference<>();
    private final AtomicInteger currentSteps = new AtomicInteger(0);
    private final AtomicReference<Double> currentSpeed = new AtomicReference<>(0.0);
    private final AtomicReference<Double> currentAltitude = new AtomicReference<>(0.0);
    private final AtomicReference<Float> currentBearing = new AtomicReference<>(0f);
    private final AtomicReference<Double> currentAccuracy = new AtomicReference<>(0.0);

    // Accumulated values
    private double totalDistance = 0;
    private double totalElevationGain = 0;
    private Location previousLocation;

    private StatisticsCalculator(Context context) {
        super("StatisticsCalculator-Thread");
        AppDatabase db = AppDatabase.getDatabase(context);
        this.sessionDao = db.hikingSessionDao();
        this.statisticsDao = db.hikingStatisticsDao();
        this.isRunning = new AtomicBoolean(false);
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

    // Simple update methods for sensors
    public void updateLocation(Location location) {
        lastLocation.set(location);
    }

    public void updateSteps(int steps) {
        currentSteps.set(steps);
    }

    public void updateSpeed(double speed) {
        currentSpeed.set(speed);
    }

    public void updateAltitude(double altitude) {
        currentAltitude.set(altitude);
    }

    public void updateBearing(float bearing) {
        currentBearing.set(bearing);
    }

    public void updateAccuracy(double accuracy) {
        currentAccuracy.set(accuracy);
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
                Log.e(TAG, "Calculator thread interrupted", e);
                if (!isRunning.get()) break;
            } catch (Exception e) {
                Log.e(TAG, "Error in calculator thread", e);
            }
        }
    }

    private void saveStatistics() {
        if (currentSession == null) return;

        Location location = lastLocation.get();
        if (location != null) {
            // Create and save statistics entity
            HikingStatisticsEntity statistics = new HikingStatisticsEntity();
            statistics.setSessionId(currentSession.getId());
            statistics.setTimestamp(System.currentTimeMillis());
            statistics.setLatitude(location.getLatitude());
            statistics.setLongitude(location.getLongitude());
            statistics.setAltitude(currentAltitude.get());
            statistics.setSpeed(currentSpeed.get());
            statistics.setAccuracy(currentAccuracy.get());
            statistics.setSteps(currentSteps.get());
            statistics.setBearing(currentBearing.get());

            statisticsDao.insert(statistics);

            // Update accumulated values
            updateAccumulatedValues(location);

            // Update session
            // updateSession();
        }
    }

    private void updateAccumulatedValues(Location location) {
        if (previousLocation != null) {
            // Update distance
            float[] results = new float[1];
            Location.distanceBetween(
                    previousLocation.getLatitude(), previousLocation.getLongitude(),
                    location.getLatitude(), location.getLongitude(),
                    results
            );
            totalDistance += results[0];

            // Update elevation gain
            double elevDiff = location.getAltitude() - previousLocation.getAltitude();
            if (elevDiff > 0) {
                totalElevationGain += elevDiff;
            }
        }
        previousLocation = location;
    }

    private void updateSession() {
        currentSession.setDistance(totalDistance);
        currentSession.setSteps(currentSteps.get());
        currentSession.setAverageSpeed(currentSpeed.get());
        currentSession.setTotalElevationGain((int) totalElevationGain);

        // Update tracked path
        try {
            JSONArray pathArray = new JSONArray(currentSession.getTrackedPath());
            JSONObject point = new JSONObject();
            Location location = lastLocation.get();
            point.put("lat", location.getLatitude());
            point.put("lng", location.getLongitude());
            point.put("alt", currentAltitude.get());
            point.put("time", System.currentTimeMillis());
            pathArray.put(point);
            currentSession.setTrackedPath(pathArray.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error updating tracked path", e);
        }

        sessionDao.update(currentSession);
    }

    public void reset() {
        totalDistance = 0;
        totalElevationGain = 0;
        previousLocation = null;
        lastLocation.set(null);
        currentSteps.set(0);
        currentSpeed.set(0.0);
        currentAltitude.set(0.0);
        currentBearing.set(0f);
        currentAccuracy.set(0.0);
    }
}