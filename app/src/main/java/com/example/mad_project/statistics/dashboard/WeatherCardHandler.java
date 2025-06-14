package com.example.mad_project.statistics.dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.mad_project.R;
import com.example.mad_project.api.WeatherRepository;
import com.example.mad_project.api.models.CurrentWeather;
import com.example.mad_project.api.models.WeatherForecast;
import com.example.mad_project.content_downloader.WeatherIconDownloader;
import com.example.mad_project.sensors.SensorsController;
import com.example.mad_project.services.HikingRecommendationHelper;
import com.example.mad_project.services.WeatherService;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.ui.pages.home.card.CardHandler;
import com.example.mad_project.ui.pages.home.card.WeatherCardElements;
import com.example.mad_project.utils.WeatherWarningUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.example.mad_project.utils.Common.dpToPx;

public class WeatherCardHandler implements CardHandler, WeatherService.WeatherUpdateListener {
    private Context context;
    private WeatherService weatherService;
    private WeatherIconDownloader iconDownloader;
    private WeatherForecast currentForecast;

    // UI References (weak references to prevent memory leaks)
    private final WeakReference<ImageView> weatherIcon;
    private final WeakReference<TextView> temperatureText;
    private final WeakReference<TextView> humidityText;
    private final WeakReference<TextView> uvIndexText;
    private final WeakReference<TextView> districtText;
    private final WeakReference<ImageButton> reloadButton;
    private final WeakReference<LinearLayout> warningMessagesContainer;
    private final WeakReference<View> loadingView;
    private final WeakReference<View> weatherContainer;
    private final WeakReference<ImageButton> infoButton;
    private final WeakReference<MaterialCardView> hikingConditionCard;
    private final WeakReference<TextView> hikingAdviceText;
    private final WeakReference<TextView> hikingConfidenceText;
    private PopupWindow infoPopup;
    private String currentForecastMessage = "";
    private List<WeatherWarningUtils.WarningInfo> currentWarnings = new ArrayList<>();

    private SensorsController sensorsController;

    private HikingRecommendationHelper hikingRecommendationHelper;




    public WeatherCardHandler(WeatherCardElements elements) {
        this.weatherIcon = new WeakReference<>(elements.getWeatherIcon());
        this.temperatureText = new WeakReference<>(elements.getTemperatureText());
        this.humidityText = new WeakReference<>(elements.getHumidityText());
        this.uvIndexText = new WeakReference<>(elements.getUvIndexText());
        this.districtText = new WeakReference<>(elements.getDistrictText());
        this.reloadButton = new WeakReference<>(elements.getReloadButton());
        this.warningMessagesContainer = new WeakReference<>(elements.getWarningMessagesContainer());
        this.loadingView = new WeakReference<>(elements.getLoadingView());
        this.weatherContainer = new WeakReference<>(elements.getContainerView());
        this.infoButton = new WeakReference<>(elements.getInfoButton());
        this.hikingConditionCard = new WeakReference<>(elements.getHikingConditionCard());
        this.hikingAdviceText = new WeakReference<>(elements.getHikingAdviceText());
        this.hikingConfidenceText = new WeakReference<>(elements.getHikingConfidenceText());
    }

    @Override
    public void initialize(Context context) {
        this.context = context;
        weatherService = WeatherService.getInstance(context);
        iconDownloader = WeatherIconDownloader.getInstance(context);
        sensorsController = SensorsController.getInstance(context);
        hikingRecommendationHelper = new HikingRecommendationHelper(context);

        weatherService.addListener(this);
        setupInfoButton();
        setupReloadButton();

        // Initial data fetch
        showLoading(true);
        fetchWeatherData();
    }

    private void setupInfoButton() {
        ImageButton button = infoButton.get();
        if (button != null) {
            button.setOnClickListener(v -> showInfoPopup(v));
        }
    }

