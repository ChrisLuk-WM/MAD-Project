package com.example.mad_project.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.mad_project.api.WeatherRepository;
import com.example.mad_project.api.models.CurrentWeather;
import com.example.mad_project.utils.WeatherWarningUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WeatherWorker extends Worker {
    private static final String TAG = "WeatherWorker";
    private static final String WEATHER_CHANNEL_ID = "weather_channel";
    private static final int WEATHER_NOTIFICATION_ID = 1001;

    private final Context context;
    private final NotificationManager notificationManager;
    private final WeatherRepository repository;
    private volatile boolean isRunning = true;

    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.repository = new WeatherRepository(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                WEATHER_CHANNEL_ID,
                "Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Weather alerts and warnings for hikers");
        notificationManager.createNotificationChannel(channel);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            while (isRunning && !isStopped()) {
                fetchAndProcessWeather();
                // Sleep for 1 minute before next update
                Thread.sleep(60 * 1000);
            }
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error in weather worker", e);
            return Result.failure();
        }
    }

    private void fetchAndProcessWeather() {
        CountDownLatch latch = new CountDownLatch(1);
        repository.getCurrentWeather(new WeatherRepository.WeatherCallback<CurrentWeather>() {
            @Override
            public void onSuccess(CurrentWeather weather) {
                WeatherService.getInstance(context).updateWeatherData(weather);
                checkAndNotifyWarnings(weather);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching weather: " + error);
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Log.e(TAG, "Weather fetch interrupted", e);
        }
    }

    private void checkAndNotifyWarnings(CurrentWeather weather) {
        if (weather.getWarningMessage() != null && !weather.getWarningMessage().isEmpty()) {
            List<WeatherWarningUtils.WarningInfo> warnings =
                    WeatherWarningUtils.parseWarnings(weather.getWarningMessage());

            WeatherService.getInstance(context).processWarnings(warnings);
        }
    }

    @Override
    public void onStopped() {
        super.onStopped();
        isRunning = false;
    }
}