package com.example.mad_project.api.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

public class WeatherForecast {
    @SerializedName("generalSituation")
    @Nullable
    private String generalSituation;

    @SerializedName("tcInfo")
    @Nullable
    private String tcInfo;

    @SerializedName("fireDangerWarning")
    @Nullable
    private String fireDangerWarning;

    @SerializedName("forecastPeriod")
    @Nullable
    private String forecastPeriod;

    @SerializedName("forecastDesc")
    @Nullable
    private String forecastDesc;

    @SerializedName("outlook")
    @Nullable
    private String outlook;

    @SerializedName("updateTime")
    @Nullable
    private String updateTime;

    // Safe getters that never return null
    @NonNull
    public String getGeneralSituation() {
        return generalSituation != null ? generalSituation : "";
    }

    @NonNull
    public String getTcInfo() {
        return tcInfo != null ? tcInfo : "";
    }

    @NonNull
    public String getFireDangerWarning() {
        return fireDangerWarning != null ? fireDangerWarning : "";
    }

    @NonNull
    public String getForecastPeriod() {
        return forecastPeriod != null ? forecastPeriod : "";
    }

    @NonNull
    public String getForecastDesc() {
        return forecastDesc != null ? forecastDesc : "";
    }

    @NonNull
    public String getOutlook() {
        return outlook != null ? outlook : "";
    }

    @NonNull
    public String getUpdateTime() {
        return updateTime != null ? updateTime : "";
    }

    // Regular setters
    public void setGeneralSituation(@Nullable String generalSituation) {
        this.generalSituation = generalSituation;
    }

    public void setTcInfo(@Nullable String tcInfo) {
        this.tcInfo = tcInfo;
    }

    public void setFireDangerWarning(@Nullable String fireDangerWarning) {
        this.fireDangerWarning = fireDangerWarning;
    }

    public void setForecastPeriod(@Nullable String forecastPeriod) {
        this.forecastPeriod = forecastPeriod;
    }

    public void setForecastDesc(@Nullable String forecastDesc) {
        this.forecastDesc = forecastDesc;
    }

    public void setOutlook(@Nullable String outlook) {
        this.outlook = outlook;
    }

    public void setUpdateTime(@Nullable String updateTime) {
        this.updateTime = updateTime;
    }

    // Helper methods
    public boolean hasWarnings() {
        return !getFireDangerWarning().isEmpty() ||
                !getTcInfo().isEmpty();
    }

    public boolean hasForecast() {
        return !getForecastDesc().isEmpty() &&
                !getForecastPeriod().isEmpty();
    }

    @NonNull
    public String getFullForecast() {
        StringBuilder forecast = new StringBuilder();

        if (!getForecastPeriod().isEmpty()) {
            forecast.append(getForecastPeriod()).append("\n\n");
        }

        if (!getForecastDesc().isEmpty()) {
            forecast.append(getForecastDesc()).append("\n\n");
        }

        if (!getOutlook().isEmpty()) {
            forecast.append("Outlook: ").append(getOutlook());
        }

        return forecast.toString().trim();
    }

    @NonNull
    public String getWarningsText() {
        StringBuilder warnings = new StringBuilder();

        if (!getGeneralSituation().isEmpty()) {
            warnings.append(getGeneralSituation()).append("\n\n");
        }

        if (!getTcInfo().isEmpty()) {
            warnings.append("Tropical Cyclone Information:\n")
                    .append(getTcInfo()).append("\n\n");
        }

        if (!getFireDangerWarning().isEmpty()) {
            warnings.append("Fire Danger Warning:\n")
                    .append(getFireDangerWarning());
        }

        return warnings.toString().trim();
    }

    /**
     * Extracts important weather conditions from forecast description
     * @return Array of weather conditions
     */
    @NonNull
    public String[] getWeatherConditions() {
        String desc = getForecastDesc().toLowerCase();
        java.util.List<String> conditions = new java.util.ArrayList<>();

        // Check for common weather conditions
        if (desc.contains("thunderstorm")) conditions.add("THUNDERSTORM");
        if (desc.contains("rain") || desc.contains("shower")) conditions.add("RAIN");
        if (desc.contains("wind") || desc.contains("gale")) conditions.add("WINDY");
        if (desc.contains("sunny") || desc.contains("fine")) conditions.add("SUNNY");
        if (desc.contains("cloudy")) conditions.add("CLOUDY");
        if (desc.contains("fog") || desc.contains("mist")) conditions.add("FOG");
        if (desc.contains("hot")) conditions.add("HOT");
        if (desc.contains("cold")) conditions.add("COLD");
        if (desc.contains("humid")) conditions.add("HUMID");

        return conditions.toArray(new String[0]);
    }
}