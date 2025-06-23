package com.example.mad_project.ui.pages.sessions.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mad_project.database.entities.HikingStatisticsEntity;
import com.example.mad_project.statistics.StatisticsCalculator;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.ui.pages.sessions.SessionViewModel;

import java.time.LocalDateTime;
import java.util.List;

public abstract class BaseStatisticsFragment extends Fragment {
    protected StatisticsCalculator calculator;
    protected StatisticsManager statisticsManager;
    protected boolean isRealTime = false;
    protected long sessionId = -1;
    protected SessionViewModel viewModel;
    protected LocalDateTime sessionStartTime;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calculator = StatisticsCalculator.getInstance(requireContext());
        statisticsManager = StatisticsManager.getInstance();

        Bundle args = getArguments();
        if (args != null) {
            isRealTime = args.getBoolean("isRealTime", false);
            sessionId = args.getLong("sessionId", -1);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize ViewModel in onViewCreated
        viewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);
    }

    public void onSessionActive(long activeSessionId) {
        this.sessionId = activeSessionId;
        if (isRealTime) {
            // Set current time as initial start time
            sessionStartTime = LocalDateTime.now();
            setupChart();
            // Load historical data if any exists
            loadHistoricalData();
            // Start real-time updates
            startRealTimeUpdates();
        }
    }

    protected abstract void startRealTimeUpdates();
    protected void loadHistoricalData() {
        if (sessionId != -1 && viewModel != null) {  // Add null check
            viewModel.getSessionStatistics(sessionId).observe(getViewLifecycleOwner(), statistics -> {
                if (statistics != null && !statistics.isEmpty()) {
                    sessionStartTime = statistics.get(0).getDateTime();
                    onHistoricalDataLoaded(statistics);
                }
            });
        }
    }

    protected abstract void updateStatistics();
    protected abstract void setupChart();

    protected boolean isNightMode() {
        return (requireContext().getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setupChart(); // Re-setup chart when configuration changes (e.g., dark mode toggle)
    }

    protected void initializeSessionStartTime(long sessionId, Runnable onInitialized) {
        if (sessionId != -1) {
            viewModel.getSessionStatistics(sessionId).observe(getViewLifecycleOwner(), statistics -> {
                if (statistics != null && !statistics.isEmpty()) {
                    sessionStartTime = statistics.get(0).getDateTime();
                    onHistoricalDataLoaded(statistics);
                } else {
                    sessionStartTime = LocalDateTime.now();
                }
                if (onInitialized != null) {
                    onInitialized.run();
                }
            });
        } else {
            sessionStartTime = LocalDateTime.now();
            if (onInitialized != null) {
                onInitialized.run();
            }
        }
    }

    protected abstract void onHistoricalDataLoaded(List<HikingStatisticsEntity> statistics);
}