package com.example.mad_project.content_downloader;

import static com.example.mad_project.utils.Common.getExtenstionName;

import android.content.Context;
import android.util.Log;

import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.database.entities.TrailImage;
import com.example.mad_project.utils.DownloadManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;

public class MapContentHandler {
    private static final String TAG = "MapContentHandler";
    private final Context context;
    private final AppDatabase database;
    private final DownloadManager downloadManager;

    public MapContentHandler(Context context) {
        this.context = context;
        this.database = AppDatabase.getDatabase(context);
        this.downloadManager = DownloadManager.getInstance(context);
    }

    public void handleMapContent(TrailEntity trail, String url) {
        if (url == null || url.isEmpty() || trail == null) {
            return;
        }

        // Check if it's a TrailWatch URL or direct image
        if (url.contains("trailwatch.hk")) {
            handleTrailWatchMap(trail, url);
        } else if (url.endsWith(".gif") || url.endsWith(".jpg") ||
                url.endsWith(".jpeg") || url.endsWith(".png")) {
            handleMapImage(trail, url);
        } else {
            Log.w(TAG, "Unsupported map URL format: " + url);
        }
    }

    private void handleMapImage(TrailEntity trail, String imageUrl) {
        try {
            // Create directory for the trail if it doesn't exist
            String dirPath = "hiking_trail/" + trail.getTrailName();
            File directory = new File(context.getFilesDir(), dirPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Create file name from URL
            String fileName = "map" + getExtenstionName(imageUrl);
            File imageFile = new File(directory, fileName);

            // Check if this map image already exists in database
            TrailImage existingImage = database.trailImageDao().getImageByUrl(imageUrl);
            if (existingImage != null) {
                Log.d(TAG, "Map image already exists in database: " + imageUrl);
                return;
            }

            // Download the image
            downloadManager.downloadFile(imageUrl, imageFile, null);

            // Create and save trail image entry
            TrailImage trailImage = new TrailImage();
            trailImage.setTrailId(trail.getId());
            trailImage.setImagePath(imageFile.getAbsolutePath());
            trailImage.setImageUrl(imageUrl);
            trailImage.setDownloadTime(System.currentTimeMillis());
            trailImage.setDescription("Trail Map"); // You might want to customize this

            database.trailImageDao().insert(trailImage);

            trail.setImagePath(imageFile.getAbsolutePath());
            database.trailDao().update(trail);

            Log.d(TAG, "Successfully handled map image for trail: " + trail.getTrailName());

        } catch (Exception e) {
            Log.e(TAG, "Error handling map image: " + e.getMessage());
        }
    }

    private void handleTrailWatchMap(TrailEntity trail, String mapUrl) {
        try {
            // Extract the TrailWatch ID from the URL
            String trailwatchId = extractTrailwatchId(mapUrl);
            if (trailwatchId == null || trailwatchId.isEmpty()) {
                Log.e(TAG, "Failed to extract TrailWatch ID from URL: " + mapUrl);
                return;
            }

            // Construct the static map URL
            String staticMapUrl = "https://static.trailwatch.hk/static/?aid=" + trailwatchId;

//            Document doc = Jsoup.connect(staticMapUrl).get();
//
//            // Extract coordinates from the infoDistance div
//            Element infoDistanceDiv = doc.select("div[id=infoDistance]").first();
//            if (infoDistanceDiv != null) {
//                String lat = infoDistanceDiv.attr("lat");
//                String lon = infoDistanceDiv.attr("lon");
//
//                if (!lat.isEmpty() && !lon.isEmpty()) {
//                    try {
//                        double latitude = Double.parseDouble(lat);
//                        double longitude = Double.parseDouble(lon);
//
//                        // Update trail coordinates
//                        trail.setLatitude(latitude);
//                        trail.setLongitude(longitude);
//                        database.trailDao().update(trail);
//
//                    } catch (NumberFormatException e) {
//                        // Log.e(TAG, "Error parsing coordinates: " + e.getMessage());
//                    }
//                }
//            }

            // Create directory for the trail if it doesn't exist
            String dirPath = "hiking_trail/" + trail.getTrailName();
            File directory = new File(context.getFilesDir(), dirPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Create file name from URL
            String fileName = "map.png"; // TrailWatch static maps are typically JPG
            File imageFile = new File(directory, fileName);

            // Check if this map image already exists in database
            TrailImage existingImage = database.trailImageDao().getImageByUrl(staticMapUrl);
            if (existingImage != null) {
                Log.d(TAG, "Map image already exists in database: " + staticMapUrl);
                return;
            }

            // Download the image
            downloadManager.downloadFile(staticMapUrl, imageFile, null);

            // Create and save trail image entry
            TrailImage trailImage = new TrailImage();
            trailImage.setTrailId(trail.getId());
            trailImage.setImagePath(imageFile.getAbsolutePath());
            trailImage.setImageUrl(staticMapUrl);
            trailImage.setDownloadTime(System.currentTimeMillis());
            trailImage.setDescription("Trail Map"); // You might want to customize this

            database.trailImageDao().insert(trailImage);

            trail.setImagePath(imageFile.getAbsolutePath());
            database.trailDao().update(trail);

            Log.d(TAG, "Successfully handled TrailWatch map for trail: " + trail.getTrailName());

        } catch (Exception e) {
            Log.e(TAG, "Error handling TrailWatch map: " + e.getMessage());
        }
    }

    public String extractTrailwatchId(String mapUrl) {
        try {
            Log.d(TAG, "Extracting ID from URL: " + mapUrl);

            // For URL pattern: https://www.trailwatch.hk/?t=activities&rid=10344284
            if (mapUrl.contains("rid=")) {
                int startIndex = mapUrl.indexOf("rid=") + 4;
                int endIndex = mapUrl.indexOf("&", startIndex);
                if (endIndex == -1) {
                    endIndex = mapUrl.length();
                }
                String id = mapUrl.substring(startIndex, endIndex);
                Log.d(TAG, "Extracted ID using rid= pattern: " + id);
                return id;
            } else if (mapUrl.contains("aid=")) {
                int startIndex = mapUrl.indexOf("aid=") + 4;
                int endIndex = mapUrl.indexOf("&", startIndex);
                if (endIndex == -1) {
                    endIndex = mapUrl.length();
                }
                String id = mapUrl.substring(startIndex, endIndex);
                Log.d(TAG, "Extracted ID using aid= pattern: " + id);
                return id;
            }
            // For URL pattern: https://www.trailwatch.hk/activities/10344284
            else if (mapUrl.contains("/activities/")) {
                int startIndex = mapUrl.indexOf("/activities/") + 11;
                int endIndex = mapUrl.indexOf("?", startIndex);
                if (endIndex == -1) {
                    endIndex = mapUrl.length();
                }
                String id = mapUrl.substring(startIndex, endIndex);
                Log.d(TAG, "Extracted ID using /activities/ pattern: " + id);
                return id;
            }

            Log.e(TAG, "No matching pattern found for URL");
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Error extracting TrailWatch ID: " + e.getMessage());
            return null;
        }
    }
}