package com.example.mad_project.ui.pages.sessions;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.mad_project.R;
import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.database.entities.HikingStatisticsEntity;
import com.example.mad_project.ui.BaseActivity;
import com.example.mad_project.ui.pages.sessions.fragments.ElevationGraphFragment;
import com.example.mad_project.ui.pages.sessions.fragments.MapFragment;
import com.example.mad_project.ui.pages.sessions.fragments.SharedSessionViewModel;
import com.example.mad_project.ui.pages.sessions.fragments.SpeedGraphFragment;
import com.example.mad_project.ui.pages.sessions.fragments.StepsStatisticsFragment;

import org.osmdroid.util.GeoPoint;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SessionAnalysisActivity extends BaseActivity {
    private static final String TAG = "SessionAnalysisActivity";
    private SharedSessionViewModel sharedViewModel;
    private SessionViewModel viewModel;
    private long sessionId;
    private MapFragment mapFragment;

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
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionId = getIntent().getLongExtra("session_id", -1);
        if (sessionId == -1) {
            finish();
            return;
        }

        // Initialize ViewModels
        viewModel = new ViewModelProvider(this).get(SessionViewModel.class);
        sharedViewModel = new ViewModelProvider(this,
                new SharedSessionViewModel.Factory(viewModel))
                .get(SharedSessionViewModel.class);

        // Initialize shared session data BEFORE creating fragments
        sharedViewModel.initializeSession(sessionId, false);

        // Setup action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        setupFragments(savedInstanceState);

        loadSessionData();
    }

    private void setupFragments(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            // Create fragments with arguments
            Bundle args = new Bundle();
            args.putLong("sessionId", sessionId);
            args.putBoolean("isRealTime", false);

            // Map Fragment
            mapFragment = new MapFragment();
            mapFragment.setArguments(args);

            // Statistics Fragments
            SpeedGraphFragment speedFragment = new SpeedGraphFragment();
            speedFragment.setArguments(new Bundle(args));

            ElevationGraphFragment elevationFragment = new ElevationGraphFragment();
            elevationFragment.setArguments(new Bundle(args));

            StepsStatisticsFragment stepsFragment = new StepsStatisticsFragment();
            stepsFragment.setArguments(new Bundle(args));

            // Add fragments
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.map_container, mapFragment)
                    .add(R.id.speed_graph_container, speedFragment)
                    .add(R.id.elevation_graph_container, elevationFragment)
                    .add(R.id.steps_stats_container, stepsFragment)
                    .commitNow();

            // Configure map fragment
            mapFragment.setRealTimeTracking(false);
        } else {
            // Restore existing fragments
            mapFragment = (MapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map_container);
            if (mapFragment != null) {
                mapFragment.setRealTimeTracking(false);
            }
        }
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("sessionId", sessionId);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        sessionId = savedInstanceState.getLong("sessionId", -1);
    }

    private void loadSessionData() {
        viewModel.getSession(sessionId).observe(this, session -> {
            if (session != null) {
                updateUI(session);

                // Load path data for the map
                viewModel.getSessionStatistics(sessionId).observe(this, statistics -> {
                    if (statistics != null && !statistics.isEmpty() && mapFragment != null) {
                        List<GeoPoint> pathPoints = new ArrayList<>();
                        for (HikingStatisticsEntity stat : statistics) {
                            pathPoints.add(new GeoPoint(stat.getLatitude(), stat.getLongitude()));
                        }
                        mapFragment.drawPath(pathPoints);
                    }
                });
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
                finish();
            } else {
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