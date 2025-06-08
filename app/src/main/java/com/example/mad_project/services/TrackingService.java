package com.example.mad_project.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.example.mad_project.MainActivity;
import com.example.mad_project.R;
import com.example.mad_project.constants.ServiceConstants;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.statistics.StatisticsType;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TrackingService extends Service {
    private static final String CHANNEL_ID = "tracking_service_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final long UPDATE_INTERVAL = 1000; // 1 second
    private static final long KEEP_ALIVE_INTERVAL = 15000; // 15 seconds

    private final IBinder binder = new LocalBinder();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private PowerManager.WakeLock wakeLock;
    private AlarmManager alarmManager;
    private NotificationManager notificationManager;
    private final StatisticsManager statisticsManager = StatisticsManager.getInstance();
    private boolean isRunning = false;

    private final BroadcastReceiver keepAliveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ServiceConstants.ACTION_KEEP_ALIVE.equals(intent.getAction())) {
                ensureServiceRunning();
            }
        }
    };

    public class LocalBinder extends Binder {
        public TrackingService getService() {
            return TrackingService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeService();
        registerKeepAliveReceiver();
        scheduleKeepAlive();
    }

    private void initializeService() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        createNotificationChannel();
        initializeWakeLock();
        startBackgroundThread();
    }

    private void initializeWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "TrackingService::WakeLock"
        );
        wakeLock.setReferenceCounted(false);
    }

    private void registerKeepAliveReceiver() {
        IntentFilter filter = new IntentFilter(ServiceConstants.ACTION_KEEP_ALIVE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(keepAliveReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(keepAliveReceiver, filter);
        }
    }

    private void scheduleKeepAlive() {
        Intent intent = new Intent(ServiceConstants.ACTION_KEEP_ALIVE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + KEEP_ALIVE_INTERVAL,
                KEEP_ALIVE_INTERVAL,
                pendingIntent
        );
    }

    private void ensureServiceRunning() {
        if (isRunning && !wakeLock.isHeld()) {
            startTracking();
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread(
                "TrackingServiceThread",
                android.os.Process.THREAD_PRIORITY_BACKGROUND
        );
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;

        if (ServiceConstants.ACTION_START_TRACKING.equals(action)) {
            startTracking();
        } else if (ServiceConstants.ACTION_STOP_TRACKING.equals(action)) {
            stopTracking();
        }

        return START_STICKY;
    }

    private void startTracking() {
        if (!isRunning) {
            isRunning = true;
            acquireWakeLock();
            startForeground(NOTIFICATION_ID, buildNotification());
            scheduleUpdates();
        } else {
            // Ensure notification is showing even if already running
            updateNotification();
        }
    }

    private void stopTracking() {
        isRunning = false;
        releaseWakeLock();
        stopForeground(true);
        stopSelf();
    }

    private void acquireWakeLock() {
        if (!wakeLock.isHeld()) {
            wakeLock.acquire(24*60*60*1000L /*24 hours*/);
        }
    }

    private void releaseWakeLock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void scheduleUpdates() {
        backgroundHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;

                mainHandler.post(() -> {
                    updateNotification();
                    ensureServiceRunning();
                });

                backgroundHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        }, UPDATE_INTERVAL);
    }

    private void updateNotification() {
        if (isRunning) {
            Notification notification = buildNotification();
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
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

        RemoteViews notificationLayout = new RemoteViews(getPackageName(),
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

        return new NotificationCompat.Builder(this, CHANNEL_ID)
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
                    android.graphics.drawable.Icon.createWithResource(this, R.drawable.avd_walking));
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

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(keepAliveReceiver);
        } catch (Exception e) {
            Log.e("TrackingService", "Error unregistering receiver", e);
        }

        if (isRunning) {
            // Try to restart service if it was running
            Intent restartIntent = new Intent(this, TrackingService.class);
            restartIntent.setAction(ServiceConstants.ACTION_START_TRACKING);
            startService(restartIntent);
        }

        releaseWakeLock();
        if (backgroundThread != null) {
            backgroundThread.quit();
        }
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (isRunning) {
            // Schedule service restart
            Intent restartIntent = new Intent(this, TrackingService.class);
            restartIntent.setAction(ServiceConstants.ACTION_START_TRACKING);
            PendingIntent pendingIntent = PendingIntent.getService(
                    this,
                    1,
                    restartIntent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.set(
                    AlarmManager.RTC,
                    System.currentTimeMillis() + 1000,
                    pendingIntent
            );
        }
    }

    public static class KeepAliveReceiver extends BroadcastReceiver {
        private final WeakReference<TrackingService> serviceReference;

        public KeepAliveReceiver(){
            serviceReference = null;
        }
        public KeepAliveReceiver(TrackingService service) {
            this.serviceReference = new WeakReference<>(service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            TrackingService service = serviceReference.get();
            if (service != null && ServiceConstants.ACTION_KEEP_ALIVE.equals(intent.getAction())) {
                service.ensureServiceRunning();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}