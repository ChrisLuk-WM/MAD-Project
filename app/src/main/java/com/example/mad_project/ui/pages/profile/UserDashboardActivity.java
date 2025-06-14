package com.example.mad_project.ui.pages.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.database.entities.ProfileEntity;
import com.example.mad_project.ui.BaseActivity;
import com.example.mad_project.utils.ImageUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.time.Duration;

public class UserDashboardActivity extends BaseActivity {
    private ExtendedFloatingActionButton fabEmergency;
    private EmergencySheetFragment emergencySheet;
    private BottomSheetBehavior<View> emergencySheetBehavior;
    private Toolbar emergencyToolbar;
    private DashboardViewModel viewModel;

    // Views for last hiking session
    private View lastHikingSessionCard;
    private TextView lastDistanceText;
    private TextView lastDurationText;
    private TextView lastElevationText;

    private TextView userNameText;
    private TextView experienceLevelText;
    private com.google.android.material.imageview.ShapeableImageView profileImage;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_user_dashboard;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        observeData();
    }

    @Override
    protected void initViews() {
        // Emergency button setup
        fabEmergency = findViewById(R.id.fab_emergency);
        setupEmergencyButton();

        userNameText = findViewById(R.id.text_user_name);
        experienceLevelText = findViewById(R.id.text_experience_level);
        profileImage = findViewById(R.id.profile_image);

        // Last hiking session views
        lastHikingSessionCard = findViewById(R.id.card_last_hiking);
        lastDistanceText = findViewById(R.id.text_last_distance);
        lastDurationText = findViewById(R.id.text_last_duration);
        lastElevationText = findViewById(R.id.text_last_elevation);
    }

    private void observeData() {
        // Observe profile data
        viewModel.getCurrentProfile().observe(this, profile -> {
            if (profile != null) {
                updateProfileUI(profile);
            }
        });

        // Observe last hiking session
        viewModel.getLastSession().observe(this, session -> {
            if (session != null) {
                updateLastHikingSessionUI(session);
            } else {
                hideLastHikingSession();
            }
        });
    }

    private void updateProfileUI(ProfileEntity profile) {
        userNameText.setText(profile.getName());
        experienceLevelText.setText(profile.getExperienceLevel());

        // Load profile image if exists
        if (profile.getProfilePhotoPath() != null) {
            Bitmap photo = ImageUtils.loadProfilePhoto(this, profile.getProfilePhotoPath());
            if (photo != null) {
                profileImage.setImageBitmap(photo);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile);
            }
        } else {
            profileImage.setImageResource(R.drawable.ic_profile);
        }
    }
    private void updateLastHikingSessionUI(HikingSessionEntity session) {
        lastHikingSessionCard.setVisibility(View.VISIBLE);
        lastDistanceText.setText(String.format("%.1f km", session.getDistance()));

        // Calculate duration between start and end time
        if (session.getStartTime() != null && session.getEndTime() != null) {
            Duration duration = Duration.between(session.getStartTime(), session.getEndTime());
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            lastDurationText.setText(String.format("%dh %dm", hours, minutes));
        } else {
            lastDurationText.setText("--");
        }

        lastElevationText.setText(String.format("%dm", session.getTotalElevationGain()));
    }

    private void hideLastHikingSession() {
        lastHikingSessionCard.setVisibility(View.GONE);
    }

    private void setupEmergencyButton() {
        fabEmergency.setOnClickListener(v -> showEmergencySheet());
    }

    private void showEmergencySheet() {
        emergencySheet = EmergencySheetFragment.newInstance();
        emergencySheet.setOnDismissListener(dialog -> fabEmergency.show());
        emergencySheet.show(getSupportFragmentManager(), "emergency_sheet");
        fabEmergency.hide();
    }

    private void hideEmergencySheet() {
        if (emergencySheetBehavior != null) {
            emergencySheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public void onBackPressed() {
        if (emergencySheet != null && emergencySheet.isVisible()) {
            emergencySheet.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void setupActions() {
        // Setup any click listeners or other actions
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit_profile) {
            startActivity(new Intent(this, MyProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}