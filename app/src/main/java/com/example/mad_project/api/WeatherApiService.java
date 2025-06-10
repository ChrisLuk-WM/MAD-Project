package com.example.mad_project.api;

import com.example.mad_project.api.models.*;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    String WEATHER_API_PATH = "weatherAPI/opendata/weather.php";
    String OPENDATA_API_PATH = "weatherAPI/opendata/opendata.php";
    String LUNAR_API_PATH = "weatherAPI/opendata/lunardate.php";
    String RAINFALL_API_PATH = "weatherAPI/opendata/hourlyRainfall.php";

    // Weather Information API
    @GET(WEATHER_API_PATH)
    Call<WeatherForecast> getLocalWeatherForecast(@Query("dataType") String dataType, @Query("lang") String lang);

    @GET(WEATHER_API_PATH)
    Call<NineDayForecast> getNineDayForecast(@Query("dataType") String dataType, @Query("lang") String lang);

    @GET(WEATHER_API_PATH)
    Call<CurrentWeather> getCurrentWeather(@Query("dataType") String dataType, @Query("lang") String lang);

    @GET(WEATHER_API_PATH)
    Call<WeatherWarnings> getWeatherWarnings(@Query("dataType") String dataType, @Query("lang") String lang);

    @GET(WEATHER_API_PATH)
    Call<WarningInfo> getWarningInfo(@Query("dataType") String dataType, @Query("lang") String lang);

    @GET(WEATHER_API_PATH)
    Call<SpecialWeatherTips> getSpecialWeatherTips(@Query("dataType") String dataType, @Query("lang") String lang);

    // Climate and Weather Information API
    @GET(OPENDATA_API_PATH)
    Call<LatestVisibility> getLatestVisibility(
            @Query("dataType") String dataType,
            @Query("lang") String lang,
            @Query("rformat") String format
    );

    @GET(OPENDATA_API_PATH)
    Call<WeatherRadiationLevel> getWeatherRadiationLevel(
            @Query("dataType") String dataType,
            @Query("date") String date,
            @Query("lang") String lang
    );

    // Lunar Calendar API
    @GET(LUNAR_API_PATH)
    Call<LunarDate> getLunarDate(@Query("date") String date);

    // Hourly Rainfall API
    @GET(RAINFALL_API_PATH)
    Call<HourlyRainfall> getHourlyRainfall(@Query("lang") String lang);

    @GET("weatherAPI/opendata/earthquake.php")
    Call<QuickEarthquakeMessage> getQuickEarthquakeMessages(
            @Query("dataType") String dataType,
            @Query("lang") String lang
    );

    @GET("weatherAPI/opendata/earthquake.php")
    Call<FeltEarthquake> getFeltEarthquakeReport(
            @Query("dataType") String dataType,
            @Query("lang") String lang
    );
}