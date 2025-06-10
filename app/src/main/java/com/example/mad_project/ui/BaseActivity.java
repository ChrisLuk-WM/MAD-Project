// BaseActivity.java
package com.example.mad_project.ui;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.mad_project.MainActivity;
import com.example.mad_project.constants.DownloadError;
import com.example.mad_project.content_downloader.DownloadStateListener;
import com.example.mad_project.content_downloader.HikingTrailImageDownloader;
import com.example.mad_project.ui.pages.profile.MyProfileActivity;
import com.example.mad_project.ui.pages.profile.UserDashboardActivity;
import com.example.mad_project.ui.pages.route.RouteDetailsActivity;
import com.example.mad_project.ui.pages.route.RoutePlanningActivity;
import com.example.mad_project.ui.pages.statistics.StatisticsActivity;
import com.example.mad_project.utils.DownloadManager;
import com.google.android.material.navigation.NavigationView;
import com.example.mad_project.R;

import java.io.File;

public abstract class BaseActivity extends AppCompatActivity implements DownloadStateListener {
    protected Toolbar toolbar;
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected ActionBarDrawerToggle drawerToggle;
    protected MenuItem downloadMenuItem;
    protected HikingTrailImageDownloader imageDownloader;
    private AnimatedVectorDrawable downloadingAnimation;
    protected boolean useNavigationDrawer() {
        return true; // Default to true for backward compatibility
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        imageDownloader = new HikingTrailImageDownloader(this);
        HikingTrailImageDownloader.addDownloadStateListener(this);
        downloadingAnimation = (AnimatedVectorDrawable)
                getDrawable(R.drawable.animated_downloading).mutate();

        // Initialize toolbar first
        toolbar = findViewById(R.id.toolbar_layout);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Only setup drawer if needed
        if (useNavigationDrawer()) {
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);

            if (drawerLayout != null && navigationView != null) {
                setupDrawer();
                setupNavigationView();
            }
        } else if (getSupportActionBar() != null) {
            // If not using drawer, show back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initViews();
        setupActions();
    }

    @Override
    public void onDownloadStateChanged(boolean isDownloading) {
        // Run on UI thread since this might be called from a background thread
        runOnUiThread(() -> {
            if (downloadMenuItem == null) return;

            if (isDownloading) {
                try {
                    if (downloadingAnimation.isRunning()) {
                        return;
                    }
                    downloadMenuItem.setIcon(downloadingAnimation);
                    downloadingAnimation.start();
                } catch (Exception e) {
                    downloadMenuItem.setIcon(R.drawable.ic_download);
                }
            } else {
                if (downloadingAnimation != null && downloadingAnimation.isRunning()) {
                    downloadingAnimation.stop();
                }
                downloadMenuItem.setIcon(R.drawable.ic_download);
            }
        });
    }

    public void hideMainToolbar() {
        if (toolbar != null) {
            toolbar.setVisibility(View.GONE);
        }
    }

    // Add this method to BaseActivity
    public void showMainToolbar() {
        if (toolbar != null) {
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    private void setupDrawer() {
        // Create drawer toggle
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Handle navigation
            if (id == R.id.nav_home) {
                navigateToHome();
            } else if (id == R.id.nav_profile) {
                navigateToProfile();
            } else if (id == R.id.nav_route) {
                navigateToRoute();
            } else if (id == R.id.nav_statistics) {
                navigateToStatistics();
            } else if (id == R.id.nav_settings) {
                openSettings();
            } else if (id == R.id.nav_about) {
                openAbout();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // Navigation methods that can be overridden by child activities
    protected void navigateToHome() {
        // Default implementation using explicit intent
        finish();
        // If current activity is not MainActivity, start MainActivity
        if (!(this instanceof MainActivity)) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    protected void navigateToProfile() {
        startActivity(new Intent(this, UserDashboardActivity.class));
        if (!(this instanceof UserDashboardActivity)) {
            finish();
        }
    }

    protected void navigateToRoute() {
        startActivity(new Intent(this, RoutePlanningActivity.class));
        if (!(this instanceof RoutePlanningActivity)) {
            finish();
        }
    }

    protected void navigateToStatistics() {
        startActivity(new Intent(this, StatisticsActivity.class));
        if (!(this instanceof StatisticsActivity)) {
            finish();
        }
    }

    protected void openSettings() {
        // Implement settings navigation
    }

    protected void openAbout() {
        // Implement about navigation
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Abstract methods that must be implemented by child activities
    protected abstract int getLayoutResourceId();
    protected abstract void initViews();
    protected abstract void setupActions();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_common, menu);
        downloadMenuItem = menu.findItem(R.id.action_download);

        // Only show download button in Route activities
        if (this instanceof RoutePlanningActivity ||
                this instanceof RouteDetailsActivity) {
            downloadMenuItem.setVisible(true);
        } else {
            downloadMenuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_download) {
            handleDownloadAction();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void handleDownloadAction() {
        if (imageDownloader.isDownloading()) {
            imageDownloader.pauseDownloads();
        } else {
            checkAndStartDownload();
        }
    }

    protected void checkAndStartDownload() {
        new AlertDialog.Builder(this)
                .setTitle("Download Content")
                .setMessage("Do you want to download hiking trail content?")
                .setPositiveButton("Download", (dialog, which) -> {
                    startDownload();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    protected void startDownload() {
        imageDownloader.loadTrailsData();
    }

    protected void updateDownloadProgress(int progress) {
        // Optional: Show download progress in toolbar
        // Could implement a custom view in toolbar to show progress
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloadingAnimation != null && downloadingAnimation.isRunning()) {
            downloadingAnimation.stop();
        }
    }
}