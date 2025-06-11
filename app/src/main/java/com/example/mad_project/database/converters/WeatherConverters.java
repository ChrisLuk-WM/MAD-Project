package com.example.mad_project.database.converters;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class WeatherConverters {
    @TypeConverter
    public static String fromIntegerList(List<Integer> list) {
        if (list == null) return null;
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<Integer> toIntegerList(String value) {
        if (value == null) return null;
        Gson gson = new Gson();
        Type type = new TypeToken<List<Integer>>() {}.getType();
        return gson.fromJson(value, type);
    }

    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null) return null;
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toStringList(String value) {
        if (value == null) return null;
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, type);
    }

    @TypeConverter
    public static String fromRainfallMap(Map<String, Double> map) {
        if (map == null) return null;
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    @TypeConverter
    public static Map<String, Double> toRainfallMap(String value) {
        if (value == null) return null;
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Double>>() {}.getType();
        return gson.fromJson(value, type);
    }
}