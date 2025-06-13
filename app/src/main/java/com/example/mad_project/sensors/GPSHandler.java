package com.example.mad_project.sensors;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.statistics.StatisticsType;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class GPSHandler implements SensorHandler {
    private static final long UPDATE_INTERVAL = 10000; // 10 seconds
    private static final long FASTEST_INTERVAL = 5000; // 5 seconds

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationCallback locationCallback;
    private final LocationRequest locationRequest;
    private final StatisticsManager statisticsManager;

    private final MutableLiveData<Location> currentLocation = new MutableLiveData<>();
    private boolean isTracking = false;

    public GPSHandler(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.statisticsManager = StatisticsManager.getInstance();

        // Configure location request
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
                .setIntervalMillis(UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .build();

        // Setup location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    processNewLocation(location);
                }
            }
        };
    }

    public void startTracking() {
        if (isTracking) return;

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());

        isTracking = true;
    }

    public void stopTracking() {
        if (!isTracking) return;

        fusedLocationClient.removeLocationUpdates(locationCallback);
        isTracking = false;
    }

    public void pauseTracking() {
        if (!isTracking) return;

        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public void resumeTracking() {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void processNewLocation(Location location) {
        currentLocation.setValue(location);

        // Update statistics in StatisticsManager
        Location previousLocation = statisticsManager.getValue(StatisticsType.LOCATION);
        statisticsManager.setValue(StatisticsType.LOCATION, location);
        statisticsManager.setValue(StatisticsType.SPEED, (double) location.getSpeed());
        statisticsManager.setValue(StatisticsType.ALTITUDE, location.getAltitude());
        statisticsManager.setValue(StatisticsType.BEARING, location.getBearing());
        statisticsManager.setValue(StatisticsType.ACCURACY, (double) location.getAccuracy());

        // Calculate total distance
        if (previousLocation != null) {
            double currentTotalDistance = statisticsManager.getValue(StatisticsType.TOTAL_DISTANCE);
            double newDistance = currentTotalDistance + previousLocation.distanceTo(location);
            statisticsManager.setValue(StatisticsType.TOTAL_DISTANCE, newDistance);

            // Calculate elevation gain
            double elevationDifference = location.getAltitude() - previousLocation.getAltitude();
            if (elevationDifference > 0) {
                double currentElevationGain = statisticsManager.getValue(StatisticsType.TOTAL_ELEVATION_GAIN);
                statisticsManager.setValue(StatisticsType.TOTAL_ELEVATION_GAIN,
                        currentElevationGain + elevationDifference);
            }
        }
    }

    // Only keep essential methods
    public LiveData<Location> getCurrentLocation() {
        return currentLocation;
    }

    public void processOnce(){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    public boolean isTracking() {
        return isTracking;
    }

    @Override
    public void reset() {
        currentLocation.setValue(null);
        statisticsManager.setValue(StatisticsType.LOCATION, null);
        statisticsManager.setValue(StatisticsType.TOTAL_DISTANCE, 0.0);
        statisticsManager.setValue(StatisticsType.TOTAL_ELEVATION_GAIN, 0.0);
    }
}