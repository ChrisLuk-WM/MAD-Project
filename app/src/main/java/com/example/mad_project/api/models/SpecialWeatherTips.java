package com.example.mad_project.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SpecialWeatherTips {
    public static class Tip {
        @SerializedName("desc")
        private String description;

        @SerializedName("updateTime")
        private String updateTime;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }
    }

    @SerializedName("swt")
    private List<Tip> tips;

    public List<Tip> getTips() {
        return tips;
    }

    public void setTips(List<Tip> tips) {
        this.tips = tips;
    }
}
