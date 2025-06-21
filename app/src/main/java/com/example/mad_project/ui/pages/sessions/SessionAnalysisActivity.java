package com.example.mad_project.ui.pages.sessions;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.mad_project.R;
import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.ui.BaseActivity;
import com.example.mad_project.ui.pages.sessions.fragments.ElevationGraphFragment;
import com.example.mad_project.ui.pages.sessions.fragments.MapFragment;
import com.example.mad_project.ui.pages.sessions.fragments.SpeedGraphFragment;
import com.example.mad_project.ui.pages.sessions.fragments.StepsStatisticsFragment;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SessionAnalysisActivity extends BaseActivity {
    private SessionViewModel viewModel;
    private long sessionId;

    // UI elements
    private TextView startTimeText;
    private TextView endTimeText;
    private TextView durationText;
    private TextView distanceText;
    private TextView avgSpeedText;
    private TextView stepsText;
    private TextView elevationText;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_session_analysis;
    }

    @Override
    protected boolean useNavigationDrawer() {
        return false; // Disable navigation drawer for this activity
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionId = getIntent().getLongExtra("session_id", -1);
        String source = getIntent().getStringExtra("source");

        if (sessionId == -1) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(SessionViewModel.class);
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (savedInstanceState == null) {
            // Add Map Fragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_container, createMapFragment())
                    .replace(R.id.speed_graph_container, createSpeedFragment(false))
                    .replace(R.id.elevation_graph_container, createElevationFragment(false))
                    .replace(R.id.steps_stats_container, createStepsFragment(false))
                    .commit();
        }

        loadSessionData();
    }

    private Fragment createMapFragment() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putLong("sessionId", sessionId);
        fragment.setArguments(args);
        return fragment;
    }

    private Fragment createSpeedFragment(boolean isRealTime) {
        SpeedGraphFragment fragment = new SpeedGraphFragment();
        Bundle args = new Bundle();
        args.putBoolean("isRealTime", isRealTime);
        args.putLong("sessionId", sessionId);
        fragment.setArguments(args);
        return fragment;
    }

    private Fragment createElevationFragment(boolean isRealTime) {
        ElevationGraphFragment fragment = new ElevationGraphFragment();
        Bundle args = new Bundle();
        args.putBoolean("isRealTime", isRealTime);
        args.putLong("sessionId", sessionId);
        fragment.setArguments(args);
        return fragment;
    }

    private Fragment createStepsFragment(boolean isRealTime) {
        StepsStatisticsFragment fragment = new StepsStatisticsFragment();
        Bundle args = new Bundle();
        args.putBoolean("isRealTime", isRealTime);
        args.putLong("sessionId", sessionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initViews() {
        startTimeText = findViewById(R.id.text_start_time);
        endTimeText = findViewById(R.id.text_end_time);
        durationText = findViewById(R.id.text_duration);
        distanceText = findViewById(R.id.text_distance);
        avgSpeedText = findViewById(R.id.text_avg_speed);
        stepsText = findViewById(R.id.text_steps);
        elevationText = findViewById(R.id.text_elevation);
    }

    private void loadSessionData() {
        viewModel.getSession(sessionId).observe(this, session -> {
            if (session != null) {
                updateUI(session);
            }
        });
    }

    private void updateUI(HikingSessionEntity session) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");

        startTimeText.setText("Started: " + session.getStartTime().format(formatter));
        endTimeText.setText("Ended: " + session.getEndTime().format(formatter));

        long durationMillis = session.getDuration();
        String duration = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(durationMillis),
                TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60,
                TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60);
        durationText.setText("Duration: " + duration);

        distanceText.setText(String.format(Locale.getDefault(), "%.2f km", session.getDistance() / 1000));
        avgSpeedText.setText(String.format(Locale.getDefault(), "%.1f km/h", session.getAverageSpeed() * 3.6));
        stepsText.setText(String.valueOf(session.getSteps()));
        elevationText.setText(String.format(Locale.getDefault(), "%d m", session.getTotalElevationGain()));
    }

    @Override
    protected void setupActions() {
        // No additional actions needed
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            String source = getIntent().getStringExtra("source");
            if ("history".equals(source)) {
                // Go back to history
                finish();
            } else {
                // Go to session activity
                Intent intent = new Intent(this, SessionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}