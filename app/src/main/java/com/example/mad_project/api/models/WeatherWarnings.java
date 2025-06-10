package com.example.mad_project.api.models;

import com.google.gson.annotations.SerializedName;

public class WeatherWarnings {
    public static class Warning {
        @SerializedName("name")
        private String name;

        @SerializedName("code")
        private String code;

        @SerializedName("actionCode")
        private String actionCode;

        @SerializedName("issueTime")
        private String issueTime;

        @SerializedName("updateTime")
        private String updateTime;

        @SerializedName("expireTime")
        private String expireTime;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getActionCode() {
            return actionCode;
        }

        public void setActionCode(String actionCode) {
            this.actionCode = actionCode;
        }

        public String getIssueTime() {
            return issueTime;
        }

        public void setIssueTime(String issueTime) {
            this.issueTime = issueTime;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public String getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(String expireTime) {
            this.expireTime = expireTime;
        }
    }

    @SerializedName("WFIRE")
    private Warning fireDangerWarning;

    @SerializedName("WFROST")
    private Warning frostWarning;

    @SerializedName("WHOT")
    private Warning hotWeatherWarning;

    @SerializedName("WCOLD")
    private Warning coldWeatherWarning;

    @SerializedName("WMSGNL")
    private Warning strongMonsoonWarning;

    @SerializedName("WTCSGNL")
    private Warning tropicalCycloneWarning;

    public Warning getFireDangerWarning() {
        return fireDangerWarning;
    }

    public void setFireDangerWarning(Warning fireDangerWarning) {
        this.fireDangerWarning = fireDangerWarning;
    }

    public Warning getFrostWarning() {
        return frostWarning;
    }

    public void setFrostWarning(Warning frostWarning) {
        this.frostWarning = frostWarning;
    }

    public Warning getHotWeatherWarning() {
        return hotWeatherWarning;
    }

    public void setHotWeatherWarning(Warning hotWeatherWarning) {
        this.hotWeatherWarning = hotWeatherWarning;
    }

    public Warning getColdWeatherWarning() {
        return coldWeatherWarning;
    }

    public void setColdWeatherWarning(Warning coldWeatherWarning) {
        this.coldWeatherWarning = coldWeatherWarning;
    }

    public Warning getStrongMonsoonWarning() {
        return strongMonsoonWarning;
    }

    public void setStrongMonsoonWarning(Warning strongMonsoonWarning) {
        this.strongMonsoonWarning = strongMonsoonWarning;
    }

    public Warning getTropicalCycloneWarning() {
        return tropicalCycloneWarning;
    }

    public void setTropicalCycloneWarning(Warning tropicalCycloneWarning) {
        this.tropicalCycloneWarning = tropicalCycloneWarning;
    }
}
