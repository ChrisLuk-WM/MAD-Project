package com.example.mad_project.ui.statistics;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import com.example.mad_project.R;
import com.example.mad_project.ui.BaseActivity;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.statistics.StatisticsType;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class StatisticsActivity extends BaseActivity {
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private StatisticsManager statisticsManager;

    // UI elements
    private TextView textDuration;
    private TextView textDistance;
    private TextView textSteps;
    private TextView textSpeed;
    private TextView textElevation;
    private TextView textElevationGain;
    private TextView textLatitude;
    private TextView textLongitude;
    private TextView textAccuracy;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_statistics;
    }

    @Override
    protected void initViews() {
        textDuration = findViewById(R.id.text_duration);
        textDistance = findViewById(R.id.text_distance);
        textSteps = findViewById(R.id.text_steps);
        textSpeed = findViewById(R.id.text_speed);
        textElevation = findViewById(R.id.text_elevation);
        textElevationGain = findViewById(R.id.text_elevation_gain);
        textLatitude = findViewById(R.id.text_latitude);
        textLongitude = findViewById(R.id.text_longitude);
        textAccuracy = findViewById(R.id.text_accuracy);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statisticsManager = StatisticsManager.getInstance();
        setupRefreshHandler();
    }

    private void setupRefreshHandler() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                updateStatistics();
                refreshHandler.postDelayed(this, 1000); // Update every second
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHandler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void updateStatistics() {
        // Update duration
        long duration = statisticsManager.getSessionDuration();
        String durationText = formatDuration(duration);
        textDuration.setText(durationText);

        // Update distance
        double distance = statisticsManager.getValue(StatisticsType.TOTAL_DISTANCE);
        textDistance.setText(String.format(Locale.getDefault(), "%.2f km", distance / 1000));

        // Update steps
        int steps = statisticsManager.getValue(StatisticsType.STEPS);
        textSteps.setText(String.valueOf(steps));

        // Update speed
        double speed = statisticsManager.getValue(StatisticsType.SPEED);
        textSpeed.setText(String.format(Locale.getDefault(), "%.1f km/h", speed * 3.6));

        // Update elevation
        double altitude = statisticsManager.getValue(StatisticsType.ALTITUDE);
        textElevation.setText(String.format(Locale.getDefault(), "%.1f m", altitude));

        // Update elevation gain
        double elevationGain = statisticsManager.getValue(StatisticsType.TOTAL_ELEVATION_GAIN);
        textElevationGain.setText(String.format(Locale.getDefault(), "%.1f m", elevationGain));

        // Update location
        Location location = statisticsManager.getValue(StatisticsType.LOCATION);
        if (location != null) {
            textLatitude.setText(String.format(Locale.getDefault(), "%.6f", location.getLatitude()));
            textLongitude.setText(String.format(Locale.getDefault(), "%.6f", location.getLongitude()));
            textAccuracy.setText(String.format(Locale.getDefault(), "%.1f m", location.getAccuracy()));
        }
    }

    private String formatDuration(long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    protected void setupActions() {
        // No actions needed for now
    }
}