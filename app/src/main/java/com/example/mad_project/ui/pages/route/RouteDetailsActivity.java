package com.example.mad_project.ui.pages.route;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.database.entities.TrailImage;
import com.example.mad_project.ui.BaseActivity;
import com.example.mad_project.utils.MapUtil;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class RouteDetailsActivity extends BaseActivity {
    private RouteImageAdapter imageAdapter;
    private long trailId;
    private RouteDetailsViewModel viewModel;

    @Override
    protected boolean useNavigationDrawer() {
        return false; // This activity doesn't need the navigation drawer
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize ViewModel before super.onCreate
        trailId = getIntent().getLongExtra("trail_id", -1);
        if (trailId == -1) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this,
                new RouteDetailsViewModel.Factory(getApplication(), trailId))
                .get(RouteDetailsViewModel.class);

        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // Add the planning fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.planning_container, RoutePlanningFragment.newInstance(trailId))
                    .commit();
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_route_details;
    }

    @Override
    protected void initViews() {
        // Initialize gallery adapter
        imageAdapter = new RouteImageAdapter(this::showFullScreenImage);

        // Setup loading indicator
        View loadingIndicator = findViewById(R.id.loading_indicator);
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void setupActions() {
        // Show loading state
        showLoading(true);

        viewModel.getTrailWithImages().observe(this, trailWithImages -> {
            // Hide loading state
            showLoading(false);

            if (trailWithImages != null) {
                updateUI(trailWithImages);
            } else {
                // Show error state
                showError();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        View loadingIndicator = findViewById(R.id.loading_indicator);
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        // Remove reference to routeName.setText()
        // Instead, show/hide content containers
        View detailsHeader = findViewById(R.id.route_details_header);
        View planningContainer = findViewById(R.id.planning_container);

        if (isLoading) {
            // Hide content during loading
            if (detailsHeader != null) detailsHeader.setVisibility(View.INVISIBLE);
            if (planningContainer != null) planningContainer.setVisibility(View.INVISIBLE);
        } else {
            // Show content after loading
            if (detailsHeader != null) detailsHeader.setVisibility(View.VISIBLE);
            if (planningContainer != null) planningContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showError() {
        // Show error state in UI
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Failed to load trail details")
                .setPositiveButton("Retry", (dialog, which) -> viewModel.refreshData())
                .setNegativeButton("Close", (dialog, which) -> finish())
                .show();
    }


    protected void updateUI(TrailWithImages trailWithImages) {
        try {
            TrailEntity trail = trailWithImages.getTrail();
            List<TrailImage> galleryImages = trailWithImages.getGalleryImages();

            // Remove reference to routeName.setText()
            // This is now handled in the fragment

            // Load map
            ImageView mapView = findViewById(R.id.route_map);
            if (mapView != null) {
                MapUtil.loadRouteMap(mapView, trail.getImagePath(), this);
            }

            // Update statistics in the upper part (below map)
            TextView difficultyValue = findViewById(R.id.difficulty_value);
            TextView lengthValue = findViewById(R.id.length_value);
            TextView durationValue = findViewById(R.id.duration_value);
            TextView sightValue = findViewById(R.id.sight_value);

            if (difficultyValue != null) difficultyValue.setText(String.format(Locale.getDefault(), "%.1f", trail.getDifficultyRating()));
            if (lengthValue != null) lengthValue.setText(String.format(Locale.getDefault(), "%.1f km", trail.getLengthRating()));
            if (durationValue != null) durationValue.setText(String.format(Locale.getDefault(), "%.1f h", trail.getDurationRating()));
            if (sightValue != null) sightValue.setText(String.format(Locale.getDefault(), "%.1f", trail.getSightRating()));

            // Update gallery
            ViewPager2 galleryPager = findViewById(R.id.gallery_pager);
            if (galleryPager != null) {
                if (galleryPager.getAdapter() == null) {
                    galleryPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                    galleryPager.setOffscreenPageLimit(1);
                    galleryPager.setAdapter(imageAdapter);
                    galleryPager.setPageTransformer((page, position) -> {
                        float absPosition = Math.abs(position);
                        page.setAlpha(1.0f - absPosition * 0.5f);
                        float scale = 1.0f - absPosition * 0.15f;
                        page.setScaleX(scale);
                        page.setScaleY(scale);
                    });
                }
                imageAdapter.setImages(galleryImages);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError();
        }
    }
    private void startHiking() {
        new AlertDialog.Builder(this)
                .setTitle("Start Hiking")
                .setMessage("Are you ready to start hiking?")
                .setPositiveButton("Start", (dialog, which) -> {
                    viewModel.startHiking(this);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFullScreenImage(TrailImage image) {
        // Create a full screen dialog to show the image
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        try {
            File imgFile = new File(image.getImagePath());
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.ic_hiking);
        }

        imageView.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(imageView);
        dialog.show();
    }
}