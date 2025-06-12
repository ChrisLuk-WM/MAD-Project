package com.example.mad_project.statistics.dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mad_project.R;
import com.example.mad_project.api.WeatherRepository;
import com.example.mad_project.api.models.CurrentWeather;
import com.example.mad_project.content_downloader.WeatherIconDownloader;
import com.example.mad_project.services.WeatherService;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.ui.pages.home.card.CardHandler;
import com.example.mad_project.ui.pages.home.card.WeatherCardElements;
import com.example.mad_project.utils.WeatherWarningUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.example.mad_project.utils.Common.dpToPx;

public class WeatherCardHandler implements CardHandler, WeatherService.WeatherUpdateListener {
    private Context context;
    private WeatherService weatherService;
    private WeatherIconDownloader iconDownloader;

    // UI References (weak references to prevent memory leaks)
    private final WeakReference<ImageView> weatherIcon;
    private final WeakReference<TextView> temperatureText;
    private final WeakReference<TextView> humidityText;
    private final WeakReference<TextView> uvIndexText;
    private final WeakReference<TextView> districtText;
    private final WeakReference<ImageButton> reloadButton;
    private final WeakReference<MaterialButton> hikingConditionChip;
    private final WeakReference<LinearLayout> warningMessagesContainer;
    private final WeakReference<View> loadingView;
    private final WeakReference<View> weatherContainer;

    public WeatherCardHandler(WeatherCardElements elements) {
        this.weatherIcon = new WeakReference<>(elements.getWeatherIcon());
        this.temperatureText = new WeakReference<>(elements.getTemperatureText());
        this.humidityText = new WeakReference<>(elements.getHumidityText());
        this.uvIndexText = new WeakReference<>(elements.getUvIndexText());
        this.districtText = new WeakReference<>(elements.getDistrictText());
        this.reloadButton = new WeakReference<>(elements.getReloadButton());
        this.hikingConditionChip = new WeakReference<>(elements.getHikingConditionChip());
        this.warningMessagesContainer = new WeakReference<>(elements.getWarningMessagesContainer());
        this.loadingView = new WeakReference<>(elements.getLoadingView());
        this.weatherContainer = new WeakReference<>(elements.getContainerView());
    }

    @Override
    public void initialize(Context context) {
        this.context = context;
        weatherService = WeatherService.getInstance(context);
        iconDownloader = WeatherIconDownloader.getInstance(context);

        weatherService.addListener(this);
        setupReloadButton();

        // Initial data fetch
        showLoading(true);
        fetchWeatherData();
    }

    @Override
    public void cleanup() {
        if (weatherService != null) {
            weatherService.removeListener(this);
        }
        if (iconDownloader != null) {
            iconDownloader.shutdown();
        }
    }

    private void setupReloadButton() {
        ImageButton button = reloadButton.get();
        if (button != null) {
            button.setOnClickListener(v -> {
                button.animate()
                        .rotationBy(360f)
                        .setDuration(1000)
                        .start();
                showLoading(true);
                fetchWeatherData();
            });
        }
    }

