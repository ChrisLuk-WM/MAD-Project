package com.example.mad_project.ui.pages.home;

import android.content.Context;
import android.os.Bundle;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.example.mad_project.statistics.dashboard.*;
import com.example.mad_project.ui.pages.home.card.*;

import java.util.List;

public class DashboardFragment extends Fragment {
    private WeatherCardHandler weatherHandler;
//    private ActivityCardHandler activityHandler;
    private HealthCardHandler healthHandler;

    // Weather section
    private ImageView weatherIcon;
    private TextView temperatureText;
    private TextView humidityText;
    private TextView uvIndexText;
    private TextView districtText;
    private ImageButton reloadButton;
    private MaterialButton hikingConditionChip;
    private LinearLayout warningMessagesContainer;
    private View weatherContainer;
    private View loadingView;

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
        initializeHandlers(view);
    }

    private void initializeHandlers(View view) {
        // Initialize weather card
        View weatherCard = view.findViewById(R.id.weather_card);
        WeatherCardElements weatherElements = new WeatherCardElements(
                weatherCard,
                weatherCard.findViewById(R.id.weather_icon),
                weatherCard.findViewById(R.id.text_temperature),
                weatherCard.findViewById(R.id.text_humidity),
                weatherCard.findViewById(R.id.text_uv_index),
                weatherCard.findViewById(R.id.text_district),
                weatherCard.findViewById(R.id.button_reload_weather),
                weatherCard.findViewById(R.id.warning_messages_container),
                weatherCard.findViewById(R.id.loading_view),
                weatherCard.findViewById(R.id.button_info),
                weatherCard.findViewById(R.id.hiking_condition_card),
                weatherCard.findViewById(R.id.text_hiking_advice),
                weatherCard.findViewById(R.id.text_hiking_confidence)
        );
        weatherHandler = new WeatherCardHandler(weatherElements);

        // Initialize activity card
//        View activityCard = view.findViewById(R.id.activity_card);
//        ActivityCardElements activityElements = new ActivityCardElements(
//                activityCard,
//                activityCard.findViewById(R.id.progress_steps),
//                activityCard.findViewById(R.id.text_steps),
//                activityCard.findViewById(R.id.text_steps_goal),
//                activityCard.findViewById(R.id.text_distance),
//                activityCard.findViewById(R.id.text_calories),
//                activityCard.findViewById(R.id.text_active_time)
//        );
//        activityHandler = new ActivityCardHandler(activityElements);

        // Initialize health card
        View healthCard = view.findViewById(R.id.health_card);
        HealthCardElements healthElements = new HealthCardElements(
                healthCard,
                healthCard.findViewById(R.id.text_health_insight)
        );
        healthHandler = new HealthCardHandler(healthElements);

        // Initialize handlers
        Context context = requireContext();
        weatherHandler.initialize(context);
//        activityHandler.initialize(context);
        healthHandler.initialize(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (weatherHandler != null) weatherHandler.cleanup();
//        if (activityHandler != null) activityHandler.cleanup();
        if (healthHandler != null) healthHandler.cleanup();
    }
}