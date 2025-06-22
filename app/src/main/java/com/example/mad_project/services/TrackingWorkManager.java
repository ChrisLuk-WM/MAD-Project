package com.example.mad_project.services;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.mad_project.sensors.SensorsController;
import com.example.mad_project.statistics.StatisticsManager;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TrackingWorkManager {
    private static final String TRACKING_WORK_NAME = "TrackingWork";
    private static final String WEATHER_WORK_NAME = "WeatherWork";
    private final WorkManager workManager;
    private final Context context;

    public TrackingWorkManager(Context context) {
        this.context = context.getApplicationContext();
        this.workManager = WorkManager.getInstance(context);
        if (!StatisticsManager.isInitialized()) {
            StatisticsManager.init(context);
        }
    }
    public void startTracking() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        // Create an expedited one-time work request
        OneTimeWorkRequest trackingRequest = new OneTimeWorkRequest.Builder(TrackingWorker.class)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .keepResultsForAtLeast(Duration.ZERO) // Don't keep results
                .build();

        // Enqueue the work
        workManager.enqueueUniqueWork(
                TRACKING_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                trackingRequest
        );

        // Weather work remains the same
        OneTimeWorkRequest weatherRequest = new OneTimeWorkRequest.Builder(WeatherWorker.class)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .build();

        workManager.enqueueUniqueWork(
                WEATHER_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                weatherRequest
        );

        WeatherService.getInstance(context).onTrackingStarted();
    }

    public void stopTracking() {
        // Cancel both immediate and periodic work
        workManager.cancelUniqueWork(TRACKING_WORK_NAME + "_immediate");
        workManager.cancelUniqueWork(TRACKING_WORK_NAME);
        workManager.cancelUniqueWork(WEATHER_WORK_NAME);
        WeatherService.getInstance(context).onTrackingStopped();
    }

    public boolean isTracking() {
        try {
            ListenableFuture<List<WorkInfo>> immediateWorkInfo =
                    workManager.getWorkInfosForUniqueWork(TRACKING_WORK_NAME + "_immediate");
            ListenableFuture<List<WorkInfo>> periodicWorkInfo =
                    workManager.getWorkInfosForUniqueWork(TRACKING_WORK_NAME);

            List<WorkInfo> immediateList = immediateWorkInfo.get();
            List<WorkInfo> periodicList = periodicWorkInfo.get();

            boolean immediateRunning = false;
            boolean periodicRunning = false;

            if (immediateList != null && !immediateList.isEmpty()) {
                WorkInfo.State state = immediateList.get(0).getState();
                immediateRunning = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED;
            }

            if (periodicList != null && !periodicList.isEmpty()) {
                WorkInfo.State state = periodicList.get(0).getState();
                periodicRunning = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED;
            }

            return immediateRunning || periodicRunning;
        } catch (Exception e) {
            Log.e("TrackingWorkManager", "Error checking tracking status", e);
            return false;
        }
    }
}