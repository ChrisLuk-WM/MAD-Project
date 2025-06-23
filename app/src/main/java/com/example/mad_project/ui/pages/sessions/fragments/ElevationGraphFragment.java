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
import com.example.mad_project.ui.views.ElevationGraphView;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ElevationGraphFragment extends BaseStatisticsFragment {
    private ElevationGraphView elevationGraph;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_elevation_graph, container, false);
    }

    @Override
    protected void initializeViews(View view) {
        elevationGraph = view.findViewById(R.id.elevation_graph);
    }

    @Override
    protected void setupChart() {
        if (!isAdded() || elevationGraph == null) return;

        elevationGraph.setBackgroundColor(requireContext().getColor(
                isNightMode() ? R.color.graph_background_dark : R.color.graph_background_light));
        elevationGraph.setTextColor(requireContext().getColor(
                isNightMode() ? R.color.graph_text_dark : R.color.graph_text_light));
        elevationGraph.setGridColor(requireContext().getColor(
                isNightMode() ? R.color.graph_grid_dark : R.color.graph_grid_light));
        elevationGraph.setLineColor(requireContext().getColor(
                isNightMode() ? R.color.graph_line_dark : R.color.graph_line_light));
    }

    @Override
    protected void onStatisticsUpdated(List<HikingStatisticsEntity> statistics) {
        if (statistics == null || statistics.isEmpty() || sessionStartTime == null) return;

        List<ElevationGraphView.Entry> entries = new ArrayList<>();
        for (HikingStatisticsEntity stat : statistics) {
            float timeSeconds = (float) ChronoUnit.SECONDS.between(sessionStartTime, stat.getDateTime());
            float elevationMeters = (float) stat.getAltitude();
            entries.add(new ElevationGraphView.Entry(timeSeconds, elevationMeters));
        }
        elevationGraph.setEntries(entries);
    }
}