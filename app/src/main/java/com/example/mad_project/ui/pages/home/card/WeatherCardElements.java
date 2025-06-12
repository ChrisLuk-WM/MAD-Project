package com.example.mad_project.ui.pages.home.card;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

public class WeatherCardElements extends CardElements {
    private final ImageView weatherIcon;
    private final TextView temperatureText;
    private final TextView humidityText;
    private final TextView uvIndexText;
    private final TextView districtText;
    private final ImageButton reloadButton;
    private final MaterialButton hikingConditionChip;
    private final LinearLayout warningMessagesContainer;
    private final View loadingView;

    public WeatherCardElements(
            View containerView,
            ImageView weatherIcon,
            TextView temperatureText,
            TextView humidityText,
            TextView uvIndexText,
            TextView districtText,
            ImageButton reloadButton,
            MaterialButton hikingConditionChip,
            LinearLayout warningMessagesContainer,
            View loadingView
    ) {
        super(containerView);
        this.weatherIcon = weatherIcon;
        this.temperatureText = temperatureText;
        this.humidityText = humidityText;
        this.uvIndexText = uvIndexText;
        this.districtText = districtText;
        this.reloadButton = reloadButton;
        this.hikingConditionChip = hikingConditionChip;
        this.warningMessagesContainer = warningMessagesContainer;
        this.loadingView = loadingView;
    }

    public ImageView getWeatherIcon() { return weatherIcon; }
    public TextView getTemperatureText() { return temperatureText; }
    public TextView getHumidityText() { return humidityText; }
    public TextView getUvIndexText() { return uvIndexText; }
    public TextView getDistrictText() { return districtText; }
    public ImageButton getReloadButton() { return reloadButton; }
    public MaterialButton getHikingConditionChip() { return hikingConditionChip; }
    public LinearLayout getWarningMessagesContainer() { return warningMessagesContainer; }
    public View getLoadingView() { return loadingView; }
}