package com.example.mad_project.statistics;

import android.location.Location;

public enum StatisticsType {
    TOTAL_DISTANCE(new StatisticsValue<>(0.0)),
    STEPS(new StatisticsValue<>(0)),
    SPEED(new StatisticsValue<>(0.0)),
    ALTITUDE(new StatisticsValue<>(0.0)),
    ACCURACY(new StatisticsValue<>(0.0)),
    BEARING(new StatisticsValue<>(0.0)),
    TOTAL_ELEVATION_GAIN(new StatisticsValue<>(0.0)),
    LOCATION(new StatisticsValue<>(null)),
    SESSION_ACTIVE(new StatisticsValue<>(false)),
    SESSION_START_TIME(new StatisticsValue<>(0L));

    private final StatisticsValue<?> defaultValue;
    private final Class<?> type;

    @SuppressWarnings("unchecked")
    <T> StatisticsType(StatisticsValue<T> defaultValue) {
        this.defaultValue = defaultValue;
        this.type = defaultValue.get() != null ?
                defaultValue.get().getClass() :
                Object.class;
    }

    public StatisticsValue<?> getDefaultValue() {
        return defaultValue;
    }

    public Class<?> getType() {
        return type;
    }
}