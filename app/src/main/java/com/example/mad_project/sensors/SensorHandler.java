package com.example.mad_project.sensors;

public interface SensorHandler {
    void startTracking();
    void stopTracking();
    void pauseTracking();
    void resumeTracking();
    boolean isTracking();
    void reset();
}