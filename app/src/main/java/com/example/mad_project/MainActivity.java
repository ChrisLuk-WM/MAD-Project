package com.example.mad_project;

import static com.example.mad_project.constants.Common.REQUEST_PERMISSION;
import static com.example.mad_project.constants.Common.REQUEST_BACKGROUND_PERMISSION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.example.mad_project.content_downloader.HikingTrailImageDownloader;
import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.sensors.SensorsController;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.ui.BaseActivity;
import com.example.mad_project.utils.DownloadManager;
import com.example.mad_project.constants.RequiredPermissions;
import com.example.mad_project.utils.ProfileManager;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private HikingTrailImageDownloader imageDownloader;
    private NavController navController;
    private SensorsController sensorsController;
    private boolean coreComponentsInitialized = false;


    // Core component initialization and signal handling
    private void initCoreComponents() {
        try {
            // Initialize database and downloader
            AppDatabase.getDatabase(this);
            imageDownloader = new HikingTrailImageDownloader(this);

            DownloadManager.getInstance(this);

            // Make sure StatisticsManager is initialized before SensorsController
            if (!StatisticsManager.isInitialized()) {
                StatisticsManager.init(this);
            }

            sensorsController = SensorsController.getInstance(this);
            coreComponentsInitialized = true;

            // Check if we need to start tracking immediately
            if (StatisticsManager.getInstance().isSessionActive()) {
                sensorsController.startTracking();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing core components", e);
            showInitializationErrorDialog();
        }
    }

    private void showInitializationErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Initialization Error")
                .setMessage("Failed to initialize app components. Please restart the app.")
                .setPositiveButton("Restart", (dialog, which) -> {
                    // Restart the app
                    Intent intent = getPackageManager()
                            .getLaunchIntentForPackage(getPackageName());
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    finish();
                })
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize StatisticsManager first
        StatisticsManager.init(getApplicationContext());

        // Initialize SensorsController early
        try {
            sensorsController = SensorsController.getInstance(this);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize SensorsController", e);
        }

        // Check permissions and initialize other components
        if (checkAllPermissionsGranted()) {
            initCoreComponents();
        } else {
            onCheckRequestPermissions();
        }

        Toolbar toolbar = findViewById(R.id.toolbar_layout);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration
                    .Builder(navController.getGraph())
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }
    }

    private boolean checkAllPermissionsGranted() {
        for (String permission : RequiredPermissions.getRequiredPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private void onCheckRequestPermissions() {

        ActivityCompat.requestPermissions(this,
                RequiredPermissions.getRequiredPermissions(),
                REQUEST_PERMISSION);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setTitle("Background Location Access")
                    .setMessage("This app needs to access location in the background to track your hikes.")
                    .setPositiveButton("Grant", (dialog, which) -> {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                REQUEST_BACKGROUND_PERMISSION);
                    })
                    .setNegativeButton("Deny", null)
                    .show();
        }

        requestBatteryOptimizationPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                showPermissionDeniedDialog();
            } else {
                initCoreComponents();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage("This app requires certain permissions to function properly. " +
                        "Please grant all requested permissions.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void requestBatteryOptimizationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        // Initialize core components only
    }

    @Override
    protected void setupActions() {
        // Setup core component interactions
    }

    private void setupSignalTransmission() {
        // Setup communication between core components
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only try to start tracking if core components are initialized
        if (coreComponentsInitialized &&
                StatisticsManager.getInstance().isSessionActive() &&
                sensorsController != null) {
            try {
                sensorsController.startTracking();
            } catch (Exception e) {
                Log.e(TAG, "Error starting tracking in onResume", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageDownloader.shutdown();
    }

}