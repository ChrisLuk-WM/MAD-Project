package com.example.mad_project.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WarningInfo {
    public static class Detail {
        @SerializedName("contents")
        private List<String> contents;

        @SerializedName("warningStatementCode")
        private String warningStatementCode;

        @SerializedName("subtype")
        private String subtype;

        @SerializedName("updateTime")
        private String updateTime;

        public List<String> getContents() {
            return contents;
        }

        public void setContents(List<String> contents) {
            this.contents = contents;
        }

        public String getWarningStatementCode() {
            return warningStatementCode;
        }

        public void setWarningStatementCode(String warningStatementCode) {
            this.warningStatementCode = warningStatementCode;
        }

        public String getSubtype() {
            return subtype;
        }

        public void setSubtype(String subtype) {
            this.subtype = subtype;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }
    }

    @SerializedName("details")
    private List<Detail> details;

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }
}
