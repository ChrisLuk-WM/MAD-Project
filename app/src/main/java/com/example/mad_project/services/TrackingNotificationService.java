package com.example.mad_project.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.statistics.StatisticsType;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TrackingNotificationService extends Service {
    private static final String CHANNEL_ID = "tracking_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final long UPDATE_INTERVAL = 1000; // Update every second

    private final Handler handler = new Handler();
    private final StatisticsManager statisticsManager = StatisticsManager.getInstance();

    private NotificationManager notificationManager;
    private boolean isServiceRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "START_TRACKING":
                        startTrackingNotification();
                        break;
                    case "STOP_TRACKING":
                        stopTrackingNotification();
                        break;
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tracking Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows tracking status");
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void startTrackingNotification() {
        isServiceRunning = true;
        startForeground(NOTIFICATION_ID, buildNotification());
        scheduleNotificationUpdates();
    }

    private void stopTrackingNotification() {
        isServiceRunning = false;
        stopForeground(true);
        stopSelf();
    }

    private void updateNotificationIcon(RemoteViews notificationLayout) {
        // Set the animated icon
        Icon walkingIcon;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            walkingIcon = Icon.createWithResource(this, R.drawable.avd_walking);
            notificationLayout.setImageViewIcon(R.id.icon_walking, walkingIcon);
        } else {
            notificationLayout.setImageViewResource(R.id.icon_walking, R.drawable.ic_walking);
        }
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Get statistics
        Double totalDistance = statisticsManager.getValue(StatisticsType.TOTAL_DISTANCE);
        Double altitude = statisticsManager.getValue(StatisticsType.ALTITUDE);
        Double elevationGain = statisticsManager.getValue(StatisticsType.TOTAL_ELEVATION_GAIN);
        long duration = statisticsManager.getSessionDuration();

        // Format values
        String distanceText = String.format(Locale.getDefault(), "%.1f km",
                (totalDistance != null ? totalDistance : 0) / 1000);
        String durationText = formatDuration(duration);
        String altitudeText = String.format(Locale.getDefault(), "%.0f m",
                altitude != null ? altitude : 0);
        String elevationGainText = String.format(Locale.getDefault(), "%.0f m↑",
                elevationGain != null ? elevationGain : 0);

        // Create custom notification layout
        RemoteViews notificationLayout = new RemoteViews(getPackageName(),
                R.layout.notification_tracking);

        // Update icon
        updateNotificationIcon(notificationLayout);

        // Update values
        notificationLayout.setTextViewText(R.id.text_distance, distanceText);
        notificationLayout.setTextViewText(R.id.text_duration, durationText);
        notificationLayout.setTextViewText(R.id.text_altitude, altitudeText);
        notificationLayout.setTextViewText(R.id.text_elevation_gain, elevationGainText);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setCustomContentView(notificationLayout)
                .setSmallIcon(R.drawable.ic_tracking)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void scheduleNotificationUpdates() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isServiceRunning) {
                    notificationManager.notify(NOTIFICATION_ID, buildNotification());
                    handler.postDelayed(this, UPDATE_INTERVAL);
                }
            }
        }, UPDATE_INTERVAL);
    }

    private String formatDuration(long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }
}