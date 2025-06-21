package com.example.mad_project.ui.pages.sessions;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.ui.BaseActivity;
import com.example.mad_project.ui.pages.sessions.fragments.MapFragment;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SessionDetailsActivity extends BaseActivity {
    private static final String TAG = "SessionDetailsActivity";
    private MapFragment mapFragment;
    private long sessionId;
    private SessionViewModel viewModel;

    // UI Elements
    private TextView startTimeText;
    private TextView endTimeText;
    private TextView durationText;
    private TextView distanceText;
    private TextView avgSpeedText;
    private TextView stepsText;
    private TextView elevationGainText;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_session_details;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionId = getIntent().getLongExtra("session_id", -1);
        if (sessionId == -1) {
            Log.e(TAG, "No session ID provided");
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(SessionViewModel.class);

        if (savedInstanceState == null) {
            mapFragment = new MapFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();
        }

        observeSessionData();
    }

    @Override
    protected void initViews() {
        startTimeText = findViewById(R.id.text_start_time);
        endTimeText = findViewById(R.id.text_end_time);
        durationText = findViewById(R.id.text_duration);
        distanceText = findViewById(R.id.text_distance);
        avgSpeedText = findViewById(R.id.text_avg_speed);
        stepsText = findViewById(R.id.text_steps);
        elevationGainText = findViewById(R.id.text_elevation_gain);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void observeSessionData() {
        viewModel.getSession(sessionId).observe(this, session -> {
            if (session != null) {
                updateUI(session);
            }
        });
    }

    private void updateUI(HikingSessionEntity session) {
        // Format dates
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");

        // Update time information
        startTimeText.setText("Started: " + session.getStartTime().format(dateFormatter));
        if (session.getEndTime() != null) {
            endTimeText.setText("Ended: " + session.getEndTime().format(dateFormatter));
        } else {
            endTimeText.setText("Ongoing");
        }

        // Update duration
        long durationMillis = session.getDuration();
        String duration = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(durationMillis),
                TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60,
                TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60);
        durationText.setText("Duration: " + duration);

        // Update statistics
        distanceText.setText(String.format(Locale.getDefault(), "%.2f km",
                session.getDistance() / 1000));
        avgSpeedText.setText(String.format(Locale.getDefault(), "%.1f km/h",
                session.getAverageSpeed() * 3.6));
        stepsText.setText(String.valueOf(session.getSteps()));
        elevationGainText.setText(String.format(Locale.getDefault(), "%d m",
                session.getTotalElevationGain()));

        // Update map if available
        if (mapFragment != null) {
            // Will implement path drawing later
        }
    }

    @Override
    protected void setupActions() {
        // Handle any additional actions
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}