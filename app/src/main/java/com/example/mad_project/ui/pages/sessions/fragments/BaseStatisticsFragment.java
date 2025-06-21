package com.example.mad_project.ui.pages.sessions.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mad_project.statistics.StatisticsCalculator;
import com.example.mad_project.statistics.StatisticsManager;

public abstract class BaseStatisticsFragment extends Fragment {
    protected StatisticsCalculator calculator;
    protected StatisticsManager statisticsManager;
    protected boolean isRealTime = false;
    protected long sessionId = -1;

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

    public void onSessionActive(long activeSessionId) {
        this.sessionId = activeSessionId;
        if (isRealTime) {
            // Reset any existing data and start fresh for the new session
            setupChart();
            updateStatistics();
        }
    }

    protected abstract void updateStatistics();
    protected abstract void setupChart();
}