package com.example.mad_project.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.mad_project.api.models.*;
import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.WeatherHistoryDao;
import com.example.mad_project.database.entities.WeatherHistoryEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {
    private final WeatherApiService apiService;
    private final WeatherHistoryDao weatherHistoryDao;
    private static final String LANG_EN = "en";

    public WeatherRepository(Context context) {
        this.apiService = WeatherApiClient.getInstance().getService();
        this.weatherHistoryDao = AppDatabase.getDatabase(context).weatherHistoryDao();
    }

    // Weather Information API Methods

    public void getLocalWeatherForecast(final WeatherCallback<WeatherForecast> callback) {
        apiService.getLocalWeatherForecast("flw", LANG_EN)
                .enqueue(new Callback<WeatherForecast>() {
                    @Override
                    public void onResponse(Call<WeatherForecast> call, Response<WeatherForecast> response) {
                        handleResponse(response, callback);
                    }

                    @Override
                    public void onFailure(Call<WeatherForecast> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void getNineDayForecast(final WeatherCallback<NineDayForecast> callback) {
        apiService.getNineDayForecast("fnd", LANG_EN)
                .enqueue(new Callback<NineDayForecast>() {
                    @Override
                    public void onResponse(Call<NineDayForecast> call, Response<NineDayForecast> response) {
                        handleResponse(response, callback);
                    }

                    @Override
                    public void onFailure(Call<NineDayForecast> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void getCurrentWeather(final WeatherCallback<CurrentWeather> callback) {
        apiService.getCurrentWeather("rhrread", LANG_EN)
                .enqueue(new Callback<CurrentWeather>() {
                    @Override
                    public void onResponse(Call<CurrentWeather> call, Response<CurrentWeather> response) {
                        handleResponse(response, callback);
                    }

                    @Override
                    public void onFailure(Call<CurrentWeather> call, Throwable t) {
                        callback.onError(t.getMessage());
                        Log.d("getCurrentWeather", t.getMessage(), t);
                    }
                });
    }

    public void getWeatherWarnings(final WeatherCallback<WeatherWarnings> callback) {
        apiService.getWeatherWarnings("warnsum", LANG_EN)
                .enqueue(new Callback<WeatherWarnings>() {
                    @Override
                    public void onResponse(Call<WeatherWarnings> call, Response<WeatherWarnings> response) {
                        handleResponse(response, callback);
                    }

                    @Override
                    public void onFailure(Call<WeatherWarnings> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void getWarningInfo(final WeatherCallback<WarningInfo> callback) {
        apiService.getWarningInfo("warningInfo", LANG_EN)
                .enqueue(new Callback<WarningInfo>() {
                    @Override
                    public void onResponse(Call<WarningInfo> call, Response<WarningInfo> response) {
                        handleResponse(response, callback);
                    }

                    @Override
                    public void onFailure(Call<WarningInfo> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void getSpecialWeatherTips(final WeatherCallback<SpecialWeatherTips> callback) {
        apiService.getSpecialWeatherTips("swt", LANG_EN)
                .enqueue(new Callback<SpecialWeatherTips>() {
                    @Override
                    public void onResponse(Call<SpecialWeatherTips> call, Response<SpecialWeatherTips> response) {
                        handleResponse(response, callback);
                    }

                    @Override
                    public void onFailure(Call<SpecialWeatherTips> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    // Climate and Weather Information API Methods

    public void getHourlyRainfall(final WeatherCallback<HourlyRainfall> callback) {
        apiService.getHourlyRainfall(LANG_EN)
                .enqueue(new Callback<HourlyRainfall>() {
                    @Override
                    public void onResponse(Call<HourlyRainfall> call, Response<HourlyRainfall> response) {
                        handleResponse(response, callback);
                    }

                    @Override
                    public void onFailure(Call<HourlyRainfall> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void getLatestVisibility(final WeatherCallback<LatestVisibility> callback) {
        apiService.getLatestVisibility("LTMV", LANG_EN, "json")
                .enqueue(new Callback<LatestVisibility>() {
                    @Override
                    public void onResponse(Call<LatestVisibility> call, Response<LatestVisibility> response) {
                        handleResponse(response, callback);
                    }

                    @Override
                    public void onFailure(Call<LatestVisibility> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void getWeatherRadiationLevel(String date, final WeatherCallback<WeatherRadiationLevel> callback) {
        apiService.getWeatherRadiationLevel("RYES", date, LANG_EN)
                .enqueue(new Callback<WeatherRadiationLevel>() {
                    @Override
                    public void onResponse(Call<WeatherRadiationLevel> call, Response<WeatherRadiationLevel> response) {
                        handleResponse(response, callback);
                    }

                    @Override
                    public void onFailure(Call<WeatherRadiationLevel> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    public void getLunarDate(String date, final WeatherCallback<LunarDate> callback) {
        apiService.getLunarDate(date)
                .enqueue(new Callback<LunarDate>() {
                    @Override
                    public void onResponse(Call<LunarDate> call, Response<LunarDate> response) {
                        handleResponse(response, callback);
                    }

                    @Override
                    public void onFailure(Call<LunarDate> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    // Helper method to handle responses
    private <T> void handleResponse(Response<T> response, WeatherCallback<T> callback) {
        if (response.isSuccessful() && response.body() != null) {
            if (response.body() instanceof CurrentWeather) {
                saveWeatherToDatabase((CurrentWeather) response.body());
            }
            callback.onSuccess(response.body());
        } else {
            String errorMsg = "Error: ";
            if (response.errorBody() != null) {
                try {
                    errorMsg += response.errorBody().string();
                } catch (IOException e) {
                    errorMsg += "Failed to get error message";
                }
            } else {
                errorMsg += "Unknown error occurred";
            }
            callback.onError(errorMsg);
        }
    }

    private void saveWeatherToDatabase(CurrentWeather weather) {
        new Thread(() -> {
            WeatherHistoryEntity entity = new WeatherHistoryEntity();
            entity.setUpdateTime(weather.getUpdateTime());
            entity.setIcon(weather.getIcon());
//            entity.setIconUpdateTime(weather.getIconUpdateTime());

            // Temperature
            if (weather.getTemperature() != null && !weather.getTemperature().getData().isEmpty()) {
                CurrentWeather.TemperatureRecord hkoTemp = weather.getTemperature().getData().stream()
                        .filter(t -> t.getPlace().equals("Hong Kong Observatory"))
                        .findFirst()
                        .orElse(weather.getTemperature().getData().get(0));

                entity.setTemperature(hkoTemp.getValue());
                entity.setTemperatureUnit(hkoTemp.getUnit());
                entity.setTemperatureRecordTime(weather.getTemperature().getRecordTime());
            }

            // Humidity
            if (weather.getHumidity() != null && !weather.getHumidity().getData().isEmpty()) {
                CurrentWeather.HumidityRecord humidityRecord = weather.getHumidity().getData().get(0);
                entity.setHumidity(humidityRecord.getValue());
                entity.setHumidityUnit(humidityRecord.getUnit());
                entity.setHumidityRecordTime(weather.getHumidity().getRecordTime());
            }

            // Rainfall
            if (weather.getRainfall() != null && weather.getRainfall().getData() != null) {
                Map<String, Double> rainfallMap = new HashMap<>();
                for (CurrentWeather.RainfallRecord record : weather.getRainfall().getData()) {
                    rainfallMap.put(record.getPlace(), record.getMax().doubleValue());
                }
                entity.setRainfallData(rainfallMap);
                entity.setRainfallUnit(weather.getRainfall().getData().get(0).getUnit());
                entity.setRainfallStartTime(weather.getRainfall().getStartTime());
                entity.setRainfallEndTime(weather.getRainfall().getEndTime());
            }

            entity.setWarningMessages(weather.getWarningMessage());

            // UV index
            if (weather.getUvindex() != null && !weather.getUvindex().getData().isEmpty()) {
                CurrentWeather.UVIndexRecord uvRecord = weather.getUvindex().getData().get(0);
                entity.setUvIndexValue(String.valueOf(uvRecord.getValue()));
                entity.setUvIndexDesc(uvRecord.getDesc());
            }

            weatherHistoryDao.insert(entity);
            weatherHistoryDao.deleteOldRecords(); // Keep database size managed
        }).start();
    }

    public void getLatestWeather(WeatherCallback<WeatherHistoryEntity> callback) {
        new Thread(() -> {
            WeatherHistoryEntity latestWeather = weatherHistoryDao.getLatestWeather();
            new Handler(Looper.getMainLooper()).post(() -> {
                if (latestWeather != null) {
                    callback.onSuccess(latestWeather);
                } else {
                    callback.onError("No weather data available");
                }
            });
        }).start();
    }

    // Callback interface
    public interface WeatherCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}