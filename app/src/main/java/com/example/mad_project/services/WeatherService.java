package com.example.mad_project.services;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.mad_project.R;
import com.example.mad_project.api.WeatherRepository;
import com.example.mad_project.api.models.CurrentWeather;
import com.example.mad_project.utils.WeatherWarningUtils;

import java.util.ArrayList;
import java.util.List;

public class WeatherService {
    private static final String TAG = "WeatherService";
    private static final String WEATHER_CHANNEL_ID = "weather_channel";
    private static final int WEATHER_NOTIFICATION_ID = 1001;

    private static WeatherService instance;
    private final Context context;
    private final NotificationManager notificationManager;
    private final List<WeatherUpdateListener> listeners;
    private CurrentWeather currentWeather;
    private final WeatherRepository repository;
    private boolean isTrackingActive = false;

    public interface WeatherUpdateListener {
        void onWeatherUpdated(CurrentWeather weather);
        void onWarningsUpdated(List<WeatherWarningUtils.WarningInfo> warnings);
        void onError(String error);
    }

    private WeatherService(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.listeners = new ArrayList<>();
        this.repository = new WeatherRepository(context);
    }

    public static synchronized WeatherService getInstance(Context context) {
        if (instance == null) {
            instance = new WeatherService(context);
        }
        return instance;
    }

    // Called by WeatherWorker when new weather data is available
    void updateWeatherData(CurrentWeather weather) {
        this.currentWeather = weather;
        notifyListenersWeatherUpdated(weather);
    }

    // Called by WeatherWorker when warnings need to be processed
    void processWarnings(List<WeatherWarningUtils.WarningInfo> warnings) {
        if (isTrackingActive && !warnings.isEmpty()) {
            // Filter severe warnings
            List<WeatherWarningUtils.WarningInfo> severeWarnings = new ArrayList<>();
            for (WeatherWarningUtils.WarningInfo warning : warnings) {
                if (warning.getSeverity() >= 3) {
                    severeWarnings.add(warning);
                }
            }

            if (!severeWarnings.isEmpty()) {
                showWarningNotification(severeWarnings);
            }
        }

        notifyListenersWarningsUpdated(warnings);
    }

    private void showWarningNotification(List<WeatherWarningUtils.WarningInfo> warnings) {
        StringBuilder message = new StringBuilder("Weather Alert:\n");
        for (WeatherWarningUtils.WarningInfo warning : warnings) {
            message.append("• ").append(warning.getLevel())
                    .append(": ").append(warning.getReminder()).append("\n");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, WEATHER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Weather Warning")
                .setContentText(message.toString())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message.toString()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(WEATHER_NOTIFICATION_ID, builder.build());
    }

    // Called by TrackingWorkManager
    void onTrackingStarted() {
        isTrackingActive = true;
    }

    void onTrackingStopped() {
        isTrackingActive = false;
    }

    // Listener management
    public void addListener(WeatherUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(WeatherUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListenersWeatherUpdated(CurrentWeather weather) {
        for (WeatherUpdateListener listener : listeners) {
            listener.onWeatherUpdated(weather);
        }
    }

    private void notifyListenersWarningsUpdated(List<WeatherWarningUtils.WarningInfo> warnings) {
        for (WeatherUpdateListener listener : listeners) {
            listener.onWarningsUpdated(warnings);
        }
    }

    // Getter methods
    public CurrentWeather getCurrentWeather() {
        return currentWeather;
    }

    public boolean isTrackingActive() {
        return isTrackingActive;
    }

    public void fetchWeatherData(WeatherRepository.WeatherCallback<CurrentWeather> callback) {
        repository.getCurrentWeather(new WeatherRepository.WeatherCallback<CurrentWeather>() {
            @Override
            public void onSuccess(CurrentWeather result) {
                currentWeather = result;
                notifyListenersWeatherUpdated(result);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
                for (WeatherUpdateListener listener : listeners) {
                    listener.onError(error);
                }
            }
        });
    }

    public void fetchNineDayForecast(WeatherRepository.WeatherCallback<CurrentWeather> callback) {
        repository.getCurrentWeather(new WeatherRepository.WeatherCallback<CurrentWeather>() {
            @Override
            public void onSuccess(CurrentWeather result) {
                currentWeather = result;
                notifyListenersWeatherUpdated(result);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
                for (WeatherUpdateListener listener : listeners) {
                    listener.onError(error);
                }
            }
        });
    }
}