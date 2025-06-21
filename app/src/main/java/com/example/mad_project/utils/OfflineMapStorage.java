package com.example.mad_project.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.mad_project.constants.DownloadError;
import com.example.mad_project.content_downloader.DownloadStateListener;
import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.OfflineMapDao;
import com.example.mad_project.database.entities.OfflineMapTileEntity;
import com.example.mad_project.database.entities.TrailEntity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.config.Configuration;


import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import in.excogitation.lib.sensey.BuildConfig;

public class OfflineMapStorage {
    private static final String TAG = "OfflineMapStorage";
    private static final String MAP_DIRECTORY = "offline_maps";
    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org/search";
    private static final double AREA_SIZE_KM = 5; // 5km radius = 10km x 10km area
    private final Context context;
    private final DownloadManager downloadManager;
    private final AppDatabase database;
    private OfflineMapDownloadListener downloadListener;

    private static class DownloadProgress {
        int completedTiles = 0;
        int totalTiles = 0;
        final Object lock = new Object();

        void incrementCompleted() {
            synchronized (lock) {
                completedTiles++;
            }
        }

        int getProgress() {
            synchronized (lock) {
                return totalTiles > 0 ? (completedTiles * 100) / totalTiles : 0;
            }
        }
    }

    private static class TileCoordinate {
        final int x;
        final int y;

        TileCoordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "TileCoordinate{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TileCoordinate that = (TileCoordinate) o;
            return x == that.x && y == that.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private final DownloadProgress progress;

    public OfflineMapStorage(Context context) {
        this.context = context;
        this.downloadManager = DownloadManager.getInstance(context);
        this.database = AppDatabase.getDatabase(context);
        this.progress = new DownloadProgress();
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
                            double lat = location.getDouble("lat");
                            double lon = location.getDouble("lon");

                            // Calculate bounding box
                            LatLngBounds bounds = calculateBoundingBox(lat, lon, AREA_SIZE_KM);

                            // Calculate total tiles needed
                            calculateTotalTiles(bounds, minZoom, maxZoom);

                            // Start downloading tiles
                            downloadMapTiles(trail, bounds, minZoom, maxZoom);
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

    private void calculateTotalTiles(LatLngBounds bounds, int minZoom, int maxZoom) {
        progress.totalTiles = 0;
        progress.completedTiles = 0;

        for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
            List<TileCoordinate> tiles = calculateTilesInBounds(bounds, zoom);
            progress.totalTiles += tiles.size();
        }

        if (downloadListener != null) {
            downloadListener.onDownloadProgress(0, progress.totalTiles, "Starting download...");
        }
    }

    public void setDownloadListener(OfflineMapDownloadListener listener) {
        this.downloadListener = listener;
    }

    private LatLngBounds calculateBoundingBox(double centerLat, double centerLon, double radiusKm) {
        // Earth's radius in kilometers
        double earthRadius = 6371.0;

        // Convert radius from kilometers to degrees
        double latChange = (radiusKm / earthRadius) * (180.0 / Math.PI);
        double lonChange = (radiusKm / earthRadius) * (180.0 / Math.PI) /
                Math.cos(centerLat * Math.PI / 180.0);

        return new LatLngBounds(
                new LatLng(centerLat - latChange, centerLon - lonChange), // southwest
                new LatLng(centerLat + latChange, centerLon + lonChange)  // northeast
        );
    }

