package com.example.mad_project.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.sensors.SensorsController;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.statistics.StatisticsType;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TrackingWorker extends Worker {
    private static final String CHANNEL_ID = "tracking_service_channel";
    private static final int NOTIFICATION_ID = 1;
    private volatile boolean isRunning = true;
    private final Handler mainHandler;

    private final Context context;
    private final NotificationManager notificationManager;
    private SensorsController sensorsController;
    private StatisticsManager statisticsManager;

    public TrackingWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.mainHandler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }


    private void initializeManagers() {
        if (!StatisticsManager.isInitialized()) {
            StatisticsManager.init(context);
        }
        statisticsManager = StatisticsManager.getInstance();
        sensorsController = SensorsController.getInstance(context);
    }




    @NonNull
    @Override
    public Result doWork() {
        android.util.Log.d("TrackingWorker", "Starting work execution");

        try {
            // Initialize managers first
            initializeManagers();

            // Set as foreground service immediately
            ForegroundInfo foregroundInfo = createForegroundInfo();
            setForegroundAsync(foregroundInfo).get();

            // Start tracking on main thread
            mainHandler.post(() -> {
                try {
                    if (sensorsController != null) {
                        sensorsController.startTracking();
                        android.util.Log.d("TrackingWorker", "Sensor tracking started");
                    }
                } catch (Exception e) {
                    android.util.Log.e("TrackingWorker", "Error starting sensors", e);
                }
            });

            // Keep the worker alive
            while (isRunning && !isStopped()) {
                try {
                    // Update notification
                    setForegroundAsync(createForegroundInfo()).get();

                    // Log status periodically
                    android.util.Log.d("TrackingWorker", "Worker still running...");

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    android.util.Log.e("TrackingWorker", "Worker interrupted", e);
                    isRunning = false;
                }
            }

            return Result.success();
        } catch (Exception e) {
            android.util.Log.e("TrackingWorker", "Worker failed", e);
            return Result.retry();
        }
    }

    @NonNull
    private ForegroundInfo createForegroundInfo() {
        Notification notification = buildNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new ForegroundInfo(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            );
        }

        return new ForegroundInfo(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tracking Service",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Shows tracking status");
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Get statistics
        Double totalDistance = statisticsManager.getValue(StatisticsType.TOTAL_DISTANCE);
        Double altitude = statisticsManager.getValue(StatisticsType.ALTITUDE);
        Double elevationGain = statisticsManager.getValue(StatisticsType.TOTAL_ELEVATION_GAIN);
        long duration = statisticsManager.getSessionDuration();

        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(),
                R.layout.notification_tracking);

        updateNotificationIcon(notificationLayout);

        notificationLayout.setTextViewText(R.id.text_distance,
                String.format(Locale.getDefault(), "%.1f km",
                        (totalDistance != null ? totalDistance : 0) / 1000));
        notificationLayout.setTextViewText(R.id.text_duration,
                formatDuration(duration));
        notificationLayout.setTextViewText(R.id.text_altitude,
                String.format(Locale.getDefault(), "%.0f m",
                        altitude != null ? altitude : 0));
        notificationLayout.setTextViewText(R.id.text_elevation_gain,
                String.format(Locale.getDefault(), "%.0f m↑",
                        elevationGain != null ? elevationGain : 0));

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setCustomContentView(notificationLayout)
                .setSmallIcon(R.drawable.ic_tracking)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build();
    }

    private void updateNotificationIcon(RemoteViews notificationLayout) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationLayout.setImageViewIcon(R.id.icon_walking,
                    android.graphics.drawable.Icon.createWithResource(context, R.drawable.avd_walking));
        } else {
            notificationLayout.setImageViewResource(R.id.icon_walking, R.drawable.ic_walking);
        }
    }

    private String formatDuration(long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;

        return hours > 0
                ? String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
                : String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void updateNotification() {
        setForegroundAsync(createForegroundInfo());
    }

    @Override
    public void onStopped() {
        android.util.Log.d("TrackingWorker", "Worker stopped");
        isRunning = false;

        mainHandler.post(() -> {
            try {
                if (sensorsController != null) {
                    sensorsController.stopTracking();
                    android.util.Log.d("TrackingWorker", "Sensors stopped");
                }
            } catch (Exception e) {
                android.util.Log.e("TrackingWorker", "Error stopping sensors", e);
            }
        });

        super.onStopped();
    }
}