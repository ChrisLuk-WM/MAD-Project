package com.example.mad_project.sensors;

import android.content.Context;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mad_project.services.TrackingWorkManager;
import com.example.mad_project.statistics.StatisticsCalculator;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.statistics.StatisticsType;

import java.util.ArrayList;
import java.util.List;

/**
 * Central controller for all sensor-related operations.
 * This class follows the Singleton pattern to ensure only one instance manages all sensors.
 */
public class SensorsController {
    private static SensorsController instance;
    private final Context context;
    private final List<SensorHandler> sensorHandlers = new ArrayList<>();
    private final StatisticsManager statisticsManager;
    private final StatisticsCalculator statisticsCalculator;
    private final TrackingWorkManager trackingWorkManager;

    // LiveData for tracking status only
    private final MutableLiveData<Boolean> isTracking = new MutableLiveData<>(false);

    private SensorsController(Context context) {
        this.context = context.getApplicationContext();
        this.trackingWorkManager = new TrackingWorkManager(context);
        this.statisticsManager = StatisticsManager.getInstance();
        this.statisticsCalculator = StatisticsCalculator.getInstance(context);

        initializeSensorHandlers();
    }

    private void initializeSensorHandlers() {
        // Add all sensor handlers to the list
        sensorHandlers.add(new GPSHandler(context));
        sensorHandlers.add(new StepCounterHandler(context));
        // Add more handlers here as needed
    }

    public static synchronized SensorsController getInstance(Context context) {
        if (instance == null) {
            instance = new SensorsController(context);
        }
        return instance;
    }

    public void startTracking() {
        if (statisticsManager.isSessionActive()) return;

        trackingWorkManager.startTracking();
        statisticsCalculator.startSession();
        statisticsManager.startSession();
        for (SensorHandler handler : sensorHandlers) {
            handler.startTracking();
        }
        isTracking.setValue(true);
    }

    public void stopTracking() {
        trackingWorkManager.stopTracking();
        for (SensorHandler handler : sensorHandlers) {
            handler.stopTracking();
        }
        statisticsManager.stopSession();
        // Stop statistics calculation
        statisticsCalculator.stopSession();
        isTracking.setValue(false);
    }

    public void pauseTracking() {
        for (SensorHandler handler : sensorHandlers) {
            handler.pauseTracking();
        }
        isTracking.setValue(false);
    }

    public void resumeTracking() {
        for (SensorHandler handler : sensorHandlers) {
            handler.resumeTracking();
        }
        isTracking.setValue(true);
    }

    public void resetAllSensors() {
        for (SensorHandler handler : sensorHandlers) {
            handler.reset();
        }
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