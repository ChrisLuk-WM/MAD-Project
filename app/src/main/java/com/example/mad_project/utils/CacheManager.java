package com.example.mad_project.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mad_project.constants.DownloadState;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CacheManager {
    private static final String PREF_NAME = "download_cache";
    private static final String KEY_DOWNLOAD_STATES = "download_states";
    private static final String KEY_DOWNLOAD_PROGRESS = "download_progress";

    private final SharedPreferences preferences;

    public CacheManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveDownloadState(String url, DownloadState state, long bytesDownloaded) {
        try {
            JSONObject states = getDownloadStates();
            JSONObject progress = getDownloadProgress();

            states.put(url, state.name());
            progress.put(url, bytesDownloaded);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_DOWNLOAD_STATES, states.toString());
            editor.putString(KEY_DOWNLOAD_PROGRESS, progress.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Map<String, DownloadState> getAllDownloadStates() {
        Map<String, DownloadState> result = new HashMap<>();
        try {
            JSONObject states = getDownloadStates();
            Iterator<String> keys = states.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                result.put(key, DownloadState.valueOf(states.getString(key)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Long getDownloadedBytes(String url) {
        try {
            JSONObject progress = getDownloadProgress();
            return progress.has(url) ? progress.getLong(url) : null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clearCache() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    private JSONObject getDownloadStates() throws JSONException {
        String statesStr = preferences.getString(KEY_DOWNLOAD_STATES, "{}");
        return new JSONObject(statesStr);
    }

    private JSONObject getDownloadProgress() throws JSONException {
        String progressStr = preferences.getString(KEY_DOWNLOAD_PROGRESS, "{}");
        return new JSONObject(progressStr);
    }
}