package com.example.mad_project.ui.pages.home.card;

import android.view.View;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

public class ActivityCardElements extends CardElements {
    private final LinearProgressIndicator stepsProgress;
    private final TextView stepsText;
    private final TextView stepsGoalText;
    private final TextView distanceText;
    private final TextView caloriesText;
    private final TextView activeTimeText;

    public ActivityCardElements(
            View containerView,
            LinearProgressIndicator stepsProgress,
            TextView stepsText,
            TextView stepsGoalText,
            TextView distanceText,
            TextView caloriesText,
            TextView activeTimeText
    ) {
        super(containerView);
        this.stepsProgress = stepsProgress;
        this.stepsText = stepsText;
        this.stepsGoalText = stepsGoalText;
        this.distanceText = distanceText;
        this.caloriesText = caloriesText;
        this.activeTimeText = activeTimeText;
    }

    public LinearProgressIndicator getStepsProgress() { return stepsProgress; }
    public TextView getStepsText() { return stepsText; }
    public TextView getStepsGoalText() { return stepsGoalText; }
    public TextView getDistanceText() { return distanceText; }
    public TextView getCaloriesText() { return caloriesText; }
    public TextView getActiveTimeText() { return activeTimeText; }
}