    private void fetchWeatherData() {
        weatherService.fetchWeatherData(new WeatherRepository.WeatherCallback<CurrentWeather>() {
            @Override
            public void onSuccess(CurrentWeather result) {
                updateWeatherViews(result);
                showLoading(false);
            }

            @Override
            public void onError(String error) {
                loadTemplateData();
                showError(error);
                showLoading(false);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        View loadingView = this.loadingView.get();
        View contentView = this.weatherContainer.get();
        ImageButton reloadBtn = this.reloadButton.get();

        if (loadingView != null) {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        if (reloadBtn != null) {
            reloadBtn.setEnabled(!isLoading);
            reloadBtn.setAlpha(isLoading ? 0.5f : 1.0f);
        }

        if (contentView != null) {
            contentView.setAlpha(isLoading ? 0.5f : 1.0f);
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateWeatherViews(CurrentWeather weather) {
        String currentDistrict = StatisticsManager.getInstance().getDistrict();
        String nearestStation = StatisticsManager.getInstance().getNearestWeatherStation();

        // Update district
        TextView district = districtText.get();
        if (district != null) {
            district.setText(currentDistrict);
        }

        // Update temperature
        if (weather.getTemperature() != null && !weather.getTemperature().getData().isEmpty()) {
            CurrentWeather.TemperatureRecord stationTemp = weather.getTemperature().getData().stream()
                    .filter(t -> t.getPlace().equals(nearestStation))
                    .findFirst()
                    .orElse(weather.getTemperature().getData().get(0));

            TextView tempText = temperatureText.get();
            if (tempText != null) {
                tempText.setText(String.format("%d°%s",
                        stationTemp.getValue(),
                        stationTemp.getUnit()));
            }
        }

        // Update humidity
        if (weather.getHumidity() != null && !weather.getHumidity().getData().isEmpty()) {
            CurrentWeather.HumidityRecord humidity = weather.getHumidity().getData().get(0);
            TextView humidText = humidityText.get();
            if (humidText != null) {
                humidText.setText(String.format("Humidity: %d%s",
                        humidity.getValue(),
                        humidity.getUnit().equals("percent") ? "%" : humidity.getUnit()));
            }
        }

        // Update UV index
        TextView uvText = uvIndexText.get();
        if (uvText != null) {
            if (weather.getUvindex() != null && !weather.getUvindex().getData().isEmpty()) {
                CurrentWeather.UVIndexRecord uvRecord = weather.getUvindex().getData().get(0);
                uvText.setText(String.format("UV Index: %.1f (%s)",
                        uvRecord.getValue(),
                        uvRecord.getDesc()));
                uvText.setVisibility(View.VISIBLE);
            } else {
                uvText.setVisibility(View.GONE);
            }
        }

        // Update weather icon
        if (weather.getIcon() != null && !weather.getIcon().isEmpty()) {
            updateWeatherIcon(weather.getIcon().get(0));
        }

        // Update warning messages
        if (weather.getWarningMessage() != null) {
            List<WeatherWarningUtils.WarningInfo> warningInfos =
                    WeatherWarningUtils.parseWarnings(weather.getWarningMessage());
            updateWarningMessagesWithInfo(warningInfos);
        }
    }

    private void updateWeatherIcon(int iconCode) {
        ImageView icon = weatherIcon.get();
        if (icon == null) return;

        WeatherIconDownloader.WeatherIconInfo iconInfo = iconDownloader.getWeatherIcon(iconCode);
        if (iconInfo != null) {
            icon.setImageBitmap(iconInfo.getIconBitmap());
        } else {
            icon.setImageResource(R.drawable.ic_unknown);
            iconDownloader.updateWeatherIcons(new WeatherIconDownloader.OnUpdateCompleteListener() {
                @Override
                public void onUpdateComplete(boolean success, String error) {
                    if (success) {
                        WeatherIconDownloader.WeatherIconInfo updatedInfo =
                                iconDownloader.getWeatherIcon(iconCode);
                        if (updatedInfo != null && icon != null) {
                            icon.post(() -> icon.setImageBitmap(updatedInfo.getIconBitmap()));
                        }
                    }
                }
            });
        }
    }

    private void loadTemplateData() {
        ImageView icon = weatherIcon.get();
        TextView temp = temperatureText.get();
        TextView humid = humidityText.get();
        MaterialButton hikingChip = hikingConditionChip.get();

        if (icon != null) icon.setImageResource(R.drawable.ic_weather_sunny);
        if (temp != null) temp.setText("24°C");
        if (humid != null) humid.setText("Humidity: 65%");
        if (hikingChip != null) hikingChip.setText("Good conditions for hiking");
    }

    private void showError(String error) {
        Log.d("WeatherCardHandler", "Error: " + error);
    }

    @Override
    public void onWeatherUpdated(CurrentWeather weather) {
        updateWeatherViews(weather);
    }

    @Override
    public void onWarningsUpdated(List<WeatherWarningUtils.WarningInfo> warnings) {
        updateWarningMessagesWithInfo(warnings);
    }

    @Override
    public void onError(String error) {
        loadTemplateData();
        showError(error);
    }

    private void updateWarningMessagesWithInfo(List<WeatherWarningUtils.WarningInfo> warningInfos) {
        LinearLayout container = warningMessagesContainer.get();
        if (container == null) return;

        container.removeAllViews();

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
                MaterialCardView iconCard = createWarningIconCard(context, warning.getLevel());
                warningIconsContainer.addView(iconCard);
            }

            container.addView(warningIconsContainer);

            // Add reminder card
            MaterialCardView reminderCard = createReminderCard(context, warningInfos);
            container.addView(reminderCard);

            // Update hiking advice
            MaterialButton hikingChip = hikingConditionChip.get();
            if (hikingChip != null) {
                String hikingAdvice = WeatherWarningUtils.getHikingAdvice(warningInfos);
                boolean isRecommended = WeatherWarningUtils.isHikingRecommended(warningInfos);

                hikingChip.setText(hikingAdvice);
                hikingChip.setBackgroundTintList(
                        ColorStateList.valueOf(isRecommended ?
                                Color.parseColor("#4CAF50") : // Green
                                Color.parseColor("#F44336")   // Red
                        )
                );
            }
        }
    }

    private MaterialCardView createWarningIconCard(Context context, String warningText) {
        MaterialCardView iconCard = new MaterialCardView(context);
        LinearLayout.LayoutParams iconCardParams = new LinearLayout.LayoutParams(
                dpToPx(context, 48),
                dpToPx(context, 48)
        );
        iconCardParams.setMargins(dpToPx(context, 4), 0, dpToPx(context, 4), 0);
        iconCard.setLayoutParams(iconCardParams);
        iconCard.setCardElevation(0);
        iconCard.setRadius(dpToPx(context, 8));
        iconCard.setCardBackgroundColor(Color.WHITE);

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
}