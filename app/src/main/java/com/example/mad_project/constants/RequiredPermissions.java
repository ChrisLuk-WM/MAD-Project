package com.example.mad_project.constants;

import android.Manifest;
import android.os.Build;

public class RequiredPermissions {
    private static final String[] BASE_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
    };

    private static final String[] TIRAMISU_PERMISSIONS = {
            Manifest.permission.POST_NOTIFICATIONS
    };

    private static final String[] UPSIDE_DOWN_CAKE_PERMISSIONS = {
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION
    };

    private static final String[] ANDROID_Q_PERMISSIONS = {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    private static final String[] ANDROID_15_PERMISSIONS = {
            Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE,
            Manifest.permission.USE_EXACT_ALARM,
    };

    public static String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return combinePermissions(
                    BASE_PERMISSIONS,
                    TIRAMISU_PERMISSIONS,
                    UPSIDE_DOWN_CAKE_PERMISSIONS,
                    ANDROID_15_PERMISSIONS,
                    ANDROID_Q_PERMISSIONS
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return combinePermissions(
                    BASE_PERMISSIONS,
                    TIRAMISU_PERMISSIONS,
                    UPSIDE_DOWN_CAKE_PERMISSIONS
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return combinePermissions(
                    BASE_PERMISSIONS,
                    TIRAMISU_PERMISSIONS
            );
        }
        return BASE_PERMISSIONS;
    }

    private static String[] combinePermissions(String[]... arrays) {
        int totalLength = 0;
        for (String[] array : arrays) {
            totalLength += array.length;
        }

        String[] result = new String[totalLength];
        int currentIndex = 0;

        for (String[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }

        return result;
    }
}