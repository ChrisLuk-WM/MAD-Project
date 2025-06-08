package com.example.mad_project.constants;

import android.Manifest;

public class RequiredPermissions {
    public static final String[] PERMISSIONS_LIST = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
    };

    // Private constructor to prevent instantiation
    private RequiredPermissions() {
        // Empty constructor
    }
}