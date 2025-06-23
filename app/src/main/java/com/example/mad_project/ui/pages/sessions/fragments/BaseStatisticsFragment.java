package com.example.mad_project.ui.pages.sessions.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    protected SharedSessionViewModel sharedViewModel;
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

        sharedViewModel = new ViewModelProvider(requireActivity(),
                new SharedSessionViewModel.Factory(
                        new ViewModelProvider(requireActivity()).get(SessionViewModel.class)
                )).get(SharedSessionViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupChart();
        observeSharedData();
    }

    protected abstract void initializeViews(View view);

    protected void observeSharedData() {
        sharedViewModel.getSessionStartTime().observe(getViewLifecycleOwner(), startTime -> {
            sessionStartTime = startTime;
        });

        sharedViewModel.getSessionStatistics().observe(getViewLifecycleOwner(), statistics -> {
            if (statistics != null && !statistics.isEmpty() && sessionStartTime != null) {
                onStatisticsUpdated(statistics);
            }
        });
    }

    public void onSessionActive(long activeSessionId) {
        this.sessionId = activeSessionId;
        if (isRealTime) {
            sessionStartTime = LocalDateTime.now();
            setupChart();
            // Initialize session in SharedViewModel instead of handling it here
            sharedViewModel.initializeSession(sessionId, true);
        }
    }

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

    protected abstract void onStatisticsUpdated(List<HikingStatisticsEntity> statistics);
}