package com.example.mad_project.statistics;

import android.location.Location;
import androidx.annotation.NonNull;

public enum StatisticsType {
    LOCATION(Location.class),
    STEPS(Integer.class),
    SPEED(Double.class),
    ALTITUDE(Double.class),
    BEARING(Float.class),
    ACCURACY(Double.class),
    TOTAL_DISTANCE(Double.class),
    TOTAL_ELEVATION_GAIN(Double.class),
    SESSION_START_TIME(Long.class),
    SESSION_ACTIVE(Boolean.class);

    private final Class<?> type;

    StatisticsType(Class<?> type) {
        this.type = type;
    }

    @NonNull
    public Class<?> getType() {
        return type;
    }
}