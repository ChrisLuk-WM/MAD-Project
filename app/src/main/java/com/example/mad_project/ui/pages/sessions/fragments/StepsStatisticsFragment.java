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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_steps_statistics, container, false);
    }

    @Override
    protected void initializeViews(View view) {
        stepsGraph = view.findViewById(R.id.steps_graph);
        totalStepsText = view.findViewById(R.id.text_total_steps);
        stepsPerMinuteText = view.findViewById(R.id.text_steps_per_minute);
    }

    @Override
    protected void setupChart() {
        if (!isAdded() || stepsGraph == null) return;

        stepsGraph.setBackgroundColor(requireContext().getColor(
                isNightMode() ? R.color.graph_background_dark : R.color.graph_background_light));
        stepsGraph.setTextColor(requireContext().getColor(
                isNightMode() ? R.color.graph_text_dark : R.color.graph_text_light));
        stepsGraph.setGridColor(requireContext().getColor(
                isNightMode() ? R.color.graph_grid_dark : R.color.graph_grid_light));
        stepsGraph.setBarColor(requireContext().getColor(
                isNightMode() ? R.color.graph_line_dark : R.color.graph_line_light));
    }

    @Override
    protected void onStatisticsUpdated(List<HikingStatisticsEntity> statistics) {
        if (statistics == null || statistics.isEmpty() || sessionStartTime == null) return;

        Map<Integer, Integer> stepsPerMinute = new HashMap<>();
        int totalSteps = 0;
        int currentMinuteSteps = 0;

        for (HikingStatisticsEntity stat : statistics) {
            int minute = (int) ChronoUnit.MINUTES.between(sessionStartTime, stat.getDateTime());
            int steps = stat.getSteps();

            // Accumulate steps for each minute
            currentMinuteSteps = stepsPerMinute.getOrDefault(minute, 0) + steps;
            stepsPerMinute.put(minute, currentMinuteSteps);
            totalSteps += steps;
        }

        // Update UI
        stepsGraph.updateSteps(stepsPerMinute);
        if (getActivity() != null) {
            int finalTotalSteps = totalSteps;
            getActivity().runOnUiThread(() -> {
                totalStepsText.setText(String.format("Total Steps: %d", finalTotalSteps));

                // Calculate average steps per minute
                int avgStepsPerMinute = finalTotalSteps / Math.max(1, stepsPerMinute.size());
                stepsPerMinuteText.setText(String.format("Avg Steps/min: %d", avgStepsPerMinute));
            });
        }
    }
}