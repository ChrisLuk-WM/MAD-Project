// BaseActivity.java
package com.example.mad_project.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.mad_project.MainActivity;
import com.example.mad_project.ui.pages.profile.MyProfileActivity;
import com.example.mad_project.ui.pages.route.RoutePlanningActivity;
import com.example.mad_project.ui.pages.statistics.StatisticsActivity;
import com.google.android.material.navigation.NavigationView;
import com.example.mad_project.R;

public abstract class BaseActivity extends AppCompatActivity {
    protected Toolbar toolbar;
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected ActionBarDrawerToggle drawerToggle;

    protected boolean useNavigationDrawer() {
        return true; // Default to true for backward compatibility
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

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
        startActivity(new Intent(this, MyProfileActivity.class));
        if (!(this instanceof MyProfileActivity)) {
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}