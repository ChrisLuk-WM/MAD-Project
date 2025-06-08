package com.example.mad_project.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;

public class TrackingBackgroundService extends Service {
    private static TrackingBackgroundService instance;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final IBinder binder = new LocalBinder();
    private boolean isRunning = false;

    public class LocalBinder extends Binder {
        TrackingBackgroundService getService() {
            return TrackingBackgroundService.this;
        }
    }

    public static TrackingBackgroundService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        startBackgroundThread();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void startBackgroundThread() {
        HandlerThread handlerThread = new HandlerThread(
                "TrackingBackgroundThread",
                Process.THREAD_PRIORITY_BACKGROUND
        );
        handlerThread.start();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}