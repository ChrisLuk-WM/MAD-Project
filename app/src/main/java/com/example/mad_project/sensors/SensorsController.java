package com.example.mad_project.sensors;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mad_project.constants.ServiceConstants;
import com.example.mad_project.services.TrackingService;
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

    // LiveData for tracking status only
    private final MutableLiveData<Boolean> isTracking = new MutableLiveData<>(false);

    private SensorsController(Context context) {
        this.context = context.getApplicationContext();
        this.statisticsManager = StatisticsManager.getInstance();

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
    private void startServices() {
        Intent serviceIntent = new Intent(context, TrackingService.class);
        serviceIntent.setAction(ServiceConstants.ACTION_START_TRACKING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    private void stopServices() {
        Intent serviceIntent = new Intent(context, TrackingService.class);
        serviceIntent.setAction(ServiceConstants.ACTION_STOP_TRACKING);
        context.startService(serviceIntent);
    }

    public void startTracking() {
        if (statisticsManager.isSessionActive()) return;

        startServices();
        statisticsManager.startSession();
        for (SensorHandler handler : sensorHandlers) {
            handler.startTracking();
        }
        isTracking.setValue(true);
    }

    public void stopTracking() {
        for (SensorHandler handler : sensorHandlers) {
            handler.stopTracking();
        }
        statisticsManager.stopSession();
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