package com.example.mad_project.ui.pages.sessions.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.HikingStatisticsEntity;
import com.example.mad_project.statistics.StatisticsType;
import com.example.mad_project.ui.pages.sessions.SessionViewModel;
import com.example.mad_project.ui.views.SpeedGraphView;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class SpeedGraphFragment extends BaseStatisticsFragment  {
    private SpeedGraphView speedGraph;
    private SessionViewModel viewModel;
    private Handler updateHandler;
    private static final int UPDATE_INTERVAL = 5000; // 5 seconds
    private LocalDateTime sessionStartTime;
    private List<SpeedGraphView.Entry> entries = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speed_graph, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);  // This will initialize viewModel
        speedGraph = view.findViewById(R.id.speed_graph);

        setupChart();

        if (isRealTime) {
            updateHandler = new Handler(Looper.getMainLooper());
            sessionStartTime = LocalDateTime.now();
            startRealTimeUpdates();
        }
        loadHistoricalData();  // Load historical data for both real-time and historical views
    }

    @Override
    protected void setupChart() {
        if (!isAdded()) return;

        speedGraph.setBackgroundColor(requireContext().getColor(
                isNightMode() ? R.color.graph_background_dark : R.color.graph_background_light));
        speedGraph.setTextColor(requireContext().getColor(
                isNightMode() ? R.color.graph_text_dark : R.color.graph_text_light));
        speedGraph.setGridColor(requireContext().getColor(
                isNightMode() ? R.color.graph_grid_dark : R.color.graph_grid_light));
        speedGraph.setLineColor(requireContext().getColor(
                isNightMode() ? R.color.graph_line_dark : R.color.graph_line_light));
    }

    protected boolean isNightMode() {
        int nightModeFlags = requireContext().getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    protected void startRealTimeUpdates() {
        if (updateHandler == null) {
            updateHandler = new Handler(Looper.getMainLooper());
        }

        updateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRealTime && isAdded()) {
                    updateStatistics();
                    updateHandler.postDelayed(this, UPDATE_INTERVAL);
                }
            }
        }, UPDATE_INTERVAL);
    }
    @Override
    protected void updateStatistics() {
        if (!isAdded()) return;

        Double currentSpeed = statisticsManager.getValue(StatisticsType.SPEED);
        if (currentSpeed == null) return;

        float speedKmh = (float) (currentSpeed * 3.6);
        float timeSeconds = sessionStartTime != null ?
                (float) ChronoUnit.SECONDS.between(sessionStartTime, LocalDateTime.now()) : 0f;

        speedGraph.addEntry(timeSeconds, speedKmh);
    }

    @Override
    protected void loadHistoricalData() {
        if (!isAdded() || viewModel == null) return;  // Add safety check
        super.loadHistoricalData();
    }

    private void updateHistoricalGraph(List<HikingStatisticsEntity> statistics) {
        if (statistics == null || statistics.isEmpty()) return;

        entries.clear();
        for (HikingStatisticsEntity stat : statistics) {
            float timeSeconds = (float) ChronoUnit.SECONDS.between(sessionStartTime, stat.getDateTime());
            float speedKmh = (float) (stat.getSpeed() * 3.6);
            entries.add(new SpeedGraphView.Entry(timeSeconds, speedKmh));
        }

        speedGraph.setEntries(new ArrayList<>(entries));
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