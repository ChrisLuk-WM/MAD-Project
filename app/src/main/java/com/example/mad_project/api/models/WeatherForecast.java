package com.example.mad_project.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherForecast {
    @SerializedName("generalSituation")
    private String generalSituation;

    @SerializedName("tcInfo")
    private String tcInfo;

    @SerializedName("fireDangerWarning")
    private String fireDangerWarning;

    @SerializedName("forecastPeriod")
    private String forecastPeriod;

    @SerializedName("forecastDesc")
    private String forecastDesc;

    @SerializedName("outlook")
    private String outlook;

    @SerializedName("updateTime")
    private String updateTime;

    public String getGeneralSituation() {
        return generalSituation;
    }

    public void setGeneralSituation(String generalSituation) {
        this.generalSituation = generalSituation;
    }

    public String getTcInfo() {
        return tcInfo;
    }

    public void setTcInfo(String tcInfo) {
        this.tcInfo = tcInfo;
    }

    public String getFireDangerWarning() {
        return fireDangerWarning;
    }

    public void setFireDangerWarning(String fireDangerWarning) {
        this.fireDangerWarning = fireDangerWarning;
    }

    public String getForecastPeriod() {
        return forecastPeriod;
    }

    public void setForecastPeriod(String forecastPeriod) {
        this.forecastPeriod = forecastPeriod;
    }

    public String getForecastDesc() {
        return forecastDesc;
    }

    public void setForecastDesc(String forecastDesc) {
        this.forecastDesc = forecastDesc;
    }

    public String getOutlook() {
        return outlook;
    }

    public void setOutlook(String outlook) {
        this.outlook = outlook;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}

