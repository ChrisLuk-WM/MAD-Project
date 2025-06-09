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
    private ViewPager2 imagePager;
    private RouteImageAdapter imageAdapter;
    private TextView routeName;
    private TextView routeDifficulty;
    private TextView routeLength;
    private TextView routeDuration;
    private TextView routeSight;
    private TextView suggestions;
    private Button btnStartHiking;
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
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_route_details;
    }

    @Override
    protected void initViews() {
        // Find views from the new layout structure
        routeName = findViewById(R.id.route_name);

        // Statistics views are now found in updateUI
        // as they're part of the dynamic content

        btnStartHiking = findViewById(R.id.btn_start_hiking);

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

        btnStartHiking.setOnClickListener(v -> startHiking());
    }

    private void showLoading(boolean isLoading) {
        View loadingIndicator = findViewById(R.id.loading_indicator);
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        // Disable UI elements during loading
        if (isLoading) {
            routeName.setText("Loading...");
            btnStartHiking.setEnabled(false);

            // Hide content during loading
            findViewById(R.id.route_map).setVisibility(View.INVISIBLE);
            findViewById(R.id.gallery_pager).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.route_map).setVisibility(View.VISIBLE);
            findViewById(R.id.gallery_pager).setVisibility(View.VISIBLE);
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

            // Update route name at the top
            routeName.setText(trail.getTrailName());

            // Load map
            ImageView mapView = findViewById(R.id.route_map);
            MapUtil.loadRouteMap(mapView, trail.getImagePath(), this);

            // Update statistics in the upper part (below map)
            TextView difficultyValue = findViewById(R.id.difficulty_value);
            TextView lengthValue = findViewById(R.id.length_value);
            TextView durationValue = findViewById(R.id.duration_value);
            TextView sightValue = findViewById(R.id.sight_value);

            difficultyValue.setText(String.format(Locale.getDefault(), "%.1f", trail.getDifficultyRating()));
            lengthValue.setText(String.format(Locale.getDefault(), "%.1f km", trail.getLengthRating()));
            durationValue.setText(String.format(Locale.getDefault(), "%.1f h", trail.getDurationRating()));
            sightValue.setText(String.format(Locale.getDefault(), "%.1f", trail.getSightRating()));

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

            // Update suggestions in lower part
            if (suggestions != null) {
                suggestions.setText(viewModel.getPersonalizedSuggestions());
            }

            // Enable start hiking button
            btnStartHiking.setEnabled(true);

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