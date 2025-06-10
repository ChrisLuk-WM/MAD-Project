package com.example.mad_project.services;

import android.content.Context;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

public class TrackingWorkManager {
    private static final String TRACKING_WORK_NAME = "TrackingWork";
    private final WorkManager workManager;
    private final Context context;

    public TrackingWorkManager(Context context) {
        this.context = context;
        this.workManager = WorkManager.getInstance(context);
    }

    public void startTracking() {
        // For expedited work, we can only use NetworkType and Storage constraints
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        // Create a regular work request for the main tracking
        OneTimeWorkRequest trackingRequest = new OneTimeWorkRequest.Builder(TrackingWorker.class)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .build();

        workManager.enqueueUniqueWork(
                TRACKING_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                trackingRequest
        );
    }

    public void stopTracking() {
        // Cancel work and wait for completion
        workManager.cancelUniqueWork(TRACKING_WORK_NAME)
                .getResult()
                .addListener(() -> {
                    // Work has been cancelled
                }, ContextCompat.getMainExecutor(context));
    }

    public boolean isTracking() {
        try {
            ListenableFuture<List<WorkInfo>> workInfos =
                    workManager.getWorkInfosForUniqueWork(TRACKING_WORK_NAME);
            List<WorkInfo> list = workInfos.get();
            if (list != null && !list.isEmpty()) {
                return list.get(0).getState() == WorkInfo.State.RUNNING;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}