    @SuppressLint("DefaultLocale")
    private void downloadMapTiles(TrailEntity trail, LatLngBounds bounds, int minZoom, int maxZoom) {
        // Initialize configuration
        Context ctx = context.getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // Use OpenTopoMap instead of OSM
        String tileServerUrl = "https://tile.opentopomap.org/%d/%d/%d.png";
        // Alternative servers if needed:
        // String tileServerUrl = "https://a.tile-cyclosm.openstreetmap.fr/cyclosm/%d/%d/%d.png";
        // String tileServerUrl = "https://tiles.wmflabs.org/osm-no-labels/%d/%d/%d.png";

        for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
            final int currentZoom = zoom;
            List<TileCoordinate> tiles = calculateTilesInBounds(bounds, zoom);

            for (TileCoordinate tile : tiles) {
                String tileUrl = String.format(tileServerUrl, currentZoom, tile.x, tile.y);

                File tileFile = new File(
                        context.getFilesDir(),
                        String.format("%s/%d/%d/%d_%d.png",
                                MAP_DIRECTORY, trail.getId(), currentZoom, tile.x, tile.y)
                );

                tileFile.getParentFile().mkdirs();

                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "HikingApp/1.0 (https://google.com)");
                headers.put("Accept", "image/webp,image/png,image/*,*/*;q=0.8");
                headers.put("Accept-Language", "en-US,en;q=0.5");
                headers.put("Accept-Encoding", "gzip, deflate");
                headers.put("Connection", "keep-alive");

                // Add longer delay between requests
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                downloadManager.downloadFile(tileUrl, tileFile, null, headers);
                downloadManager.setDownloadCallback(new DownloadManager.DownloadCallback() {
                    @Override
                    public void onProgress(String url, int progress) {

                    }

                    @Override
                    public void onComplete(String url, File file) {
                        if (url.equals(tileUrl)) {
                            // Save tile info to database
                            OfflineMapTileEntity tileEntity = new OfflineMapTileEntity();
                            tileEntity.setTrailId(trail.getId());
                            tileEntity.setZoomLevel(currentZoom);
                            tileEntity.setX(tile.x);
                            tileEntity.setY(tile.y);
                            tileEntity.setTilePath(file.getAbsolutePath());
                            tileEntity.setDownloadTimestamp(System.currentTimeMillis());
                            tileEntity.setValid(true);

                            database.offlineMapDao().insertTile(tileEntity);

                            // Update progress
                            progress.incrementCompleted();

                            if (downloadListener != null) {
                                downloadListener.onTileDownloaded(currentZoom, tile.x, tile.y);
                                downloadListener.onDownloadProgress(
                                        progress.completedTiles,
                                        progress.totalTiles,
                                        String.format("Downloading zoom level %d (%d/%d)",
                                                currentZoom, progress.completedTiles, progress.totalTiles)
                                );

                                if (progress.completedTiles == progress.totalTiles) {
                                    downloadListener.onDownloadComplete();
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(String url, DownloadError error, String message) {
                        if (downloadListener != null) {
                            downloadListener.onDownloadError(
                                    String.format("Error downloading tile at zoom %d (%d,%d): %s",
                                            currentZoom, tile.x, tile.y, message)
                            );
                        }
                    }

                    @Override
                    public void onPaused(String url, long bytesDownloaded) {

                    }

                    @Override
                    public void onAllDownloadsPaused() {

                    }

                    @Override
                    public void onConnectionLost() {

                    }

                    @Override
                    public void onConnectionRestored() {

                    }

                    @Override
                    public void onAllDownloadsFinished() {
                        if (downloadListener != null) {
                            downloadListener.onDownloadComplete();
                        }
                    }
                });
            }
        }
    }

    private List<TileCoordinate> calculateTilesInBounds(LatLngBounds bounds, int zoom) {
        List<TileCoordinate> tiles = new ArrayList<>();

        // Convert lat/lon to tile coordinates
        int minX = lonToTileX(bounds.southwest.longitude, zoom);
        int maxX = lonToTileX(bounds.northeast.longitude, zoom);
        int minY = latToTileY(bounds.northeast.latitude, zoom);
        int maxY = latToTileY(bounds.southwest.latitude, zoom);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                tiles.add(new TileCoordinate(x, y));
            }
        }

        return tiles;
    }

    private int lonToTileX(double lon, int zoom) {
        return (int) Math.floor((lon + 180) / 360 * (1 << zoom));
    }

    private int latToTileY(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        return (int) Math.floor((1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2 * (1 << zoom));
    }

    private File createTempFile(String filename) {
        return new File(context.getCacheDir(), filename);
    }
}