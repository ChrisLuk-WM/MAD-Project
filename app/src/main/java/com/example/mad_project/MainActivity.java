package com.example.mad_project;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mad_project.content_downloader.HikingTrailImageDownloader;
import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.ui.BaseActivity;
import com.example.mad_project.utils.DownloadManager;
import com.example.mad_project.utils.StatisticsCalculator;

import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private AppDatabase database;
    private HikingTrailImageDownloader imageDownloader;
    private StatisticsCalculator statisticsCalculator;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        initCoreComponents();

        // Observe data
        // observeTrailsData();
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

    @Override
    protected boolean showBackButton() {
        return false; // No back button on main screen
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