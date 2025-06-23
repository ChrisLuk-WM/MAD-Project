package com.example.mad_project.ui.pages.sessions.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.HikingStatisticsEntity;
import com.example.mad_project.statistics.StatisticsType;
import com.example.mad_project.ui.pages.sessions.SessionViewModel;
import com.example.mad_project.ui.views.StepsGraphView;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepsStatisticsFragment extends BaseStatisticsFragment {
    private StepsGraphView stepsGraph;
    private TextView totalStepsText;
    private TextView stepsPerMinuteText;
    private SessionViewModel viewModel;
    private Handler updateHandler;
    private static final int UPDATE_INTERVAL = 5000; // 5 seconds
    private LocalDateTime sessionStartTime;
    private Map<Integer, Integer> stepsPerMinute;
    private int currentMinuteSteps = 0;
    private int lastTotalSteps = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_steps_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);  // This will initialize viewModel
        stepsGraph = view.findViewById(R.id.steps_graph);
        totalStepsText = view.findViewById(R.id.text_total_steps);
        stepsPerMinuteText = view.findViewById(R.id.text_steps_per_minute);
        stepsPerMinute = new HashMap<>();

        setupChart();

        if (isRealTime) {
            updateHandler = new Handler(Looper.getMainLooper());
            sessionStartTime = LocalDateTime.now();
            startRealTimeUpdates();
        }
        loadHistoricalData();  // Load historical data for both real-time and historical views
    }


    @Override
    protected void startRealTimeUpdates() {
        if (updateHandler == null) {
            updateHandler = new Handler(Looper.getMainLooper());
        }

        updateHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isRealTime && isAdded()) {
                    updateStatistics();
                    updateHandler.postDelayed(this, UPDATE_INTERVAL);
                }
            }
        });
    }

    @Override
    protected void setupChart() {
        if (!isAdded()) return;

        stepsGraph.setBackgroundColor(requireContext().getColor(
                isNightMode() ? R.color.graph_background_dark : R.color.graph_background_light));
        stepsGraph.setTextColor(requireContext().getColor(
                isNightMode() ? R.color.graph_text_dark : R.color.graph_text_light));
        stepsGraph.setGridColor(requireContext().getColor(
                isNightMode() ? R.color.graph_grid_dark : R.color.graph_grid_light));
        stepsGraph.setBarColor(requireContext().getColor(
                isNightMode() ? R.color.graph_line_dark : R.color.graph_line_light));
    }

    protected boolean isNightMode() {
        int nightModeFlags = requireContext().getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isRealTime) {
            startRealTimeUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (updateHandler != null) {
            updateHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void updateStatistics() {
        if (!isAdded() || sessionStartTime == null) return;

        Integer currentSteps = statisticsManager.getValue(StatisticsType.STEPS);
        if (currentSteps == null) return;

        // Calculate steps in current minute
        int totalSteps = currentSteps;
        int stepsDelta = totalSteps - lastTotalSteps;
        lastTotalSteps = totalSteps;

        // Update current minute steps
        int currentMinute = (int) ChronoUnit.MINUTES.between(sessionStartTime, LocalDateTime.now());
        currentMinuteSteps += stepsDelta;
        stepsPerMinute.put(currentMinute, currentMinuteSteps);

        // Reset steps count for new minute
        if (currentMinute > stepsPerMinute.size() - 1) {
            currentMinuteSteps = 0;
        }

        // Update UI
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                stepsGraph.updateSteps(stepsPerMinute);
                totalStepsText.setText(String.format("Total Steps: %d", totalSteps));
                stepsPerMinuteText.setText(String.format("Steps/min: %d", currentMinuteSteps));
            });
        }
    }

    @Override
    protected void loadHistoricalData() {
        if (!isAdded() || viewModel == null) return;  // Add safety check
        super.loadHistoricalData();
    }

    private void updateHistoricalGraph(List<HikingStatisticsEntity> statistics) {
        if (statistics == null || statistics.isEmpty()) return;

        Map<Integer, Integer> historicalSteps = new HashMap<>();
        int totalSteps = 0;

        for (HikingStatisticsEntity stat : statistics) {
            int minute = (int) ChronoUnit.MINUTES.between(sessionStartTime, stat.getDateTime());
            int steps = stat.getSteps();
            historicalSteps.put(minute, steps);
            totalSteps += steps;
        }

        stepsGraph.updateSteps(historicalSteps);
        if (getActivity() != null) {
            int finalTotalSteps = totalSteps;
            getActivity().runOnUiThread(() -> {
                totalStepsText.setText(String.format("Total Steps: %d", finalTotalSteps));
                int avgStepsPerMinute = finalTotalSteps / Math.max(1, historicalSteps.size());
                stepsPerMinuteText.setText(String.format("Avg Steps/min: %d", avgStepsPerMinute));
            });
        }

        lastTotalSteps = totalSteps;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (updateHandler != null) {
            updateHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onHistoricalDataLoaded(List<HikingStatisticsEntity> statistics) {
        updateHistoricalGraph(statistics);
    }
}