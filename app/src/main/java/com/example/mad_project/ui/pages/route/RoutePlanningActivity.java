package com.example.mad_project.ui.pages.route;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RoutePlanningActivity extends BaseActivity {
    private RecyclerView routesRecyclerView;
    private RouteAdapter routeAdapter;
    private TextInputEditText searchEditText;
    private ChipGroup filterChipGroup;
    private FloatingActionButton fabStartFreeHiking;
    private RoutePlanningViewModel viewModel;
    private StatisticsCalculator statisticsCalculator;
    private SensorsController sensorsController;
    private Map<String, Boolean> sortDirections = new HashMap<>(); // true for ascending
    private String currentSortCriteria = null;

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
        searchEditText = findViewById(R.id.search_edit_text);
        filterChipGroup = findViewById(R.id.filter_chip_group);
        fabStartFreeHiking = findViewById(R.id.fab_start_free_hiking);

        // Initialize sort directions with default values
        initializeSortDirections();

        // Setup RecyclerView
        routesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        routeAdapter = new RouteAdapter(this, new RouteAdapter.OnRouteClickListener() {
            @Override
            public void onRouteStart(TrailEntity trail) {
                startHiking(trail);
            }

            @Override
            public void onRouteClick(TrailWithImages trailWithImages) {
                Intent intent = new Intent(RoutePlanningActivity.this, RouteDetailsActivity.class);
                intent.putExtra("trail_id", trailWithImages.getTrail().getId());
                startActivity(intent);
            }
        });
        routesRecyclerView.setAdapter(routeAdapter);

        setupSortChips();
        setupSearchAndFilter();

        // Setup FAB
        fabStartFreeHiking.setOnClickListener(v -> startFreeHiking());
    }

    private void initializeSortDirections() {
        // Initialize with default ascending order for all criteria
        sortDirections.put("name", true);
        sortDirections.put("difficulty", true);
        sortDirections.put("length", true);
        sortDirections.put("duration", true);
    }

    private void setupSortChips() {
        Chip difficultyChip = findViewById(R.id.sort_difficulty);
        Chip lengthChip = findViewById(R.id.sort_length);
        Chip durationChip = findViewById(R.id.sort_duration);

        // Set initial state
        for (Chip chip : new Chip[]{difficultyChip, lengthChip, durationChip}) {
            chip.setCloseIcon(getDrawable(R.drawable.ic_arrow_updown));
            chip.setCloseIconVisible(true);

            chip.setOnClickListener(v -> {
                Chip selectedChip = (Chip) v;
                String sortBy = selectedChip.getTag().toString();
                handleChipClick(selectedChip, sortBy);
            });
        }
    }

    private void handleChipClick(Chip chip, String sortBy) {
        if (sortBy.equals(currentSortCriteria)) {
            // Toggle sort direction
            boolean isAscending = sortDirections.get(sortBy);
            sortDirections.put(sortBy, !isAscending);
            updateChipIcon(chip, !isAscending);
        } else {
            // First click on this chip
            currentSortCriteria = sortBy;
            sortDirections.put(sortBy, true);
            updateChipIcon(chip, true);
            resetOtherChipsIcons(chip.getId());
        }

        filterAndSortRoutes();
    }

    private void updateChipIcon(Chip chip, boolean isAscending) {
        chip.setCloseIconResource(isAscending ?
                R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);
    }

    private void resetOtherChipsIcons(int selectedChipId) {
        Chip[] chips = new Chip[]{
                findViewById(R.id.sort_difficulty),
                findViewById(R.id.sort_length),
                findViewById(R.id.sort_duration)
        };

        for (Chip chip : chips) {
            if (chip.getId() != selectedChipId) {
                chip.setCloseIconResource(R.drawable.ic_arrow_updown);
            }
        }
    }

    private void setupSearchAndFilter() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterAndSortRoutes();
            }
        });
    }

    private String getCurrentSortCriteria() {
        int checkedId = filterChipGroup.getCheckedChipId();
        if (checkedId != View.NO_ID) {
            Chip selectedChip = filterChipGroup.findViewById(checkedId);
            return selectedChip.getTag().toString();
        }
        return "name"; // Default sort
    }

    private void filterAndSortRoutes() {
        String searchQuery = searchEditText.getText().toString().toLowerCase().trim();
        List<TrailWithImages> filteredList = new ArrayList<>();

        List<TrailWithImages> originalList = viewModel.getTrails().getValue();
        if (originalList == null) return;

        // Filter
        for (TrailWithImages trail : originalList) {
            if (matchesSearchCriteria(trail, searchQuery) && matchesFilterCriteria(trail)) {
                filteredList.add(trail);
            }
        }

        // Sort
        if (currentSortCriteria != null) {
            Boolean direction = sortDirections.get(currentSortCriteria);
            boolean isAscending = direction != null ? direction : true;
            viewModel.sortTrails(filteredList, currentSortCriteria, isAscending);
        } else {
            // Default sort by name
            viewModel.sortTrails(filteredList, "name", true);
        }

        routeAdapter.setTrails(filteredList);
    }

    private void filterAndSortRoutes(String sortBy) {
        String searchQuery = searchEditText.getText().toString().toLowerCase().trim();
        List<TrailWithImages> filteredList = new ArrayList<>();

        List<TrailWithImages> originalList = viewModel.getTrails().getValue();
        if (originalList == null) return;

        // Filter
        for (TrailWithImages trail : originalList) {
            if (matchesSearchCriteria(trail, searchQuery) && matchesFilterCriteria(trail)) {
                filteredList.add(trail);
            }
        }

        // Sort
        boolean isAscending = sortDirections.get(sortBy);
        viewModel.sortTrails(filteredList, sortBy, isAscending);

        // Update adapter
        routeAdapter.setTrails(filteredList);
    }

    private void filterRoutes() {
        String searchQuery = searchEditText.getText().toString().toLowerCase().trim();
        List<TrailWithImages> filteredList = new ArrayList<>();

        List<TrailWithImages> originalList = viewModel.getTrails().getValue();
        if (originalList == null) return;

        for (TrailWithImages trail : originalList) {
            if (matchesSearchCriteria(trail, searchQuery) && matchesFilterCriteria(trail)) {
                filteredList.add(trail);
            }
        }

        routeAdapter.setTrails(filteredList);
    }

    private boolean matchesSearchCriteria(TrailWithImages trail, String query) {
        if (query.isEmpty()) return true;

        TrailEntity trailEntity = trail.getTrail();
        return trailEntity.getTrailName().toLowerCase().contains(query);
    }

    private boolean matchesFilterCriteria(TrailWithImages trail) {
        Chip difficultyChip = findViewById(R.id.sort_difficulty);
        Chip lengthChip = findViewById(R.id.sort_length);
        Chip durationChip = findViewById(R.id.sort_duration);

        TrailEntity trailEntity = trail.getTrail();
        boolean matches = true;

        if (difficultyChip.isChecked()) {
            matches &= trailEntity.getDifficultyRating() <= 4.0; // Example criteria
        }
        if (lengthChip.isChecked()) {
            matches &= trailEntity.getLengthRating() <= 10.0; // Example criteria
        }
        if (durationChip.isChecked()) {
            matches &= trailEntity.getDurationRating() <= 3.0; // Example criteria
        }

        return matches;
    }

    @Override
    protected void setupActions() {
        if (viewModel != null) {
            viewModel.getTrails().observe(this, trails -> {
                if (trails != null) {
                    filterRoutes(); // Apply current filters to new data
                }
            });
        }
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