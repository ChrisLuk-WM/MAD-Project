package com.example.mad_project.ui.pages.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mad_project.R;
import com.example.mad_project.api.WeatherRepository;
import com.example.mad_project.api.models.CurrentWeather;
import com.example.mad_project.content_downloader.WeatherIconDownloader;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class DashboardFragment extends Fragment {
    // Weather section
    private ImageView weatherIcon;
    private TextView temperatureText;
    private TextView weatherConditionText;
    private TextView humidityText;
    private TextView windText;
    private Chip hikingConditionChip;

    // Activity section
    private LinearProgressIndicator stepsProgress;
    private TextView stepsText;
    private TextView stepsGoalText;
    private TextView distanceText;
    private TextView caloriesText;
    private TextView activeTimeText;

    // Health section
    private TextView healthInsightText;
    private WeatherIconDownloader iconDownloader;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        loadTemplateData(); // This will be replaced with real data implementation

        iconDownloader = WeatherIconDownloader.getInstance(requireContext());

        // Start real data updates
        updateWeatherInfo();
        startWeatherUpdates();
    }

    private void initializeViews(View view) {
        // Weather section
        weatherIcon = view.findViewById(R.id.weather_icon);
        temperatureText = view.findViewById(R.id.text_temperature);
        weatherConditionText = view.findViewById(R.id.text_weather_condition);
        humidityText = view.findViewById(R.id.text_humidity);
        windText = view.findViewById(R.id.text_wind);
        hikingConditionChip = view.findViewById(R.id.chip_hiking_condition);

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
        weatherConditionText.setText("Sunny");
        humidityText.setText("Humidity: 65%");
        windText.setText("Wind: 5 km/h");
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

        WeatherRepository repository = new WeatherRepository();
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
        if (weather.getTemperature() != null &&
                !weather.getTemperature().getData().isEmpty()) {
            CurrentWeather.TemperatureRecord hkoTemp = weather.getTemperature().getData().stream()
                    .filter(t -> t.getPlace().equals("Hong Kong Observatory"))
                    .findFirst()
                    .orElse(weather.getTemperature().getData().get(0));

            temperatureText.setText(String.format("%d°%s",
                    hkoTemp.getValue(),
                    hkoTemp.getUnit()));
        }

        if (weather.getHumidity() != null &&
                !weather.getHumidity().getData().isEmpty()) {
            CurrentWeather.HumidityRecord humidity = weather.getHumidity().getData().get(0);
            humidityText.setText(String.format("Humidity: %d%s",
                    humidity.getValue(),
                    humidity.getUnit().equals("percent") ? "%" : humidity.getUnit()));
        }

        if (weather.getIcon() != null && !weather.getIcon().isEmpty()) {
            int iconCode = weather.getIcon().get(0);
            WeatherIconDownloader.WeatherIconInfo iconInfo = iconDownloader.getWeatherIcon(iconCode);

            if (iconInfo != null) {
                weatherIcon.setImageBitmap(iconInfo.getIconBitmap());
                weatherConditionText.setText(iconInfo.getCaption());
            } else {
                // Fallback to default icon while downloading
                weatherIcon.setImageResource(R.drawable.ic_unknown);
                // Start downloading icons if not available
                iconDownloader.updateWeatherIcons(new WeatherIconDownloader.OnUpdateCompleteListener() {
                    @Override
                    public void onUpdateComplete(boolean success, String error) {
                        if (success && isAdded()) {
                            // Try setting the icon again after download
                            WeatherIconDownloader.WeatherIconInfo updatedInfo =
                                    iconDownloader.getWeatherIcon(iconCode);
                            if (updatedInfo != null) {
                                requireActivity().runOnUiThread(() -> {
                                    weatherIcon.setImageBitmap(updatedInfo.getIconBitmap());
                                    weatherConditionText.setText(updatedInfo.getCaption());
                                });
                            }
                        }
                    }
                });
            }
        }
    }

    private void showError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
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