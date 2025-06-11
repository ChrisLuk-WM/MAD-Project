// StatisticsManager.java
package com.example.mad_project.statistics;


import java.util.EnumMap;
import java.util.Map;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.example.mad_project.utils.LocationUtils;

public class StatisticsManager {
    private static volatile StatisticsManager instance;
    private static Context applicationContext;

    private final Map<StatisticsType, StatisticsValue<?>> statistics;
    private static final String PREFS_NAME = "location_prefs";
    private static final String KEY_LAST_LAT = "last_latitude";
    private static final String KEY_LAST_LON = "last_longitude";

    // Default location (Hong Kong Observatory)
    private static final double DEFAULT_LAT = 22.3020;
    private static final double DEFAULT_LON = 114.1740;

    private StatisticsManager(Context context) {
        statistics = new EnumMap<>(StatisticsType.class);
        applicationContext = context.getApplicationContext();
        initializeDefaultValues();
    }

    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
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
                    if (applicationContext == null) {
                        throw new IllegalStateException("Call StatisticsManager.init(Context) first");
                    }
                    instance = new StatisticsManager(applicationContext);
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

        if (type == StatisticsType.LOCATION && value instanceof Location) {
            // Store location in SharedPreferences when it's updated
            saveLastLocation((Location) value);
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

    private void saveLastLocation(Location location) {
        SharedPreferences prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(KEY_LAST_LAT, (float) location.getLatitude());
        editor.putFloat(KEY_LAST_LON, (float) location.getLongitude());
        editor.apply();
    }

    private Location getLastLocation() {
        SharedPreferences prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        float lat = prefs.getFloat(KEY_LAST_LAT, (float) DEFAULT_LAT);
        float lon = prefs.getFloat(KEY_LAST_LON, (float) DEFAULT_LON);

        Location location = new Location("last_known");
        location.setLatitude(lat);
        location.setLongitude(lon);
        return location;
    }

    public String getDistrict() {
        // Try to get current location from statistics
        Location currentLocation = getValue(StatisticsType.LOCATION);

        if (currentLocation == null) {
            // If no current location, try to get last saved location
            currentLocation = getLastLocation();
        }

        return LocationUtils.getNearestDistrict(
                currentLocation.getLatitude(),
                currentLocation.getLongitude()
        );
    }

    public String getNearestWeatherStation() {
        // Try to get current location from statistics
        Location currentLocation = getValue(StatisticsType.LOCATION);

        if (currentLocation == null) {
            // If no current location, try to get last saved location
            currentLocation = getLastLocation();
        }

        return LocationUtils.getNearestWeatherStation(
                currentLocation.getLatitude(),
                currentLocation.getLongitude()
        );
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