package com.example.mad_project.ui.pages.home;

import static com.example.mad_project.utils.Common.dpToPx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mad_project.R;
import com.example.mad_project.api.WeatherRepository;
import com.example.mad_project.api.models.CurrentWeather;
import com.example.mad_project.content_downloader.WeatherIconDownloader;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.utils.WeatherWarningUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

public class DashboardFragment extends Fragment {
    // Weather section
    private ImageView weatherIcon;
    private TextView temperatureText;
    private TextView humidityText;
    private TextView uvIndexText;
    private TextView districtText;
    private MaterialButton hikingConditionChip;
    private LinearLayout warningMessagesContainer;
    private WeatherIconDownloader iconDownloader;

    // Activity section
    private LinearProgressIndicator stepsProgress;
    private TextView stepsText;
    private TextView stepsGoalText;
    private TextView distanceText;
    private TextView caloriesText;
    private TextView activeTimeText;

    // Health section
    private TextView healthInsightText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);

        iconDownloader = WeatherIconDownloader.getInstance(requireContext());

        // Start real data updates
        updateWeatherInfo();
        startWeatherUpdates();
    }

    private void initializeViews(View view) {
        // Weather section
        weatherIcon = view.findViewById(R.id.weather_icon);
        temperatureText = view.findViewById(R.id.text_temperature);
        humidityText = view.findViewById(R.id.text_humidity);
        uvIndexText = view.findViewById(R.id.text_uv_index);
        districtText = view.findViewById(R.id.text_district);
        hikingConditionChip = view.findViewById(R.id.chip_hiking_condition);
        hikingConditionChip.setEnabled(false); // Disable the button
        hikingConditionChip.setStateListAnimator(null); // Remove click animation
        warningMessagesContainer = view.findViewById(R.id.warning_messages_container);

        // Activity section
        stepsProgress = view.findViewById(R.id.progress_steps);
        stepsText = view.findViewById(R.id.text_steps);
        stepsGoalText = view.findViewById(R.id.text_steps_goal);
        distanceText = view.findViewById(R.id.text_distance);
        caloriesText = view.findViewById(R.id.text_calories);
        activeTimeText = view.findViewById(R.id.text_active_time);

        // Health section
        healthInsightText = view.findViewById(R.id.text_health_insight);
    }

    private void loadTemplateData() {
        // Template weather data
        weatherIcon.setImageResource(R.drawable.ic_weather_sunny);
        temperatureText.setText("24°C");
        humidityText.setText("Humidity: 65%");
        hikingConditionChip.setText("Good conditions for hiking");

        // Template activity data
        stepsProgress.setProgress(65);
        stepsText.setText("6,500 steps");
        stepsGoalText.setText("Goal: 10,000");
        distanceText.setText("4.2 km");
        caloriesText.setText("320");
        activeTimeText.setText("45 min");

        // Template health insight
        healthInsightText.setText("Perfect weather for a hike! Consider taking advantage of the mild temperature and clear skies.");
    }

    // Add periodic updates
    private void startWeatherUpdates() {
        // Create a handler for periodic updates
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable weatherUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updateWeatherInfo();
                // Schedule next update in 10 minutes
                handler.postDelayed(this, 10 * 1000);
            }
        };

        // Start periodic updates
        handler.post(weatherUpdateRunnable);
    }

    // These methods will be implemented later with real data
    private void updateWeatherInfo() {
        showLoading(true);

        WeatherRepository repository = new WeatherRepository(requireContext());
        repository.getCurrentWeather(new WeatherRepository.WeatherCallback<CurrentWeather>() {

            @Override
            public void onSuccess(CurrentWeather result) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    updateWeatherViews(result);
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    loadTemplateData();
                    showError(error);
                });
            }
        });
    }

    private void showLoading(boolean isLoading) {
        // Assuming you have a ProgressBar in your layout
//        if (progressBar != null) {
//            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
//        }
    }

    @SuppressLint("DefaultLocale")
    private void updateWeatherViews(CurrentWeather weather) {
        // Update district
        String currentDistrict = StatisticsManager.getInstance().getDistrict();
        districtText.setText(currentDistrict);

        // Get nearest weather station
        String nearestStation = StatisticsManager.getInstance().getNearestWeatherStation();

        // Update temperature
        if (weather.getTemperature() != null && !weather.getTemperature().getData().isEmpty()) {
            CurrentWeather.TemperatureRecord stationTemp = weather.getTemperature().getData().stream()
                    .filter(t -> t.getPlace().equals(nearestStation))
                    .findFirst()
                    .orElse(weather.getTemperature().getData().get(0));

            temperatureText.setText(String.format("%d°%s",
                    stationTemp.getValue(),
                    stationTemp.getUnit()));
        }

        // Update humidity
        if (weather.getHumidity() != null && !weather.getHumidity().getData().isEmpty()) {
            CurrentWeather.HumidityRecord humidity = weather.getHumidity().getData().get(0);
            humidityText.setText(String.format("Humidity: %d%s",
                    humidity.getValue(),
                    humidity.getUnit().equals("percent") ? "%" : humidity.getUnit()));
        }

        // Update UV index
        if (weather.getUvindex() != null && !weather.getUvindex().getData().isEmpty()) {
            CurrentWeather.UVIndexRecord uvRecord = weather.getUvindex().getData().get(0);
            uvIndexText.setText(String.format("UV Index: %d (%s)",
                    uvRecord.getValue(),
                    uvRecord.getDesc()));
        } else {
            uvIndexText.setVisibility(View.GONE);
        }

        // Update weather icon
        if (weather.getIcon() != null && !weather.getIcon().isEmpty()) {
            updateWeatherIcon(weather.getIcon().get(0));
        }

        // Update warning messages
        updateWarningMessages(weather.getWarningMessage());

        // Update hiking conditions
        // updateHikingConditions(weather);
    }

    private void updateWarningMessages(List<String> warnings) {
        warningMessagesContainer.removeAllViews();
        Context context = requireContext();

        if (warnings != null && !warnings.isEmpty()) {
            List<WeatherWarningUtils.WarningInfo> warningInfos =
                    WeatherWarningUtils.parseWarnings(warnings);

            // Group warnings by severity to determine the color
            int highestSeverity = warningInfos.stream()
                    .mapToInt(WeatherWarningUtils.WarningInfo::getSeverity)
                    .max()
                    .orElse(1);

            // Display original warnings as short titles
            for (String originalWarning : warnings) {
                MaterialCardView warningCard = new MaterialCardView(context);

                // Get color based on highest severity
                int[] colors = getWarningColors(highestSeverity);

                warningCard.setCardBackgroundColor(colors[0]);
                warningCard.setStrokeColor(colors[1]);
                warningCard.setStrokeWidth(2);
                warningCard.setCardElevation(0);
                warningCard.setRadius(dpToPx(context, 8));

                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, dpToPx(context, 4), 0, dpToPx(context, 4));
                warningCard.setLayoutParams(cardParams);

                TextView warningText = new TextView(context);
                warningText.setText(originalWarning.split("\\.")[0]); // Get only the first sentence
                warningText.setTextColor(colors[2]);
                warningText.setTypeface(warningText.getTypeface(), Typeface.BOLD);
                warningText.setPadding(
                        dpToPx(context, 16),
                        dpToPx(context, 12),
                        dpToPx(context, 16),
                        dpToPx(context, 12)
                );

                warningCard.addView(warningText);
                warningMessagesContainer.addView(warningCard);
            }

            // Create integrated reminder
            if (!warningInfos.isEmpty()) {
                MaterialCardView reminderCard = new MaterialCardView(context);

                reminderCard.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Light red
                reminderCard.setStrokeColor(Color.parseColor("#EF5350")); // Red
                reminderCard.setStrokeWidth(2);
                reminderCard.setCardElevation(0);
                reminderCard.setRadius(dpToPx(context, 8));

                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, dpToPx(context, 8), 0, dpToPx(context, 4));
                reminderCard.setLayoutParams(cardParams);

                LinearLayout reminderContent = new LinearLayout(context);
                reminderContent.setOrientation(LinearLayout.VERTICAL);
                reminderContent.setPadding(
                        dpToPx(context, 16),
                        dpToPx(context, 12),
                        dpToPx(context, 16),
                        dpToPx(context, 12)
                );

                // Title
                TextView titleText = new TextView(context);
                titleText.setText("Safety Reminders");
                titleText.setTypeface(titleText.getTypeface(), Typeface.BOLD);
                titleText.setTextColor(Color.parseColor("#C62828")); // Dark red

                // Combined reminders
                TextView reminderText = new TextView(context);
                StringBuilder reminders = new StringBuilder();
                for (WeatherWarningUtils.WarningInfo warning : warningInfos) {
                    reminders.append("• ").append(warning.getReminder()).append("\n");
                }
                reminderText.setText(reminders.toString().trim());
                reminderText.setTextColor(Color.parseColor("#C62828")); // Dark red
                reminderText.setPadding(0, dpToPx(context, 4), 0, 0);

                reminderContent.addView(titleText);
                reminderContent.addView(reminderText);
                reminderCard.addView(reminderContent);
                warningMessagesContainer.addView(reminderCard);
            }

            String hikingAdvice = WeatherWarningUtils.getHikingAdvice(warningInfos);
            boolean isRecommended = WeatherWarningUtils.isHikingRecommended(warningInfos);

            hikingConditionChip.setText(hikingAdvice);
            hikingConditionChip.setBackgroundTintList(
                    ColorStateList.valueOf(isRecommended ?
                            Color.parseColor("#4CAF50") : // Green
                            Color.parseColor("#F44336")   // Red
                    )
            );

