package com.example.mad_project.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NineDayForecast {
    public static class ForecastDay {
        @SerializedName("forecastDate")
        private String forecastDate;

        @SerializedName("forecastWeather")
        private String forecastWeather;

        @SerializedName("forecastMaxtemp")
        private Integer forecastMaxTemp;

        @SerializedName("forecastMintemp")
        private Integer forecastMinTemp;

        @SerializedName("week")
        private String week;

        @SerializedName("forecastWind")
        private String forecastWind;

        @SerializedName("forecastMaxrh")
        private Integer forecastMaxRh;

        @SerializedName("forecastMinrh")
        private Integer forecastMinRh;

        @SerializedName("ForecastIcon")
        private Integer forecastIcon;

        @SerializedName("PSR")
        private String psr;

        public String getForecastWind() {
            return forecastWind;
        }

        public void setForecastWind(String forecastWind) {
            this.forecastWind = forecastWind;
        }

        public String getForecastDate() {
            return forecastDate;
        }

        public void setForecastDate(String forecastDate) {
            this.forecastDate = forecastDate;
        }

        public String getForecastWeather() {
            return forecastWeather;
        }

        public void setForecastWeather(String forecastWeather) {
            this.forecastWeather = forecastWeather;
        }

        public Integer getForecastMaxTemp() {
            return forecastMaxTemp;
        }

        public void setForecastMaxTemp(Integer forecastMaxTemp) {
            this.forecastMaxTemp = forecastMaxTemp;
        }

        public Integer getForecastMinTemp() {
            return forecastMinTemp;
        }

        public void setForecastMinTemp(Integer forecastMinTemp) {
            this.forecastMinTemp = forecastMinTemp;
        }

        public String getWeek() {
            return week;
        }

        public void setWeek(String week) {
            this.week = week;
        }

        public Integer getForecastMaxRh() {
            return forecastMaxRh;
        }

        public void setForecastMaxRh(Integer forecastMaxRh) {
            this.forecastMaxRh = forecastMaxRh;
        }

        public Integer getForecastMinRh() {
            return forecastMinRh;
        }

        public void setForecastMinRh(Integer forecastMinRh) {
            this.forecastMinRh = forecastMinRh;
        }

        public Integer getForecastIcon() {
            return forecastIcon;
        }

        public void setForecastIcon(Integer forecastIcon) {
            this.forecastIcon = forecastIcon;
        }

        public String getPsr() {
            return psr;
        }

        public void setPsr(String psr) {
            this.psr = psr;
        }
    }

    @SerializedName("weatherForecast")
    private List<ForecastDay> weatherForecast;

    public List<ForecastDay> getWeatherForecast() {
        return weatherForecast;
    }

    public void setWeatherForecast(List<ForecastDay> weatherForecast) {
        this.weatherForecast = weatherForecast;
    }
}
