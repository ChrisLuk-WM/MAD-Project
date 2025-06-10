package com.example.mad_project.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherApiClient {
    private static final String BASE_URL = "https://data.weather.gov.hk/";
    private static WeatherApiClient instance;
    private final WeatherApiService service;

    private WeatherApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(WeatherApiService.class);
    }

    public static synchronized WeatherApiClient getInstance() {
        if (instance == null) {
            instance = new WeatherApiClient();
        }
        return instance;
    }

    public WeatherApiService getService() {
        return service;
    }
}