    private void showInfoPopup(View anchor) {
        if (infoPopup != null && infoPopup.isShowing()) {
            infoPopup.dismiss();
            return;
        }

        View popupView = LayoutInflater.from(context).inflate(R.layout.layout_weather_info_popup, null);

        // Setup popup content
        TextView forecastText = popupView.findViewById(R.id.text_forecast);
        TextView safetyText = popupView.findViewById(R.id.text_safety);

        // Set forecast text
        forecastText.setText(currentForecastMessage);

        // Set safety reminders
        StringBuilder reminders = new StringBuilder();
        for (WeatherWarningUtils.WarningInfo warning : currentWarnings) {
            reminders.append("• ").append(warning.getReminder()).append("\n");
        }
        safetyText.setText(reminders.toString().trim());

        // Calculate popup width
        int infoButtonLocation[] = new int[2];
        anchor.getLocationInWindow(infoButtonLocation);

        int popupWidth = infoButtonLocation[0] + anchor.getWidth() - dpToPx(context, 16); // Right align with info button

        // Create and show popup
        infoPopup = new PopupWindow(
                popupView,
                popupWidth,  // Custom width
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // Add padding to popup content
        popupView.setPadding(
                dpToPx(context, 16), // Left padding
                dpToPx(context, 8),  // Top padding
                dpToPx(context, 8),  // Right padding
                dpToPx(context, 8)   // Bottom padding
        );

        // Add animation
        infoPopup.setAnimationStyle(android.R.style.Animation_Dialog);

        // Add background dimming
        View root = anchor.getRootView();
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) root.getLayoutParams();
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.dimAmount = 0.3f;

        // Calculate X offset for popup
        int xOffset = dpToPx(context, 16); // Left margin

        // Show popup with offset
        infoPopup.showAsDropDown(anchor, -xOffset, 0);

        // Dismiss when clicking outside
        infoPopup.setOutsideTouchable(true);
        infoPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

    private void updateHikingAdvice(String forecastMessage, List<WeatherWarningUtils.WarningInfo> warnings) {
        MaterialCardView card = hikingConditionCard.get();
        TextView adviceText = hikingAdviceText.get();
        TextView confidenceText = hikingConfidenceText.get();

        if (card == null || adviceText == null || confidenceText == null) return;

        HikingRecommendationHelper.HikingRecommendation recommendation =
                getHikingAdvise(forecastMessage);

        String hikingAdvice;
        String confidence;
        int[] colors;

        if (recommendation != null) {
            hikingAdvice = recommendation.detailedAdvice;
            confidence = String.format("Confidence: %d%%",
                    Math.round(recommendation.confidence * 100));

            // Get dynamic colors based on probabilities and confidence
            colors = getRecommendationColors(
                    recommendation.allProbabilities,
                    recommendation.confidence
            );
        } else {
            hikingAdvice = WeatherWarningUtils.getHikingAdvice(warnings);
            boolean isRecommended = WeatherWarningUtils.isHikingRecommended(warnings);
            confidence = "Based on current weather conditions";

            // Fallback colors when no recommendation available
            colors = getFallbackColors(isRecommended);
        }

        // Update text
        adviceText.setText(hikingAdvice);
        confidenceText.setText(confidence);

        // Update card style with dynamic colors
        card.setCardBackgroundColor(colors[0]);
        card.setStrokeColor(colors[1]);
    }

    private int[] getRecommendationColors(float[] probabilities, float confidence) {
        // Base colors (RGB values)
        int[][] baseColors = {
                {244, 67, 54},   // Red (not recommended)
                {255, 152, 0},   // Orange (caution)
                {76, 175, 80}    // Green (recommended)
        };

        // Get dominant probability and its index
        float maxProb = 0;
        int dominantIndex = 0;
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i];
                dominantIndex = i;
            }
        }

        // Find secondary influence (second highest probability)
        float secondProb = 0;
        int secondaryIndex = 0;
        for (int i = 0; i < probabilities.length; i++) {
            if (i != dominantIndex && probabilities[i] > secondProb) {
                secondProb = probabilities[i];
                secondaryIndex = i;
            }
        }

        // Calculate blended color based on top two probabilities
        float blendRatio = secondProb / (maxProb + secondProb);
        int[] dominantColor = baseColors[dominantIndex];
        int[] secondaryColor = baseColors[secondaryIndex];

        // Blend the colors
        int[] blendedColor = new int[3];
        for (int i = 0; i < 3; i++) {
            blendedColor[i] = Math.round(
                    dominantColor[i] * (1 - blendRatio) + secondaryColor[i] * blendRatio
            );
        }

        // Adjust saturation based on confidence
        float saturationAdjust = 0.5f + (confidence * 0.5f);
        int[] adjustedColor = adjustSaturation(blendedColor, saturationAdjust);

        // Create darker version for stroke
        int[] strokeColor = new int[3];
        float darkening = 0.8f;
        for (int i = 0; i < 3; i++) {
            strokeColor[i] = Math.round(adjustedColor[i] * darkening);
        }

        // Return background and stroke colors
        return new int[] {
                Color.argb(255, adjustedColor[0], adjustedColor[1], adjustedColor[2]),
                Color.argb(255, strokeColor[0], strokeColor[1], strokeColor[2])
        };
    }

    private int[] adjustSaturation(int[] rgb, float saturationFactor) {
        // Convert RGB to HSV
        float[] hsv = new float[3];
        Color.RGBToHSV(rgb[0], rgb[1], rgb[2], hsv);

        // Adjust saturation
        hsv[1] = Math.min(1f, hsv[1] * saturationFactor);

        // Convert back to RGB
        int color = Color.HSVToColor(hsv);
        return new int[] {
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        };
    }

    private int[] getFallbackColors(boolean isRecommended) {
        if (isRecommended) {
            return new int[] {
                    Color.parseColor("#4CAF50"),  // Green background
                    Color.parseColor("#388E3C")   // Dark green stroke
            };
        } else {
            return new int[] {
                    Color.parseColor("#F44336"),  // Red background
                    Color.parseColor("#D32F2F")   // Dark red stroke
            };
        }
    }

    private String combineForecastMessages(WeatherForecast forecast) {
        if (forecast == null) return "";

        StringBuilder message = new StringBuilder();

        // Add forecast description
        if (!forecast.getForecastDesc().isEmpty()) {
            message.append(forecast.getForecastDesc());
        }

        // Add outlook if available
        if (!forecast.getOutlook().isEmpty()) {
            if (message.length() > 0) {
                message.append("\n\n");
            }
            message.append("Outlook: ").append(forecast.getOutlook());
        }

        return message.toString().trim();
    }

    private void fetchWeatherData() {
        sensorsController.getGPSInfo();
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

        // Add forecast fetch
        weatherService.getLocalWeatherForecast(new WeatherRepository.WeatherCallback<WeatherForecast>() {
            @Override
            public void onSuccess(WeatherForecast result) {
                currentForecast = result;
                if (result != null) {
                    String forecastMessage = combineForecastMessages(result);
                    updateWarningMessagesWithInfo(
                            WeatherWarningUtils.parseWarnings(new ArrayList<>()), // or current warnings
                            forecastMessage
                    );
                }
            }

            @Override
            public void onError(String error) {
                showError("Forecast error: " + error);
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
            String warningMessage = WeatherWarningUtils.parseWarningMessages(weather);
            updateWarningMessagesWithInfo(warningInfos, warningMessage);
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

        if (icon != null) icon.setImageResource(R.drawable.ic_weather_sunny);
        if (temp != null) temp.setText("24°C");
        if (humid != null) humid.setText("Humidity: 65%");

        MaterialCardView card = hikingConditionCard.get();
        TextView adviceText = hikingAdviceText.get();
        TextView confidenceText = hikingConfidenceText.get();

        if (card != null) {
            card.setCardBackgroundColor(Color.parseColor("#4CAF50")); // Green
            card.setStrokeColor(Color.parseColor("#388E3C")); // Dark Green
        }
        if (adviceText != null) {
            adviceText.setText("Good conditions for hiking");
        }
        if (confidenceText != null) {
            confidenceText.setText("Based on default conditions");
        }
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
        updateWarningMessagesWithInfo(warnings, "");
    }

    @Override
    public void onError(String error) {
        loadTemplateData();
        showError(error);
    }

    private void updateWarningMessagesWithInfo(List<WeatherWarningUtils.WarningInfo> warningInfos, String forecastMessage) {
        currentForecastMessage = forecastMessage;
        currentWarnings = new ArrayList<>(warningInfos);

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
        }

        // Update hiking advice
        updateHikingAdvice(forecastMessage, warningInfos);
    }

    private MaterialCardView createForecastCard(Context context, String forecastMessage) {
        MaterialCardView forecastCard = new MaterialCardView(context);
        forecastCard.setCardBackgroundColor(Color.parseColor("#E3F2FD")); // Light blue
        forecastCard.setStrokeColor(Color.parseColor("#2196F3")); // Blue
        forecastCard.setStrokeWidth(2);
        forecastCard.setCardElevation(0);
        forecastCard.setRadius(dpToPx(context, 8));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, dpToPx(context, 8), 0, dpToPx(context, 4));
        forecastCard.setLayoutParams(cardParams);

        LinearLayout forecastContent = new LinearLayout(context);
        forecastContent.setOrientation(LinearLayout.VERTICAL);
        forecastContent.setPadding(
                dpToPx(context, 16),
                dpToPx(context, 12),
                dpToPx(context, 16),
                dpToPx(context, 12)
        );

        TextView titleText = new TextView(context);
        titleText.setText("Weather Forecast");
        titleText.setTypeface(titleText.getTypeface(), Typeface.BOLD);
        titleText.setTextColor(Color.parseColor("#1976D2")); // Dark blue

        TextView forecastText = new TextView(context);
        forecastText.setText(forecastMessage);
        forecastText.setTextColor(Color.parseColor("#1976D2")); // Dark blue
        forecastText.setPadding(0, dpToPx(context, 4), 0, 0);

        forecastContent.addView(titleText);
        forecastContent.addView(forecastText);
        forecastCard.addView(forecastContent);

        return forecastCard;
    }
    private HikingRecommendationHelper.HikingRecommendation getHikingAdvise(String weatherConditions) {
        if (weatherConditions.isEmpty()) return null;

        HikingRecommendationHelper.HikerProfile averageHiker = new HikingRecommendationHelper.HikerProfile(
                25f, 65f, 170f, 3f, 1f, 2f, 500f, 10f
        );

        try {
            HikingRecommendationHelper.HikingRecommendation recommendation =
                    hikingRecommendationHelper.getPrediction(weatherConditions, averageHiker);

            return recommendation;
        } catch (Exception e) {

        }

        return null;
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