package com.example.mad_project.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.mad_project.constants.DownloadError;
import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.entities.TrailEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;

public class OfflineMapStorage {
    private static final String TAG = "OfflineMapStorage";
    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org/search";
    private final Context context;
    private final DownloadManager downloadManager;
    private final AppDatabase database;
    private OfflineMapDownloadListener downloadListener;

    public OfflineMapStorage(Context context) {
        this.context = context;
        this.downloadManager = DownloadManager.getInstance(context);
        this.database = AppDatabase.getDatabase(context);
    }

    public void downloadOfflineMapForTrail(TrailEntity trail, int minZoom, int maxZoom) {
        String searchUrl = String.format(
                "%s?q=%s&format=json",
                NOMINATIM_API,
                Uri.encode(trail.getTrailName())
        );

        if (downloadListener != null) {
            downloadListener.onDownloadProgress(0, 0, "Locating trail coordinates...");
        }

        // First, get coordinates from Nominatim
        downloadManager.downloadFile(searchUrl, createTempFile("nominatim_response.json"), null);
        downloadManager.setDownloadCallback(new DownloadManager.DownloadCallback() {
            @Override
            public void onProgress(String url, int progress) {}

            @Override
            public void onComplete(String url, File file) {
                if (url.equals(searchUrl)) {
                    try {
                        // Parse JSON response
                        String jsonStr = new String(Files.readAllBytes(file.toPath()));
                        JSONArray response = new JSONArray(jsonStr);
                        if (response.length() > 0) {
                            JSONObject location = response.getJSONObject(0);
                            // Successfully got coordinates, notify completion
                            if (downloadListener != null) {
                                downloadListener.onDownloadComplete();
                            }
                        } else {
                            if (downloadListener != null) {
                                downloadListener.onDownloadError("Location not found for trail: " + trail.getTrailName());
                            }
                        }
                    } catch (Exception e) {
                        if (downloadListener != null) {
                            downloadListener.onDownloadError("Error parsing location: " + e.getMessage());
                        }
                        Log.e(TAG, "Error parsing location: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onError(String url, DownloadError error, String message) {
                if (downloadListener != null) {
                    downloadListener.onDownloadError("Error downloading location data: " + message);
                }
                Log.e(TAG, "Error downloading location data: " + message);
            }

            @Override
            public void onPaused(String url, long bytesDownloaded) {}
            @Override
            public void onAllDownloadsPaused() {}
            @Override
            public void onConnectionLost() {}
            @Override
            public void onConnectionRestored() {}
            @Override
            public void onAllDownloadsFinished() {}
        });
    }

    public void setDownloadListener(OfflineMapDownloadListener listener) {
        this.downloadListener = listener;
    }

    private File createTempFile(String filename) {
        return new File(context.getCacheDir(), filename);
    }
}