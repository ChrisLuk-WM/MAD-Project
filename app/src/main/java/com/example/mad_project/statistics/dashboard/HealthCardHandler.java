package com.example.mad_project.statistics.dashboard;

import android.content.Context;
import android.widget.TextView;

import com.example.mad_project.api.models.CurrentWeather;
import com.example.mad_project.ui.pages.home.card.CardHandler;
import com.example.mad_project.ui.pages.home.card.HealthCardElements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HealthCardHandler implements CardHandler {
    private final HealthCardElements elements;
    private Context context;
    private CurrentWeather currentWeather;
    private int currentSteps;
    private int activeMinutes;

    // Constants
    private static final int RECOMMENDED_DAILY_STEPS = 10000;
    private static final int RECOMMENDED_ACTIVE_MINUTES = 30;
    private static final double HIGH_TEMPERATURE_THRESHOLD = 30.0;
    private static final double LOW_TEMPERATURE_THRESHOLD = 10.0;
    private static final int HIGH_HUMIDITY_THRESHOLD = 85;
    private static final double HIGH_UV_THRESHOLD = 7.0;

    public HealthCardHandler(HealthCardElements elements) {
        this.elements = elements;
    }

    @Override
    public void initialize(Context context) {
        this.context = context;
        loadTemplateData();
    }

    @Override
    public void cleanup() {
        // Clean up any resources if needed
    }

    private void loadTemplateData() {
        updateHealthInsight("Stay active and monitor weather conditions for the best hiking experience.");
    }

    public void updateWeatherData(CurrentWeather weather) {
        this.currentWeather = weather;
        generateHealthInsight();
    }

    public void updateActivityData(int steps, int activeMinutes) {
        this.currentSteps = steps;
        this.activeMinutes = activeMinutes;
        generateHealthInsight();
    }

    private void generateHealthInsight() {
        List<String> insights = new ArrayList<>();

        // Add time-based greeting
        insights.add(getTimeBasedGreeting());

        // Activity-based insights
        addActivityInsights(insights);

        // Combine insights
        String finalInsight = String.join("\n\n", insights);
        updateHealthInsight(finalInsight);
    }

    private String getTimeBasedGreeting() {
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH", Locale.getDefault());
        int hour = Integer.parseInt(hourFormat.format(new Date()));

        if (hour >= 5 && hour < 12) {
            return "Good morning! Start your day with some light exercise.";
        } else if (hour >= 12 && hour < 17) {
            return "Good afternoon! Keep moving to maintain your energy levels.";
        } else if (hour >= 17 && hour < 21) {
            return "Good evening! Consider a relaxing walk to wind down.";
        } else {
            return "It's late! Make sure to get enough rest for tomorrow's activities.";
        }
    }

    private void addActivityInsights(List<String> insights) {
        // Steps progress
        int remainingSteps = RECOMMENDED_DAILY_STEPS - currentSteps;
        if (remainingSteps > 0) {
            insights.add(String.format("You need %,d more steps to reach your daily goal. Keep moving!",
                    remainingSteps));
        } else {
            insights.add("Congratulations! You've reached your daily steps goal. Great job staying active!");
        }

        // Active minutes
        if (activeMinutes < RECOMMENDED_ACTIVE_MINUTES) {
            insights.add(String.format("Try to get %d more minutes of activity today for optimal health.",
                    RECOMMENDED_ACTIVE_MINUTES - activeMinutes));
        } else {
            insights.add("You've met your active minutes goal. Excellent work on staying active!");
        }
    }

    private void updateHealthInsight(String insight) {
        TextView insightText = elements.getHealthInsightText();
        if (insightText != null) {
            insightText.setText(insight);
        }
    }

    // Helper method to determine if current conditions are good for exercise
    public boolean isGoodForExercise() {
        if (currentWeather == null) return true;

        // Check temperature
        if (currentWeather.getTemperature() != null && !currentWeather.getTemperature().getData().isEmpty()) {
            double temp = currentWeather.getTemperature().getData().get(0).getValue();
            if (temp > HIGH_TEMPERATURE_THRESHOLD || temp < LOW_TEMPERATURE_THRESHOLD) {
                return false;
            }
        }

        // Check humidity
        if (currentWeather.getHumidity() != null && !currentWeather.getHumidity().getData().isEmpty()) {
            int humidity = currentWeather.getHumidity().getData().get(0).getValue();
            if (humidity > HIGH_HUMIDITY_THRESHOLD) {
                return false;
            }
        }

        // Check UV index
        if (currentWeather.getUvindex() != null && !currentWeather.getUvindex().getData().isEmpty()) {
            double uvIndex = currentWeather.getUvindex().getData().get(0).getValue();
            if (uvIndex > HIGH_UV_THRESHOLD) {
                return false;
            }
        }

        // Check warnings
        return currentWeather.getWarningMessage() == null || currentWeather.getWarningMessage().isEmpty();
    }
}