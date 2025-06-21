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
import com.example.mad_project.ui.views.SpeedGraphView;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class SpeedGraphFragment extends BaseStatisticsFragment {
    private SpeedGraphView speedGraph;
    private SessionViewModel viewModel;
    private Handler updateHandler;
    private static final int UPDATE_INTERVAL = 5000; // 5 seconds
    private LocalDateTime sessionStartTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speed_graph, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        speedGraph = view.findViewById(R.id.speed_graph);
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
        // SpeedGraphView handles its own initialization
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

        Double currentSpeed = statisticsManager.getValue(StatisticsType.SPEED);
        if (currentSpeed == null) return;

        float speedKmh = (float) (currentSpeed * 3.6); // Convert m/s to km/h
        float timeSeconds = (float) ChronoUnit.SECONDS.between(sessionStartTime, LocalDateTime.now());

        speedGraph.addEntry(timeSeconds, speedKmh);
    }

    private void loadHistoricalData() {
        if (sessionId != -1) {
            viewModel.getSessionStatistics(sessionId).observe(getViewLifecycleOwner(), this::updateHistoricalGraph);
        }
    }

    private void updateHistoricalGraph(List<HikingStatisticsEntity> statistics) {
        if (statistics == null || statistics.isEmpty()) return;

        List<SpeedGraphView.Entry> entries = new ArrayList<>();
        sessionStartTime = statistics.get(0).getDateTime();

        for (HikingStatisticsEntity stat : statistics) {
            float timeSeconds = (float) ChronoUnit.SECONDS.between(sessionStartTime, stat.getDateTime());
            float speedKmh = (float) (stat.getSpeed() * 3.6); // Convert m/s to km/h
            entries.add(new SpeedGraphView.Entry(timeSeconds, speedKmh));
        }

        speedGraph.setEntries(entries);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (updateHandler != null) {
            updateHandler.removeCallbacksAndMessages(null);
        }
    }
}