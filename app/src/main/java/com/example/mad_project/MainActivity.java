package com.example.mad_project;

import static com.example.mad_project.constants.Common.REQUEST_PERMISSION;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mad_project.content_downloader.HikingTrailImageDownloader;
import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.sensors.SensorsController;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.ui.BaseActivity;
import com.example.mad_project.utils.DownloadManager;
import com.example.mad_project.statistics.StatisticsCalculator;
import com.example.mad_project.constants.RequiredPermissions;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private AppDatabase database;
    private HikingTrailImageDownloader imageDownloader;
    private StatisticsCalculator statisticsCalculator;
    private NavController navController;
    private SensorsController sensorsController;
    private StatisticsManager statisticsManager;


    // Core component initialization and signal handling
    private void initCoreComponents() {
        // Initialize database and downloader
        database = AppDatabase.getDatabase(this);
        imageDownloader = new HikingTrailImageDownloader(this);

        DownloadManager.getInstance(this);
        statisticsCalculator = StatisticsCalculator.getInstance(this);
        statisticsManager = StatisticsManager.getInstance();
        sensorsController = SensorsController.getInstance(this);

        // Load data
        imageDownloader.loadTrailsData();

        startTracking();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCheckRequestPermissions();

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

    private void startTracking() {
        if (statisticsManager.isSessionActive()) return;
        sensorsController.startTracking();
        statisticsCalculator.startSession();
    }

    private void onCheckRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ActivityCompat.requestPermissions(this,
                    RequiredPermissions.getRequiredPermissions(),
                    REQUEST_PERMISSION);
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
                // Handle permission denial
                showPermissionDeniedDialog();
            } else {
                // All permissions granted, proceed with initialization
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
        if (statisticsManager != null && statisticsManager.isSessionActive()) {
            // Ensure services are running
            sensorsController.startTracking();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageDownloader.shutdown();
    }
}