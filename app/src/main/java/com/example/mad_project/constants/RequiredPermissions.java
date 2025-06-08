package com.example.mad_project.constants;

import android.Manifest;
import android.os.Build;

public class RequiredPermissions {
    // Base permissions (API 31+)
    private static final String[] BASE_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
    };

    // Additional permissions for API 33+ (TIRAMISU)
    private static final String[] TIRAMISU_PERMISSIONS = {
            Manifest.permission.POST_NOTIFICATIONS
    };

    // Additional permissions for API 34+ (UPSIDE_DOWN_CAKE)
    private static final String[] UPSIDE_DOWN_CAKE_PERMISSIONS = {
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION
    };

    public static String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return combinePermissions(BASE_PERMISSIONS,
                    TIRAMISU_PERMISSIONS,
                    UPSIDE_DOWN_CAKE_PERMISSIONS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return combinePermissions(BASE_PERMISSIONS,
                    TIRAMISU_PERMISSIONS);
        } else {
            return BASE_PERMISSIONS;
        }
    }

    private static String[] combinePermissions(String[]... permissionsArrays) {
        int totalLength = 0;
        for (String[] array : permissionsArrays) {
            totalLength += array.length;
        }

        String[] result = new String[totalLength];
        int currentIndex = 0;

        for (String[] array : permissionsArrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }

        return result;
    }

    // Private constructor to prevent instantiation
    private RequiredPermissions() {
        // Empty constructor
    }
}