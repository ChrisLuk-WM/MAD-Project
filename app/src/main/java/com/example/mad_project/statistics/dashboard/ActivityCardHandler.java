package com.example.mad_project.statistics.dashboard;

import android.content.Context;
import android.widget.TextView;

import com.example.mad_project.ui.pages.home.card.ActivityCardElements;
import com.example.mad_project.ui.pages.home.card.CardHandler;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class ActivityCardHandler implements CardHandler {
    private final ActivityCardElements elements;
    private Context context;

    // Constants
    private static int DAILY_STEPS_GOAL = 8000;
    private static double METERS_PER_STEP = 0.762; // Average step length
    private static double CALORIES_PER_STEP = 0.04; // Average calories burned per step

    // Current stats
    private int currentSteps = 0;
    private int activeMinutes = 0;

    public ActivityCardHandler(ActivityCardElements elements) {
        this.elements = elements;
    }

    @Override
    public void initialize(Context context) {
        this.context = context;
        // Load initial template data while waiting for real data
        loadTemplateData();
        // TODO: Initialize activity tracking
        // setupActivityTracking();
    }

    @Override
    public void cleanup() {
        // TODO: Cleanup activity tracking
        // stopActivityTracking();
    }

    private void loadTemplateData() {
        DAILY_STEPS_GOAL = 10000;
        METERS_PER_STEP = 0.762;
        CALORIES_PER_STEP = 0.04;
        updateSteps(6500);
        updateActiveTime(45);
    }

    public void updateSteps(int steps) {
        this.currentSteps = steps;

        // Update progress
        LinearProgressIndicator progress = elements.getStepsProgress();
        if (progress != null) {
            int progressPercentage = (steps * 100) / DAILY_STEPS_GOAL;
            progress.setProgress(Math.min(progressPercentage, 100));
        }

        // Update steps text
        TextView stepsText = elements.getStepsText();
        if (stepsText != null) {
            stepsText.setText(String.format("%,d steps", steps));
        }

        // Update goal text
        TextView goalText = elements.getStepsGoalText();
        if (goalText != null) {
            goalText.setText(String.format("Goal: %,d", DAILY_STEPS_GOAL));
        }

        // Update distance
        TextView distanceText = elements.getDistanceText();
        if (distanceText != null) {
            double distanceKm = (steps * METERS_PER_STEP) / 1000.0;
            distanceText.setText(String.format("%.1f km", distanceKm));
        }

        // Update calories
        TextView caloriesText = elements.getCaloriesText();
        if (caloriesText != null) {
            int calories = (int) (steps * CALORIES_PER_STEP);
            caloriesText.setText(String.valueOf(calories));
        }
    }

    public void updateActiveTime(int minutes) {
        this.activeMinutes = minutes;

        TextView activeTimeText = elements.getActiveTimeText();
        if (activeTimeText != null) {
            if (minutes < 60) {
                activeTimeText.setText(String.format("%d min", minutes));
            } else {
                int hours = minutes / 60;
                int remainingMinutes = minutes % 60;
                if (remainingMinutes == 0) {
                    activeTimeText.setText(String.format("%dh", hours));
                } else {
                    activeTimeText.setText(String.format("%dh %dm", hours, remainingMinutes));
                }
            }
        }
    }

    // Method to get current progress percentage
    public int getProgressPercentage() {
        return (currentSteps * 100) / DAILY_STEPS_GOAL;
    }

    // Method to check if daily goal is achieved
    public boolean isDailyGoalAchieved() {
        return currentSteps >= DAILY_STEPS_GOAL;
    }

    // Method to get remaining steps to goal
    public int getRemainingSteps() {
        return Math.max(0, DAILY_STEPS_GOAL - currentSteps);
    }

    // Method to get total distance in kilometers
    public double getTotalDistanceKm() {
        return (currentSteps * METERS_PER_STEP) / 1000.0;
    }

    // Method to get total calories burned
    public int getTotalCalories() {
        return (int) (currentSteps * CALORIES_PER_STEP);
    }

    // Method to get active time in minutes
    public int getActiveTime() {
        return activeMinutes;
    }
}