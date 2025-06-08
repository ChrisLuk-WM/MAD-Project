package com.example.mad_project;

import static com.example.mad_project.constants.Common.REQUEST_PERMISSION;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mad_project.content_downloader.HikingTrailImageDownloader;
import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.sensors.SensorsController;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCheckRequestPermissions();

        DownloadManager.getInstance(this);

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

        // Initialize database and downloader
        database = AppDatabase.getDatabase(this);
        imageDownloader = new HikingTrailImageDownloader(this);

        // Load data
        imageDownloader.loadTrailsData();

        sensorsController = SensorsController.getInstance(this);
        setupSensors();

        initCoreComponents();

        // Observe data
        // observeTrailsData();
    }

    private void setupSensors() {
//        sensorsController.getTrackingStatistics().observe(this, stats -> {
//            // updateUI(stats);
//        });
//
//        // Observe tracking status
//        sensorsController.getTrackingStatus().observe(this, isTracking -> {
//            // updateTrackingUI(isTracking);
//        });
        sensorsController.startTracking();
    }

    private void onCheckRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ActivityCompat.requestPermissions(this,
                    RequiredPermissions.PERMISSIONS_LIST,
                    REQUEST_PERMISSION);
        }
        requestBatteryOptimizationPermission();
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

    // Core component initialization and signal handling
    private void initCoreComponents() {
        statisticsCalculator = StatisticsCalculator.getInstance(this);
        // Initialize other core components
    }

    private void setupSignalTransmission() {
        // Setup communication between core components
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageDownloader.shutdown();
    }
}