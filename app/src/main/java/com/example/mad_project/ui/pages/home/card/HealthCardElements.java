package com.example.mad_project.ui.pages.home.card;

import android.view.View;
import android.widget.TextView;

public class HealthCardElements extends CardElements {
    private final TextView healthInsightText;

    public HealthCardElements(
            View containerView,
            TextView healthInsightText
    ) {
        super(containerView);
        this.healthInsightText = healthInsightText;
    }

    public TextView getHealthInsightText() { return healthInsightText; }
}