package com.example.mad_project.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.mad_project.constants.ServiceConstants;

// Remove the inner KeepAliveReceiver class from TrackingService and create it as a standalone class:
public class KeepAliveReceiver extends BroadcastReceiver {
    private static final String TAG = "KeepAliveReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ServiceConstants.ACTION_KEEP_ALIVE.equals(intent.getAction())) {
            // Start or ensure service is running
            Intent serviceIntent = new Intent(context, TrackingService.class);
            serviceIntent.setAction(ServiceConstants.ACTION_START_TRACKING);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}