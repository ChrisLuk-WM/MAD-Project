package com.example.mad_project.api.models;

import com.google.gson.annotations.SerializedName;
import androidx.annotation.Nullable;

import java.util.List;

public class NineDayForecast {
    @SerializedName("generalSituation")
    private String generalSituation;

    @SerializedName("updateTime")
    private String updateTime;

    @SerializedName("weatherForecast")
    private List<ForecastDay> weatherForecast;

    @Nullable
    @SerializedName("seaTemp")
    private SeaTemp seaTemp;

    @Nullable
    @SerializedName("soilTemp")
    private List<SoilTemp> soilTemp;

    public static class ForecastDay {
        @SerializedName("forecastDate")
        private String forecastDate;

        @SerializedName("forecastWeather")
        private String forecastWeather;

        @SerializedName("forecastMaxtemp")
        private Temperature forecastMaxTemp;

        @SerializedName("forecastMintemp")
        private Temperature forecastMinTemp;

        @SerializedName("week")
        private String week;

        @SerializedName("forecastWind")
        private String forecastWind;

        @SerializedName("forecastMaxrh")
        private Humidity forecastMaxRh;

        @SerializedName("forecastMinrh")
        private Humidity forecastMinRh;

        @SerializedName("ForecastIcon")
        private Integer forecastIcon;

        @SerializedName("PSR")
        private String psr;

        // Nested temperature class
        public static class Temperature {
            @SerializedName("value")
            private Integer value;

            @SerializedName("unit")
            private String unit;

            public Integer getValue() {
                return value;
            }

            public String getUnit() {
                return unit;
            }
        }

        // Nested humidity class
        public static class Humidity {
            @SerializedName("value")
            private Integer value;

            @SerializedName("unit")
            private String unit;

            public Integer getValue() {
                return value;
            }

            public String getUnit() {
                return unit;
            }
        }

        // Update getters to return new types
        public Temperature getForecastMaxTemp() {
            return forecastMaxTemp;
        }

        public Temperature getForecastMinTemp() {
            return forecastMinTemp;
        }

        public Humidity getForecastMaxRh() {
            return forecastMaxRh;
        }

        public Humidity getForecastMinRh() {
            return forecastMinRh;
        }

        // Rest of the getters remain the same
        public String getForecastDate() {
            return forecastDate;
        }

        public String getForecastWeather() {
            return forecastWeather;
        }

        public String getWeek() {
            return week;
        }

        public String getForecastWind() {
            return forecastWind;
        }

        public Integer getForecastIcon() {
            return forecastIcon;
        }

        public String getPsr() {
            return psr;
        }
    }

    // New classes for sea temperature
    public static class SeaTemp {
        @SerializedName("place")
        private String place;

        @SerializedName("value")
        private Integer value;

        @SerializedName("unit")
        private String unit;

        @SerializedName("recordTime")
        private String recordTime;

        public String getPlace() {
            return place;
        }

        public Integer getValue() {
            return value;
        }

        public String getUnit() {
            return unit;
        }

        public String getRecordTime() {
            return recordTime;
        }
    }

    // New classes for soil temperature
    public static class SoilTemp {
        @SerializedName("place")
        private String place;

        @SerializedName("value")
        private Double value;

        @SerializedName("unit")
        private String unit;

        @SerializedName("recordTime")
        private String recordTime;

        @SerializedName("depth")
        private Depth depth;

        public static class Depth {
            @SerializedName("unit")
            private String unit;

            @SerializedName("value")
            private Double value;

            public String getUnit() {
                return unit;
            }

            public Double getValue() {
                return value;
            }
        }

        public String getPlace() {
            return place;
        }

        public Double getValue() {
            return value;
        }

        public String getUnit() {
            return unit;
        }

        public String getRecordTime() {
            return recordTime;
        }

        public Depth getDepth() {
            return depth;
        }
    }

    // Getters for top-level fields
    public String getGeneralSituation() {
        return generalSituation;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public List<ForecastDay> getWeatherForecast() {
        return weatherForecast;
    }

    @Nullable
    public SeaTemp getSeaTemp() {
        return seaTemp;
    }

    @Nullable
    public List<SoilTemp> getSoilTemp() {
        return soilTemp;
    }
}