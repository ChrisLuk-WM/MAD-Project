package com.example.mad_project.ui.pages.sessions.fragments;

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
import com.example.mad_project.ui.views.ElevationGraphView;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ElevationGraphFragment extends BaseStatisticsFragment {
    private ElevationGraphView elevationGraph;
    private SessionViewModel viewModel;
    private Handler updateHandler;
    private static final int UPDATE_INTERVAL = 5000; // 5 seconds
    private LocalDateTime sessionStartTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_elevation_graph, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        elevationGraph = view.findViewById(R.id.elevation_graph);
        viewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);

        setupChart();

        if (isRealTime) {
            updateHandler = new Handler(Looper.getMainLooper());
            startRealTimeUpdates();
            sessionStartTime = LocalDateTime.now();
        } else if (sessionId != -1) {
            loadHistoricalData();
        }
    }

    @Override
    protected void setupChart() {
        // ElevationGraphView handles its own initialization
    }

    private void startRealTimeUpdates() {
        updateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateStatistics();
                if (isRealTime && isAdded()) {
                    updateHandler.postDelayed(this, UPDATE_INTERVAL);
                }
            }
        }, UPDATE_INTERVAL);
    }

    @Override
    protected void updateStatistics() {
        if (!isAdded()) return;

        Double currentElevation = statisticsManager.getValue(StatisticsType.ALTITUDE);
        if (currentElevation == null) return;

        float elevationMeters = currentElevation.floatValue();
        float timeSeconds = (float) ChronoUnit.SECONDS.between(sessionStartTime, LocalDateTime.now());

        elevationGraph.addEntry(timeSeconds, elevationMeters);
    }

    private void loadHistoricalData() {
        if (sessionId != -1) {
            viewModel.getSessionStatistics(sessionId).observe(getViewLifecycleOwner(), this::updateHistoricalGraph);
        }
    }

    private void updateHistoricalGraph(List<HikingStatisticsEntity> statistics) {
        if (statistics == null || statistics.isEmpty()) return;

        List<ElevationGraphView.Entry> entries = new ArrayList<>();
        sessionStartTime = statistics.get(0).getDateTime();

        for (HikingStatisticsEntity stat : statistics) {
            float timeSeconds = (float) ChronoUnit.SECONDS.between(sessionStartTime, stat.getDateTime());
            entries.add(new ElevationGraphView.Entry(timeSeconds, (float) stat.getAltitude()));
        }

        elevationGraph.setEntries(entries);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (updateHandler != null) {
            updateHandler.removeCallbacksAndMessages(null);
        }
    }
}