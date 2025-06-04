package com.example.mad_project.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Common {
    private static final String TAG = "Common";

    public static JSONObject loadJsonFromAsset(Context context, String fileName) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            Log.d(TAG, "First trail name: " + jsonString.substring(0, 100));

            return new JSONObject(jsonString);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error reading/parsing JSON file: " + fileName, e);
            return null;
        }
    }
}