package com.example.mad_project.ui.pages.sessions;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.database.entities.HikingStatisticsEntity;
import com.example.mad_project.sensors.SensorsController;
import com.example.mad_project.statistics.StatisticsCalculator;
import com.example.mad_project.ui.BaseActivity;
import com.example.mad_project.ui.pages.sessions.fragments.BaseStatisticsFragment;
import com.example.mad_project.ui.pages.sessions.fragments.ElevationGraphFragment;
import com.example.mad_project.ui.pages.sessions.fragments.MapFragment;
import com.example.mad_project.ui.pages.sessions.fragments.SharedSessionViewModel;
import com.example.mad_project.ui.pages.sessions.fragments.SpeedGraphFragment;
import com.example.mad_project.ui.pages.sessions.fragments.StepsStatisticsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.util.GeoPoint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionActivity extends BaseActivity implements PlannedSessionAdapter.OnSessionActionListener {
    private MapFragment mapFragment;
    private SessionViewModel viewModel;
    private View activeSessionView;
    private View plannedSessionsView;
    private FloatingActionButton fabEndSession;
    private RecyclerView plannedSessionsRecycler;
    private PlannedSessionAdapter plannedSessionsAdapter;
    private TextView emptyView;
    private SharedSessionViewModel sharedViewModel;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_session;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            viewModel = new ViewModelProvider(this).get(SessionViewModel.class);
            // Initialize SharedSessionViewModel
            sharedViewModel = new ViewModelProvider(this,
                    new SharedSessionViewModel.Factory(viewModel))
                    .get(SharedSessionViewModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("SessionActivity", e.getMessage());
        }

        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // Add Map Fragment
            mapFragment = new MapFragment();
            mapFragment.setRealTimeTracking(true);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();

            // Add Statistics Fragments for active session
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.speed_graph_container, createSpeedFragment(true))
                    .replace(R.id.elevation_graph_container, createElevationFragment(true))
                    .replace(R.id.steps_stats_container, createStepsFragment(true))
                    .commit();
        }

        observeSessionData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu); // Call super to handle common menu items
        getMenuInflater().inflate(R.menu.menu_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_history) {
            openSessionHistory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Fragment createSpeedFragment(boolean isRealTime) {
        SpeedGraphFragment fragment = new SpeedGraphFragment();
        Bundle args = new Bundle();
        args.putBoolean("isRealTime", isRealTime);
        fragment.setArguments(args);
        return fragment;
    }

    private Fragment createElevationFragment(boolean isRealTime) {
        ElevationGraphFragment fragment = new ElevationGraphFragment();
        Bundle args = new Bundle();
        args.putBoolean("isRealTime", isRealTime);
        fragment.setArguments(args);
        return fragment;
    }

    private Fragment createStepsFragment(boolean isRealTime) {
        StepsStatisticsFragment fragment = new StepsStatisticsFragment();
        Bundle args = new Bundle();
        args.putBoolean("isRealTime", isRealTime);
        fragment.setArguments(args);
        return fragment;
    }

    private void observeSessionData() {
        // Observe active session
        viewModel.getActiveSession().observe(this, session -> {
            if (session != null) {
                showActiveSession(session);
            } else {
                showPlannedSessions();
            }
        });
    }

    @Override
    protected void initViews() {
        activeSessionView = findViewById(R.id.active_session_layout);
        plannedSessionsView = findViewById(R.id.planned_sessions_layout);
        fabEndSession = findViewById(R.id.fab_end_session);
        plannedSessionsRecycler = findViewById(R.id.planned_sessions_recycler);
        emptyView = findViewById(R.id.empty_view);

        // Setup RecyclerView
        plannedSessionsAdapter = new PlannedSessionAdapter(this);
        plannedSessionsRecycler.setLayoutManager(new LinearLayoutManager(this));
        plannedSessionsRecycler.setAdapter(plannedSessionsAdapter);

        // Setup FAB
        fabEndSession.setOnClickListener(v -> endCurrentSession());

        Toolbar toolbar = findViewById(R.id.toolbar_layout);
        if (toolbar != null) {
            toolbar.inflateMenu(R.menu.menu_session);
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_history) {
                    openSessionHistory();
                    return true;
                }
                return false;
            });
        }
    }

    private void showActiveSession(HikingSessionEntity session) {
        if (activeSessionView != null && plannedSessionsView != null && fabEndSession != null) {
            activeSessionView.setVisibility(View.VISIBLE);
            plannedSessionsView.setVisibility(View.GONE);
            fabEndSession.setVisibility(View.VISIBLE);

            // Initialize shared session data for real-time tracking
            sharedViewModel.initializeSession(session.getId(), true);

            // Load historical path data if available
            if (mapFragment != null) {
                viewModel.getSessionStatistics(session.getId()).observe(this, statistics -> {
                    if (statistics != null && !statistics.isEmpty()) {
                        List<GeoPoint> points = new ArrayList<>();
                        for (HikingStatisticsEntity stat : statistics) {
                            points.add(new GeoPoint(stat.getLatitude(), stat.getLongitude()));
                        }
                        mapFragment.drawPath(points);
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sharedViewModel != null) {
            sharedViewModel.cleanup();
        }
    }

    private void openSessionHistory() {
        Intent intent = new Intent(this, SessionHistoryActivity.class);
        startActivity(intent);
    }


    private void showPlannedSessions() {
        if (activeSessionView != null && plannedSessionsView != null && fabEndSession != null) {
            activeSessionView.setVisibility(View.GONE);
            plannedSessionsView.setVisibility(View.VISIBLE);
            fabEndSession.setVisibility(View.GONE);
            loadPlannedSessions();
        }
    }

    private void updateActiveSessionUI(HikingSessionEntity session) {
        // Update statistics views based on your layout_session_statistics
        // This will be implemented later
    }

    private void loadPlannedSessions() {
        if (viewModel != null) {
            viewModel.getPlannedSessions().observe(this, sessions -> {
                if (plannedSessionsAdapter != null && emptyView != null && plannedSessionsRecycler != null) {
                    plannedSessionsAdapter.updateSessions(sessions);

                    // Show/hide empty view
                    if (sessions == null || sessions.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        plannedSessionsRecycler.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        plannedSessionsRecycler.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    @Override
    public void onStartSession(HikingSessionEntity session) {
        if (viewModel != null) {
            session.setStartTime(LocalDateTime.now());
            viewModel.updateSession(session);

            // Start tracking with sensors and statistics
            SensorsController.getInstance(this).startTracking();
            StatisticsCalculator.getInstance(this).startSession();
        }
    }

    private void endCurrentSession() {
        if (viewModel != null) {
            SensorsController.getInstance(this).stopTracking();

            // Save the current path points if needed
            if (mapFragment != null) {
                List<GeoPoint> finalPath = mapFragment.getPathPoints();
                // You can save these points to your database if needed
            }

            viewModel.endCurrentSession().observe(this, sessionId -> {
                if (sessionId != null) {
                    Intent intent = new Intent(this, SessionAnalysisActivity.class);
                    intent.putExtra("session_id", sessionId);
                    intent.putExtra("source", "session");
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    @Override
    protected void setupActions() {
        // No additional actions needed
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            observeSessionData();
        }
    }
}