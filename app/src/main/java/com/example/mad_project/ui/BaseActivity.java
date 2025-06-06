// BaseActivity.java
package com.example.mad_project.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.mad_project.R;

public abstract class BaseActivity extends AppCompatActivity {
    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        // Setup common toolbar
        toolbar = findViewById(R.id.toolbar_layout); // Changed from R.id.toolbar
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (showBackButton()) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }

        initViews();
        setupActions();
    }

    // Abstract methods that must be implemented by child activities
    protected abstract int getLayoutResourceId();
    protected abstract void initViews();
    protected abstract void setupActions();

    // Optional method for back button
    protected boolean showBackButton() {
        return true; // Default true, override to change
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_common, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        // Handle common menu items
//        switch (id) {
//            case R.id.action_settings:
//                openSettings();
//                return true;
//            case R.id.action_profile:
//                openProfile();
//                return true;
//            // Add more common menu items
//        }

        return super.onOptionsItemSelected(item);
    }

    // Common navigation methods
    protected void openSettings() {
        // Intent to Settings Activity
    }

    protected void openProfile() {
        // Intent to Profile Activity
    }
}