package com.example.mad_project.ui.pages.route.planning;

import com.example.mad_project.services.HikingRecommendationHelper;

public class ForecastItem {
    private String dayOfWeek;
    private String date;
    private WeatherDetails weatherDetails;
    private boolean isRecommended;
    private HikingRecommendationHelper.HikingRecommendation recommendation;

    public static class WeatherDetails {
        private String forecastWeather;
        private int maxTemp;
        private int minTemp;
        private String wind;
        private int humidity;
        private String warning;

        public int getHumidity() {
            return humidity;
        }

        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }

        public String getForecastWeather() {
            return forecastWeather;
        }

        public void setForecastWeather(String forecastWeather) {
            this.forecastWeather = forecastWeather;
        }

        public int getMaxTemp() {
            return maxTemp;
        }

        public void setMaxTemp(int maxTemp) {
            this.maxTemp = maxTemp;
        }

        public int getMinTemp() {
            return minTemp;
        }

        public void setMinTemp(int minTemp) {
            this.minTemp = minTemp;
        }

        public String getWind() {
            return wind;
        }

        public void setWind(String wind) {
            this.wind = wind;
        }

        public String getWarning() {
            return warning;
        }

        public void setWarning(String warning) {
            this.warning = warning;
        }
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public WeatherDetails getWeatherDetails() {
        return weatherDetails;
    }

    public void setWeatherDetails(WeatherDetails weatherDetails) {
        this.weatherDetails = weatherDetails;
    }

    public boolean isRecommended() {
        return isRecommended;
    }

    public void setRecommended(boolean recommended) {
        isRecommended = recommended;
    }

    public HikingRecommendationHelper.HikingRecommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(HikingRecommendationHelper.HikingRecommendation recommendation) {
        this.recommendation = recommendation;
    }
}