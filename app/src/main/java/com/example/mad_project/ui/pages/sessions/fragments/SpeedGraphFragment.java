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

public class SpeedGraphFragment extends BaseStatisticsFragment {
    private SpeedGraphView speedGraph;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speed_graph, container, false);
    }

    @Override
    protected void initializeViews(View view) {
        speedGraph = view.findViewById(R.id.speed_graph);
    }

    @Override
    protected void setupChart() {
        if (!isAdded() || speedGraph == null) return;

        speedGraph.setBackgroundColor(requireContext().getColor(
                isNightMode() ? R.color.graph_background_dark : R.color.graph_background_light));
        speedGraph.setTextColor(requireContext().getColor(
                isNightMode() ? R.color.graph_text_dark : R.color.graph_text_light));
        speedGraph.setGridColor(requireContext().getColor(
                isNightMode() ? R.color.graph_grid_dark : R.color.graph_grid_light));
        speedGraph.setLineColor(requireContext().getColor(
                isNightMode() ? R.color.graph_line_dark : R.color.graph_line_light));
    }

    @Override
    protected void onStatisticsUpdated(List<HikingStatisticsEntity> statistics) {
        if (statistics == null || statistics.isEmpty() || sessionStartTime == null) return;

        List<SpeedGraphView.Entry> entries = new ArrayList<>();
        for (HikingStatisticsEntity stat : statistics) {
            float timeSeconds = (float) ChronoUnit.SECONDS.between(sessionStartTime, stat.getDateTime());
            float speedKmh = (float) (stat.getSpeed() * 3.6); // Convert m/s to km/h
            entries.add(new SpeedGraphView.Entry(timeSeconds, speedKmh));
        }
        speedGraph.setEntries(entries);
    }
}