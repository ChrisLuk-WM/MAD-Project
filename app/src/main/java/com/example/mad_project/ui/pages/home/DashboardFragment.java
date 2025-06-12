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
import android.widget.ImageButton;
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
import com.example.mad_project.services.WeatherService;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.utils.WeatherWarningUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

public class DashboardFragment extends Fragment implements WeatherService.WeatherUpdateListener  {
    // Weather section
    private ImageView weatherIcon;
    private TextView temperatureText;
    private TextView humidityText;
    private TextView uvIndexText;
    private TextView districtText;
    private ImageButton reloadButton;
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
    private WeatherService weatherService;

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
        weatherService = WeatherService.getInstance(requireContext());
        weatherService.addListener(this);

        // Check if there's existing weather data
        showLoading(true);
        fetchWeatherData();
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

        reloadButton = view.findViewById(R.id.button_reload_weather);
        reloadButton.setOnClickListener(v -> {
            // Animate the button
            reloadButton.animate()
                    .rotationBy(360f)
                    .setDuration(1000)
                    .start();
            // Fetch weather data
            showLoading(true);
            fetchWeatherData();
        });

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

    private void showLoading(boolean isLoading) {
        if (getView() == null) return;

        View loadingView = getView().findViewById(R.id.loading_view);
        if (loadingView != null) {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        // Disable reload button while loading
        if (reloadButton != null) {
            reloadButton.setEnabled(!isLoading);
            reloadButton.setAlpha(isLoading ? 0.5f : 1.0f);
        }

        // Optionally dim the content while loading
        View contentView = getView().findViewById(R.id.weather_container);
        if (contentView != null) {
            contentView.setAlpha(isLoading ? 0.5f : 1.0f);
        }
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
            uvIndexText.setText(String.format("UV Index: %.1f (%s)",  // Changed from %d to %.1f
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

            // Create container for warning icons
            LinearLayout warningIconsContainer = new LinearLayout(context);
            warningIconsContainer.setOrientation(LinearLayout.HORIZONTAL);
            warningIconsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            warningIconsContainer.setPadding(0, dpToPx(context, 4), 0, dpToPx(context, 4));

            // Add warning icons
            for (WeatherWarningUtils.WarningInfo warning : warningInfos) {
                // Get clean warning text
                String warningText = warning.getLevel(); // Get only the first sentence

                // Create icon container
                MaterialCardView iconCard = new MaterialCardView(context);
                LinearLayout.LayoutParams iconCardParams = new LinearLayout.LayoutParams(
                        dpToPx(context, 48), // Fixed width
                        dpToPx(context, 48)  // Fixed height
                );
                iconCardParams.setMargins(
                        dpToPx(context, 4),
                        0,
                        dpToPx(context, 4),
                        0
                );
                iconCard.setLayoutParams(iconCardParams);
                iconCard.setCardElevation(0);
                iconCard.setRadius(dpToPx(context, 8));
                iconCard.setCardBackgroundColor(Color.WHITE);

                // Create and setup ImageView
                ImageView warningIcon = new ImageView(context);
                warningIcon.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                warningIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                warningIcon.setPadding(
                        dpToPx(context, 4),
                        dpToPx(context, 4),
                        dpToPx(context, 4),
                        dpToPx(context, 4)
                );

                // Load warning icon
                WeatherIconDownloader.WeatherIconInfo iconInfo = iconDownloader.getWarningIcon(warningText);
                if (iconInfo != null) {
                    warningIcon.setImageBitmap(iconInfo.getIconBitmap());
                }

                iconCard.addView(warningIcon);
                warningIconsContainer.addView(iconCard);
            }

            warningMessagesContainer.addView(warningIconsContainer);

            // Create integrated reminder if there are warnings
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

                // Add reminders
                LinearLayout reminderContent = new LinearLayout(context);
                reminderContent.setOrientation(LinearLayout.VERTICAL);
                reminderContent.setPadding(
                        dpToPx(context, 16),
                        dpToPx(context, 12),
                        dpToPx(context, 16),
                        dpToPx(context, 12)
                );

                TextView titleText = new TextView(context);
                titleText.setText("Safety Reminders");
                titleText.setTypeface(titleText.getTypeface(), Typeface.BOLD);
                titleText.setTextColor(Color.parseColor("#C62828")); // Dark red

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

            // Update hiking advice
            String hikingAdvice = WeatherWarningUtils.getHikingAdvice(warningInfos);
            boolean isRecommended = WeatherWarningUtils.isHikingRecommended(warningInfos);

            hikingConditionChip.setText(hikingAdvice);
            hikingConditionChip.setBackgroundTintList(
                    ColorStateList.valueOf(isRecommended ?
                            Color.parseColor("#4CAF50") : // Green
                            Color.parseColor("#F44336")   // Red
                    )
            );
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
        weatherService.removeListener(this);
        if (iconDownloader != null) {
            iconDownloader.shutdown();
        }
    }

    @Override
    public void onWeatherUpdated(CurrentWeather weather) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            showLoading(false);
            updateWeatherViews(weather);
        });
    }

