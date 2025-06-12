package com.example.mad_project.ui.pages.route;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.TrailEntity;

public class RoutePlanningFragment extends Fragment {
    private TextView routeName;
    private TextView suggestions;
    private Button btnStartHiking;
    private RouteDetailsViewModel viewModel;
    private long trailId;

    public static RoutePlanningFragment newInstance(long trailId) {
        RoutePlanningFragment fragment = new RoutePlanningFragment();
        Bundle args = new Bundle();
        args.putLong("trail_id", trailId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trailId = getArguments().getLong("trail_id", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route_planning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        routeName = view.findViewById(R.id.route_name);
        suggestions = view.findViewById(R.id.suggestions);
        btnStartHiking = view.findViewById(R.id.btn_start_hiking);

        // Get ViewModel from activity
        viewModel = new ViewModelProvider(requireActivity()).get(RouteDetailsViewModel.class);

        // Setup observers
        viewModel.getTrailWithImages().observe(getViewLifecycleOwner(), trailWithImages -> {
            if (trailWithImages != null) {
                updateUI(trailWithImages.getTrail());
            }
        });

        // Setup actions
        btnStartHiking.setOnClickListener(v -> showStartHikingDialog());
    }

    private void updateUI(TrailEntity trail) {
        routeName.setText(trail.getTrailName());
        suggestions.setText(viewModel.getPersonalizedSuggestions());
    }

    private void showStartHikingDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Start Hiking")
                .setMessage("Are you ready to start hiking?")
                .setPositiveButton("Start", (dialog, which) -> {
                    viewModel.startHiking(requireContext());
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}