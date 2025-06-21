package com.example.mad_project.utils;

public interface OfflineMapDownloadListener {
    void onDownloadProgress(int completedTiles, int totalTiles, String currentTask);
    void onDownloadComplete();
    void onDownloadError(String error);
    void onTileDownloaded(int zoom, int x, int y);
}