    @Override
    public void onWarningsUpdated(List<WeatherWarningUtils.WarningInfo> warnings) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            updateWarningMessagesWithInfo(warnings);
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

    private void updateWarningMessagesWithInfo(List<WeatherWarningUtils.WarningInfo> warningInfos) {
        warningMessagesContainer.removeAllViews();
        Context context = requireContext();

        if (!warningInfos.isEmpty()) {
            // Create container for warning icons
            LinearLayout warningIconsContainer = new LinearLayout(context);
            warningIconsContainer.setOrientation(LinearLayout.HORIZONTAL);
            warningIconsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            warningIconsContainer.setPadding(0, dpToPx(context, 4), 0, dpToPx(context, 4));

            // Add warning icons
            for (WeatherWarningUtils.WarningInfo warning : warningInfos) {
                // Get clean warning text
                String warningText = warning.getLevel();

                // Create and add icon (keep existing icon creation code)
                MaterialCardView iconCard = createWarningIconCard(context, warningText);
                warningIconsContainer.addView(iconCard);
            }

            warningMessagesContainer.addView(warningIconsContainer);

            // Add reminder card if there are warnings
            if (!warningInfos.isEmpty()) {
                MaterialCardView reminderCard = createReminderCard(context, warningInfos);
                warningMessagesContainer.addView(reminderCard);
            }

            // Update hiking advice
            String hikingAdvice = WeatherWarningUtils.getHikingAdvice(warningInfos);
            boolean isRecommended = WeatherWarningUtils.isHikingRecommended(warningInfos);

            hikingConditionChip.setText(hikingAdvice);
            hikingConditionChip.setBackgroundTintList(
                    ColorStateList.valueOf(isRecommended ?
                            Color.parseColor("#4CAF50") : // Green
                            Color.parseColor("#F44336")   // Red
                    )
            );
        }
    }

    private MaterialCardView createWarningIconCard(Context context, String warningText) {
        MaterialCardView iconCard = new MaterialCardView(context);
        LinearLayout.LayoutParams iconCardParams = new LinearLayout.LayoutParams(
                dpToPx(context, 48), // Fixed width
                dpToPx(context, 48)  // Fixed height
        );
        iconCardParams.setMargins(
                dpToPx(context, 4),
                0,
                dpToPx(context, 4),
                0
        );
        iconCard.setLayoutParams(iconCardParams);
        iconCard.setCardElevation(0);
        iconCard.setRadius(dpToPx(context, 8));
        iconCard.setCardBackgroundColor(Color.WHITE);

        // Create and setup ImageView
        ImageView warningIcon = new ImageView(context);
        warningIcon.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        warningIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        warningIcon.setPadding(
                dpToPx(context, 4),
                dpToPx(context, 4),
                dpToPx(context, 4),
                dpToPx(context, 4)
        );

        // Load warning icon
        WeatherIconDownloader.WeatherIconInfo iconInfo = iconDownloader.getWarningIcon(warningText);
        if (iconInfo != null) {
            warningIcon.setImageBitmap(iconInfo.getIconBitmap());
        }

        iconCard.addView(warningIcon);
        return iconCard;
    }

    private MaterialCardView createReminderCard(Context context, List<WeatherWarningUtils.WarningInfo> warnings) {
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

        // Add reminders
        LinearLayout reminderContent = new LinearLayout(context);
        reminderContent.setOrientation(LinearLayout.VERTICAL);
        reminderContent.setPadding(
                dpToPx(context, 16),
                dpToPx(context, 12),
                dpToPx(context, 16),
                dpToPx(context, 12)
        );

        TextView titleText = new TextView(context);
        titleText.setText("Safety Reminders");
        titleText.setTypeface(titleText.getTypeface(), Typeface.BOLD);
        titleText.setTextColor(Color.parseColor("#C62828")); // Dark red

        TextView reminderText = new TextView(context);
        StringBuilder reminders = new StringBuilder();
        for (WeatherWarningUtils.WarningInfo warning : warnings) {
            reminders.append("• ").append(warning.getReminder()).append("\n");
        }
        reminderText.setText(reminders.toString().trim());
        reminderText.setTextColor(Color.parseColor("#C62828")); // Dark red
        reminderText.setPadding(0, dpToPx(context, 4), 0, 0);

        reminderContent.addView(titleText);
        reminderContent.addView(reminderText);
        reminderCard.addView(reminderContent);

        return reminderCard;
    }

    private void fetchWeatherData() {
        weatherService.fetchWeatherData(new WeatherRepository.WeatherCallback<CurrentWeather>() {
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
}