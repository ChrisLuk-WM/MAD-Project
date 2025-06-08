// StatisticsManager.java
package com.example.mad_project.statistics;


import java.util.EnumMap;
import java.util.Map;

public class StatisticsManager {
    private static volatile StatisticsManager instance;

    private final Map<StatisticsType, StatisticsValue<?>> statistics;

    private StatisticsManager() {
        statistics = new EnumMap<>(StatisticsType.class);
        initializeDefaultValues();
    }

    private void initializeDefaultValues() {
        for (StatisticsType type : StatisticsType.values()) {
            statistics.put(type, type.getDefaultValue());
        }
    }

    public static StatisticsManager getInstance() {
        if (instance == null) {
            synchronized (StatisticsManager.class) {
                if (instance == null) {
                    instance = new StatisticsManager();
                }
            }
        }
        return instance;
    }
    @SuppressWarnings("unchecked")
    public <T> T getValue(StatisticsType type) {
        StatisticsValue<T> value = (StatisticsValue<T>) statistics.get(type);
        if (value == null) {
            value = (StatisticsValue<T>) type.getDefaultValue();
            statistics.put(type, value);
        }
        return value.get();
    }

    @SuppressWarnings("unchecked")
    public <T> void setValue(StatisticsType type, T value) {
        StatisticsValue<T> statisticsValue = (StatisticsValue<T>) statistics.get(type);
        if (statisticsValue == null) {
            statisticsValue = (StatisticsValue<T>) type.getDefaultValue();
            statistics.put(type, statisticsValue);
        }

        if (value instanceof Number && type.getType() != value.getClass()) {
            Number number = (Number) value;
            if (type.getType() == Double.class) {
                ((StatisticsValue<Double>) statisticsValue).set(number.doubleValue());
            } else if (type.getType() == Integer.class) {
                ((StatisticsValue<Integer>) statisticsValue).set(number.intValue());
            } else if (type.getType() == Long.class) {
                ((StatisticsValue<Long>) statisticsValue).set(number.longValue());
            }
        } else {
            statisticsValue.set(value);
        }
    }

    // Convenience methods for common operations
    public void startSession() {
        setValue(StatisticsType.SESSION_ACTIVE, true);
        setValue(StatisticsType.SESSION_START_TIME, System.currentTimeMillis());
        resetSessionStats();
    }

    public void stopSession() {
        setValue(StatisticsType.SESSION_ACTIVE, false);
    }

    public boolean isSessionActive() {
        return getValue(StatisticsType.SESSION_ACTIVE);
    }

    public long getSessionDuration() {
        Long startTime = getValue(StatisticsType.SESSION_START_TIME);
        return startTime > 0 ? System.currentTimeMillis() - startTime : 0;
    }

    private void resetSessionStats() {
        setValue(StatisticsType.TOTAL_DISTANCE, 0.0);
        setValue(StatisticsType.TOTAL_ELEVATION_GAIN, 0.0);
        setValue(StatisticsType.STEPS, 0);
    }

    public void clearStatistics() {
        for (StatisticsValue<?> value : statistics.values()) {
            value.reset();
        }
    }
}