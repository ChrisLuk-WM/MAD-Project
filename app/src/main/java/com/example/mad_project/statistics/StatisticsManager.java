// StatisticsManager.java
package com.example.mad_project.statistics;


import java.util.EnumMap;
import java.util.Map;

public class StatisticsManager {
    private static volatile StatisticsManager instance;

    private final Map<StatisticsType, StatisticsValue<?>> statistics;

    private StatisticsManager() {
        statistics = new EnumMap<>(StatisticsType.class);
        initializeStatistics();
    }

    private void initializeStatistics() {
        statistics.put(StatisticsType.LOCATION, new StatisticsValue<>(null));
        statistics.put(StatisticsType.STEPS, new StatisticsValue<>(0));
        statistics.put(StatisticsType.SPEED, new StatisticsValue<>(0.0));
        statistics.put(StatisticsType.ALTITUDE, new StatisticsValue<>(0.0));
        statistics.put(StatisticsType.BEARING, new StatisticsValue<>(0f));
        statistics.put(StatisticsType.ACCURACY, new StatisticsValue<>(0.0));
        statistics.put(StatisticsType.TOTAL_DISTANCE, new StatisticsValue<>(0.0));
        statistics.put(StatisticsType.TOTAL_ELEVATION_GAIN, new StatisticsValue<>(0.0));
        statistics.put(StatisticsType.SESSION_START_TIME, new StatisticsValue<>(0L));
        statistics.put(StatisticsType.SESSION_ACTIVE, new StatisticsValue<>(false));
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
        return value != null ? value.get() : null;
    }

    @SuppressWarnings("unchecked")
    public <T> void setValue(StatisticsType type, T value) {
        StatisticsValue<T> statisticsValue = (StatisticsValue<T>) statistics.get(type);
        if (statisticsValue != null) {
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

    public void reset() {
        for (Map.Entry<StatisticsType, StatisticsValue<?>> entry : statistics.entrySet()) {
            entry.getValue().reset();
        }
    }
}