package com.example.mad_project.ui.pages.sessions;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.mad_project.R;
import com.example.mad_project.ui.BaseActivity;

public class SessionActivity extends BaseActivity {
    private MapFragment mapFragment;
    private TextView textDuration;
    private TextView textDistance;
    private TextView textSpeed;
    private TextView textAvgSpeed;
    private TextView textElevation;
    private TextView textElevationGain;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_session;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_container, MapFragment.class, null)
                    .commit();
        }
    }

    @Override
    protected void initViews() {
        textDuration = findViewById(R.id.text_duration);
        textDistance = findViewById(R.id.text_distance);
        textSpeed = findViewById(R.id.text_speed);
        textAvgSpeed = findViewById(R.id.text_avg_speed);
        textElevation = findViewById(R.id.text_elevation);
        textElevationGain = findViewById(R.id.text_elevation_gain);
    }

    @Override
    protected void setupActions() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_history) {
                startActivity(new Intent(this, SessionHistoryActivity.class));
                return true;
            }
            return false;
        });
    }
}