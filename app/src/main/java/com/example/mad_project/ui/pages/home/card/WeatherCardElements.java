package com.example.mad_project.ui.pages.home.card;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class WeatherCardElements extends CardElements {
    private final ImageView weatherIcon;
    private final TextView temperatureText;
    private final TextView humidityText;
    private final TextView uvIndexText;
    private final TextView districtText;
    private final ImageButton reloadButton;
    private final LinearLayout warningMessagesContainer;
    private final View loadingView;
    private final ImageButton infoButton;
    private MaterialCardView hikingConditionCard;
    private TextView hikingAdviceText;
    private TextView hikingConfidenceText;

    public WeatherCardElements(
            View containerView,
            ImageView weatherIcon,
            TextView temperatureText,
            TextView humidityText,
            TextView uvIndexText,
            TextView districtText,
            ImageButton reloadButton,
            LinearLayout warningMessagesContainer,
            View loadingView,
            ImageButton infoButton,
            MaterialCardView hikingConditionCard,
            TextView hikingAdviceText,
            TextView hikingConfidenceText
    ) {
        super(containerView);
        this.weatherIcon = weatherIcon;
        this.temperatureText = temperatureText;
        this.humidityText = humidityText;
        this.uvIndexText = uvIndexText;
        this.districtText = districtText;
        this.reloadButton = reloadButton;
        this.warningMessagesContainer = warningMessagesContainer;
        this.loadingView = loadingView;
        this.infoButton = infoButton;
        this.hikingConditionCard = hikingConditionCard;
        this.hikingAdviceText = hikingAdviceText;
        this.hikingConfidenceText = hikingConfidenceText;
    }

    public ImageView getWeatherIcon() { return weatherIcon; }
    public TextView getTemperatureText() { return temperatureText; }
    public TextView getHumidityText() { return humidityText; }
    public TextView getUvIndexText() { return uvIndexText; }
    public TextView getDistrictText() { return districtText; }
    public ImageButton getReloadButton() { return reloadButton; }
    public LinearLayout getWarningMessagesContainer() { return warningMessagesContainer; }
    public ImageButton getInfoButton() { return infoButton; }
    public View getLoadingView() { return loadingView; }
    public MaterialCardView getHikingConditionCard() { return hikingConditionCard; }
    public TextView getHikingAdviceText() { return hikingAdviceText; }
    public TextView getHikingConfidenceText() { return hikingConfidenceText; }
}