// Set the chip's layout parameters to wrap content properly
            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            chipParams.setMargins(
                    dpToPx(context, 4),
                    dpToPx(context, 4),
                    dpToPx(context, 4),
                    dpToPx(context, 4)
            );
            hikingConditionChip.setLayoutParams(chipParams);
        }
    }

    private int[] getWarningColors(int severity) {
        // Returns [backgroundColor, strokeColor, textColor]
        switch (severity) {
            case 5:
                return new int[] {
                        Color.parseColor("#FFEBEE"), // Light red
                        Color.parseColor("#EF5350"), // Red
                        Color.parseColor("#C62828")  // Dark red
                };
            case 4:
                return new int[] {
                        Color.parseColor("#FCE4EC"), // Light pink
                        Color.parseColor("#EC407A"), // Pink
                        Color.parseColor("#880E4F")  // Dark pink
                };
            case 3:
                return new int[] {
                        Color.parseColor("#FFF3E0"), // Light orange
                        Color.parseColor("#FFB74D"), // Orange
                        Color.parseColor("#E65100")  // Dark orange
                };
            case 2:
                return new int[] {
                        Color.parseColor("#FFFDE7"), // Light yellow
                        Color.parseColor("#FDD835"), // Yellow
                        Color.parseColor("#F57F17")  // Dark yellow
                };
            default:
                return new int[] {
                        Color.parseColor("#E8F5E9"), // Light green
                        Color.parseColor("#66BB6A"), // Green
                        Color.parseColor("#2E7D32")  // Dark green
                };
        }
    }

    private void updateWeatherIcon(int iconCode) {
        WeatherIconDownloader.WeatherIconInfo iconInfo = iconDownloader.getWeatherIcon(iconCode);
        if (iconInfo != null) {
            weatherIcon.setImageBitmap(iconInfo.getIconBitmap());
        } else {
            weatherIcon.setImageResource(R.drawable.ic_unknown);
            iconDownloader.updateWeatherIcons(new WeatherIconDownloader.OnUpdateCompleteListener() {
                @Override
                public void onUpdateComplete(boolean success, String error) {
                    if (success && isAdded()) {
                        WeatherIconDownloader.WeatherIconInfo updatedInfo =
                                iconDownloader.getWeatherIcon(iconCode);
                        if (updatedInfo != null) {
                            requireActivity().runOnUiThread(() ->
                                    weatherIcon.setImageBitmap(updatedInfo.getIconBitmap())
                            );
                        }
                    }
                }
            });
        }
    }

    private void showError(String error) {
        Log.d("updateWeather", error);
    }

    private void updateActivityInfo() {
        // TODO: Implement real activity data update
    }

    private void updateHealthInsights() {
        // TODO: Implement real health insights update
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove any pending weather updates
        if (getView() != null) {
            Handler handler = getView().getHandler();
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }

        // Clean up WeatherIconDownloader
        if (iconDownloader != null) {
            iconDownloader.shutdown();
        }
    }
}