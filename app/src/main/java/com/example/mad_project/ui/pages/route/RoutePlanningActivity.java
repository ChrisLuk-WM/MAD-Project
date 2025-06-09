package com.example.mad_project.ui.pages.route;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.sensors.SensorsController;
import com.example.mad_project.statistics.StatisticsCalculator;
import com.example.mad_project.ui.BaseActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class RoutePlanningActivity extends BaseActivity {
    private RecyclerView routesRecyclerView;
    private RouteAdapter routeAdapter;
    private TabLayout tabLayout;
    private FloatingActionButton fabStartFreeHiking;
    private RoutePlanningViewModel viewModel;
    private StatisticsCalculator statisticsCalculator;
    private SensorsController sensorsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize ViewModel before calling super.onCreate
        viewModel = new ViewModelProvider(this,
                new RoutePlanningViewModel.Factory(getApplication()))
                .get(RoutePlanningViewModel.class);

        // Initialize other components
        statisticsCalculator = StatisticsCalculator.getInstance(this);
        sensorsController = SensorsController.getInstance(this);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_route_planning;
    }

    @Override
    protected void initViews() {
        routesRecyclerView = findViewById(R.id.routes_recycler_view);
        tabLayout = findViewById(R.id.tab_layout);
        fabStartFreeHiking = findViewById(R.id.fab_start_free_hiking);

        routesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        routeAdapter = new RouteAdapter(this, new RouteAdapter.OnRouteClickListener() {
            @Override
            public void onRouteStart(TrailEntity trail) {
                startHiking(trail);
            }

            @Override
            public void onRouteClick(TrailEntity trail) {
                showRouteDetails(trail);
            }
        });
        routesRecyclerView.setAdapter(routeAdapter);

        // Set initial visibility
        routesRecyclerView.setVisibility(View.VISIBLE);
        fabStartFreeHiking.hide();
    }

    @Override
    protected void setupActions() {
        // Observe trails data through ViewModel
        if (viewModel != null) {  // Add null check
            viewModel.getTrails().observe(this, trails -> {
                if (trails != null) {
                    routeAdapter.setTrails(trails);
                }
            });
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Show all routes
                    routesRecyclerView.setVisibility(View.VISIBLE);
                    fabStartFreeHiking.hide();
                } else {
                    // Free hiking tab
                    routesRecyclerView.setVisibility(View.GONE);
                    fabStartFreeHiking.show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        fabStartFreeHiking.setOnClickListener(v -> startFreeHiking());
    }

    private void startHiking(TrailEntity trail) {
        new AlertDialog.Builder(this)
                .setTitle("Start Hiking")
                .setMessage("Do you want to start hiking " + trail.getTrailName() + "?")
                .setPositiveButton("Start", (dialog, which) -> {
                    sensorsController.startTracking();
                    statisticsCalculator.startSession();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startFreeHiking() {
        new AlertDialog.Builder(this)
                .setTitle("Start Free Hiking")
                .setMessage("Do you want to start free hiking?")
                .setPositiveButton("Start", (dialog, which) -> {
                    sensorsController.startTracking();
                    statisticsCalculator.startSession();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRouteDetails(TrailEntity trail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_route_details, null);

        ImageView routeImage = dialogView.findViewById(R.id.route_detail_image);
        TextView routeName = dialogView.findViewById(R.id.route_detail_name);
        TextView difficultyRating = dialogView.findViewById(R.id.route_detail_difficulty);
        TextView lengthRating = dialogView.findViewById(R.id.route_detail_length);
        TextView durationRating = dialogView.findViewById(R.id.route_detail_duration);
        TextView sightRating = dialogView.findViewById(R.id.route_detail_sight);

        routeName.setText(trail.getTrailName());
        difficultyRating.setText(String.format(Locale.getDefault(),
                "Difficulty: %.1f", trail.getDifficultyRating()));
        lengthRating.setText(String.format(Locale.getDefault(),
                "Length: %.1f km", trail.getLengthRating()));
        durationRating.setText(String.format(Locale.getDefault(),
                "Duration: %.1f h", trail.getDurationRating()));
        sightRating.setText(String.format(Locale.getDefault(),
                "Sight Rating: %.1f", trail.getSightRating()));

        if (trail.getImagePath() != null) {
            try {
                File imgFile = new File(trail.getImagePath());
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    routeImage.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                routeImage.setImageResource(R.drawable.ic_hiking);
            }
        }

        builder.setView(dialogView)
                .setPositiveButton("Start", (dialog, which) -> startHiking(trail))
                .setNegativeButton("Close", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}