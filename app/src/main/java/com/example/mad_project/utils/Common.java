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

    public static String getExtenstionName(String fileUrl) {
        // First try to get extension from the URL path
        String extension = "";

        // Method 1: Using last segment after dot
        int lastDotIndex = fileUrl.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = fileUrl.substring(lastDotIndex).toLowerCase();

            // Remove any query parameters or fragments
            int queryIndex = extension.indexOf('?');
            if (queryIndex > 0) {
                extension = extension.substring(0, queryIndex);
            }
            int fragmentIndex = extension.indexOf('#');
            if (fragmentIndex > 0) {
                extension = extension.substring(0, fragmentIndex);
            }
        }

        return extension;
    }
}