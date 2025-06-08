package com.example.mad_project.sensors;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mad_project.services.TrackingNotificationService;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.statistics.StatisticsType;

/**
 * Central controller for all sensor-related operations.
 * This class follows the Singleton pattern to ensure only one instance manages all sensors.
 */
public class SensorsController {
    private static SensorsController instance;
    private final Context context;
    private final GPSHandler gpsHandler;
    private final StatisticsManager statisticsManager;

    // LiveData for tracking status only
    private final MutableLiveData<Boolean> isTracking = new MutableLiveData<>(false);

    private SensorsController(Context context) {
        this.context = context.getApplicationContext();
        this.statisticsManager = StatisticsManager.getInstance();
        this.gpsHandler = new GPSHandler(this.context);
        initializeObservers();
    }

    public static synchronized SensorsController getInstance(Context context) {
        if (instance == null) {
            instance = new SensorsController(context);
        }
        return instance;
    }

    private void initializeObservers() {
        // Observe GPS updates
        gpsHandler.getCurrentLocation().observeForever(location -> {
            if (location != null) {
                statisticsManager.setValue(StatisticsType.LOCATION, location);
            }
        });
    }

    private void startNotificationService() {
        Intent serviceIntent = new Intent(context, TrackingNotificationService.class);
        serviceIntent.setAction("START_TRACKING");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    private void stopNotificationService() {
        Intent serviceIntent = new Intent(context, TrackingNotificationService.class);
        serviceIntent.setAction("STOP_TRACKING");
        context.startService(serviceIntent);
    }

    // Tracking Control Methods
    public void startTracking() {
        if (statisticsManager.isSessionActive()) return;

        statisticsManager.startSession();
        gpsHandler.startTracking();
        isTracking.setValue(true);
        startNotificationService();
    }

    public void stopTracking() {
        gpsHandler.stopTracking();
        statisticsManager.stopSession();
        isTracking.setValue(false);
        stopNotificationService();
    }

    public void pauseTracking() {
        gpsHandler.pauseTracking();
        isTracking.setValue(false);
    }

    public void resumeTracking() {
        gpsHandler.resumeTracking();
        isTracking.setValue(true);
    }

    // Getter Methods
    public LiveData<Boolean> getTrackingStatus() {
        return isTracking;
    }

    public Location getLastKnownLocation() {
        return statisticsManager.getValue(StatisticsType.LOCATION);
    }

    public boolean isTracking() {
        return isTracking.getValue() != null && isTracking.getValue();
    }
}