package com.example.mad_project.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.statistics.StatisticsType;

public class StepCounterHandler implements SensorHandler, SensorEventListener {
    private static final String TAG = "StepCounterHandler";

    private final Context context;
    private final SensorManager sensorManager;
    private final Sensor stepCounterSensor;
    private final StatisticsManager statisticsManager;
    private final MutableLiveData<Integer> currentSteps = new MutableLiveData<>();

    private int initialSteps = -1;
    private boolean isTracking = false;

    public StepCounterHandler(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        this.statisticsManager = StatisticsManager.getInstance();
    }

    public boolean hasStepCounter() {
        return stepCounterSensor != null;
    }

    @Override
    public void startTracking() {
        if (!hasStepCounter() || isTracking) {
            return;
        }

        boolean success = sensorManager.registerListener(
                this,
                stepCounterSensor,
                SensorManager.SENSOR_DELAY_NORMAL
        );

        if (success) {
            isTracking = true;
            Log.d(TAG, "Step counter sensor started tracking");
        } else {
            Log.e(TAG, "Failed to start step counter tracking");
        }
    }

    @Override
    public void stopTracking() {
        if (isTracking) {
            sensorManager.unregisterListener(this);
            isTracking = false;
            initialSteps = -1;
            Log.d(TAG, "Step counter tracking stopped");
        }
    }

    @Override
    public void pauseTracking() {
        if (isTracking) {
            sensorManager.unregisterListener(this);
            isTracking = false;
            Log.d(TAG, "Step counter tracking paused");
        }
    }

    @Override
    public void resumeTracking() {
        if (!isTracking && hasStepCounter()) {
            boolean success = sensorManager.registerListener(
                    this,
                    stepCounterSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
            if (success) {
                isTracking = true;
                Log.d(TAG, "Step counter tracking resumed");
            }
        }
    }

    @Override
    public boolean isTracking() {
        return isTracking;
    }

    @Override
    public void reset() {
        initialSteps = -1;
        currentSteps.setValue(0);
        statisticsManager.setValue(StatisticsType.STEPS, 0);
        Log.d(TAG, "Step counter reset");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalSteps = (int) event.values[0];

            if (initialSteps == -1) {
                initialSteps = totalSteps;
                currentSteps.setValue(0);
                statisticsManager.setValue(StatisticsType.STEPS, 0);
                Log.d(TAG, "Initial steps set to: " + initialSteps);
                return;
            }

            int steps = totalSteps - initialSteps;
            currentSteps.setValue(steps);
            statisticsManager.setValue(StatisticsType.STEPS, steps);
            Log.d(TAG, "Steps updated: " + steps);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            Log.d(TAG, "Step counter accuracy changed to: " + accuracy);
        }
    }

    public LiveData<Integer> getCurrentSteps() {
        return currentSteps;
    }
}