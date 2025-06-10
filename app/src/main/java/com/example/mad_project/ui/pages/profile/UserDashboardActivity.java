package com.example.mad_project.ui.pages.profile;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.example.mad_project.R;
import com.example.mad_project.ui.BaseActivity;

public class UserDashboardActivity extends BaseActivity {
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_user_dashboard;
    }

    @Override
    protected void initViews() {
        // Initialize views and set dummy data
        // This will be replaced with real data later
    }

    @Override
    protected void setupActions() {
        // Setup any click listeners or other actions
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit_profile) {
            // Navigate to profile settings
            startActivity(new Intent(this, MyProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}