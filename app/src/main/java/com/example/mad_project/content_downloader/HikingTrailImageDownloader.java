package com.example.mad_project.content_downloader;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.mad_project.constants.DownloadError;
import com.example.mad_project.constants.DownloadState;
import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.database.entities.TrailImage;
import com.example.mad_project.utils.CacheManager;
import com.example.mad_project.utils.Common;
import com.example.mad_project.utils.DownloadManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
public class HikingTrailImageDownloader {
    private static final String TAG = "HikingTrailImageDownloader";
    private final Context context;
    private final ExecutorService executorService;
    private static final String BASE_URL = "https://www.oasistrek.com/";
    private final AppDatabase database;
    private final DownloadManager downloadManager;
    private final CacheManager cacheManager;
    private static volatile boolean isDownloadPaused = true;
    private final MapContentHandler mapContentHandler;
    private DownloadManager.DownloadCallback downloadCallback;

    private static final List<DownloadStateListener> listeners = new ArrayList<>();

    public HikingTrailImageDownloader(Context context) {
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(4);
        this.database = AppDatabase.getDatabase(context);
        this.downloadManager = DownloadManager.getInstance(context);
        this.cacheManager = new CacheManager(context);
        this.mapContentHandler = new MapContentHandler(context);

        setupDownloadCallbacks();
    }

    public void setDownloadCallback(DownloadManager.DownloadCallback callback) {
        downloadManager.setDownloadCallback(callback);
    }
    private void setupDownloadCallbacks() {
        downloadManager.setDownloadCallback(new DownloadManager.DownloadCallback() {
            @Override
            public void onProgress(String url, int progress) {
                isDownloadPaused = false;
                notifyListeners();
                if (downloadCallback != null) {
                    downloadCallback.onProgress(url, progress);
                }
            }

            @Override
            public void onComplete(String url, File file) {
                isDownloadPaused = false;
                notifyListeners();
                if (downloadCallback != null) {
                    downloadCallback.onComplete(url, file);
                }

            }

            @Override
            public void onError(String url, DownloadError error, String message) {
                if (downloadCallback != null) {
                    downloadCallback.onError(url, error, message);
                }
            }

            @Override
            public void onPaused(String url, long bytesDownloaded) {
                isDownloadPaused = true;
                notifyListeners();
                if (downloadCallback != null) {
                    downloadCallback.onPaused(url, bytesDownloaded);
                }
            }

            @Override
            public void onAllDownloadsPaused() {
                isDownloadPaused = true;
                notifyListeners();
                if (downloadCallback != null) {
                    downloadCallback.onAllDownloadsPaused();
                }
            }

            @Override
            public void onConnectionLost() {
                isDownloadPaused = true;
                notifyListeners();
                if (downloadCallback != null) {
                    downloadCallback.onConnectionLost();
                }
            }

            @Override
            public void onConnectionRestored() {
                isDownloadPaused = false;
                notifyListeners();
                if (downloadCallback != null) {
                    downloadCallback.onConnectionRestored();
                }
            }

            @Override
            public void onAllDownloadsFinished() {
                isDownloadPaused = true;
                notifyListeners();
                if (downloadCallback != null) {
                    downloadCallback.onConnectionLost();
                }
            }
        });
    }

    private File getDestinationFile(String url) {
        String fileName = getFileNameFromUrl(url);
        String dirPath = "hiking_trail/images";
        File directory = new File(context.getFilesDir(), dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return new File(directory, fileName);
    }

    public void loadTrailsData() {
        executorService.execute(() -> {
            try {
                // Check download status
                Map<String, DownloadState> downloadStates = cacheManager.getAllDownloadStates();
                boolean hasIncompleteDownloads = downloadStates.values().stream()
                        .anyMatch(state -> state != DownloadState.COMPLETED);

                if (hasIncompleteDownloads) {
                    notifyListeners();
                    resumeIncompleteDownloads(downloadStates);
                } else {
                    List<TrailEntity> existingTrails = database.trailDao().getAllTrailsList();
                    boolean needsUpdate = existingTrails == null ||
                            existingTrails.isEmpty() ||
                            existingTrails.stream().anyMatch(trail -> !trail.isInited());

                    if (needsUpdate) {
                        notifyListeners();
                        startFreshDownload();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading trails data", e);
                notifyUserOnMainThread("Error loading trails data");
            }
        });
    }

    private void resumeIncompleteDownloads(Map<String, DownloadState> downloadStates) {
        for (Map.Entry<String, DownloadState> entry : downloadStates.entrySet()) {
            if (entry.getValue() != DownloadState.COMPLETED) {
                String url = entry.getKey();
                Long resumePosition = cacheManager.getDownloadedBytes(url);
                File destination = getDestinationFile(url);
                downloadManager.downloadFile(url, destination, resumePosition);
            }
        }
    }

    private void startFreshDownload() {
        JSONObject jsonObject = Common.loadJsonFromAsset(context, "trails.json");
        if (jsonObject != null) {
            processTrailData(jsonObject);
            notifyUserOnMainThread("Started downloading trail data and images");
        }
    }

    // Method to add listeners
    public static void addDownloadStateListener(DownloadStateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    // Method to remove listeners
    public static void removeDownloadStateListener(DownloadStateListener listener) {
        listeners.remove(listener);
    }

    public static boolean isDownloading() {
        // Log.d("Downloading: ", isDownloadPaused? "False": "True");
        return !isDownloadPaused;
    }

    // Modified pause/resume methods to notify listeners
    public void pauseDownloads() {
        isDownloadPaused = true;
        downloadManager.pauseAll();
        notifyListeners();
    }

    public void resumeDownloads() {
        isDownloadPaused = false;
        downloadManager.resumeAll();
        notifyListeners();
    }

    private static void notifyListeners() {
        for (DownloadStateListener listener : listeners) {
            listener.onDownloadStateChanged(isDownloading());
        }
    }

    public void clearCache() {
        cacheManager.clearCache();
    }

    private void notifyUserOnMainThread(String message) {
        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
        mainHandler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    private void processTrailData(JSONObject jsonObject) {
        try {
            Iterator<String> keys = jsonObject.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject trailObject = jsonObject.getJSONObject(key);
                TrailEntity trail = parseTrailObject(trailObject);

                // Insert trail into database and get its ID
                long trailId = database.trailDao().insert(trail);

                // Download and save images
                downloadImagesForTrail(trail, trailId);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON", e);
        }
    }

    private TrailEntity parseTrailObject(JSONObject object) throws JSONException {
        return new TrailEntity(
                object.getString("trail"),
                object.getDouble("sight"),
                object.getDouble("difficulty"),
                object.getDouble("length"),
                object.getDouble("duration"),
                object.getString("url")
        );
    }

    private void downloadImagesForTrail(TrailEntity trail, long trailId) {
        executorService.execute(() -> {
            try {
                String dirPath = "hiking_trail/" + trail.getTrailName();
                File directory = new File(context.getFilesDir(), dirPath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                Document doc = Jsoup.connect(trail.getSourceUrl()).get();
                Elements images = doc.select("img");

                for (Element img : images) {
                    String imgUrl = img.attr("src");
                    String strAlt = img.attr("alt");

                    // Check if this image should be downloaded
                    if (strAlt.isEmpty()) {
                        // Extract trail URL identifier (e.g., "po_pin_chau" from "https://www.oasistrek.com/po_pin_chau.php")
                        String trailUrlId = extractTrailUrlId(trail.getSourceUrl());
                        if (!imgUrl.contains(trailUrlId)) {
                            continue; // Skip if image URL doesn't contain trail identifier
                        }
                    }

                    if (!imgUrl.startsWith("http")) {
                        imgUrl = BASE_URL + imgUrl;
                    }

                    String fileName = getFileNameFromUrl(imgUrl);
                    File imageFile = new File(directory, fileName);

                    // Download the image
                    downloadManager.downloadFile(imgUrl, imageFile, null);

                    // Get description for the image
                    String description = getImageDescription(img, strAlt);

                    // Save image info to database
                    TrailImage trailImage = new TrailImage();
                    trailImage.setTrailId(trailId);
                    trailImage.setImagePath(imageFile.getAbsolutePath());
                    trailImage.setImageUrl(imgUrl);
                    trailImage.setDownloadTime(System.currentTimeMillis());
                    trailImage.setThumbnail(fileName.contains("cover"));
                    trailImage.setDescription(description);

                    database.trailImageDao().insert(trailImage);
                }

                // Extract coordinates from the page
                Elements coordElements = doc.select("div.coordinates, div.gps-info");
                for (Element coordElement : coordElements) {
                    String coordText = coordElement.text();
                    // Look for patterns like "22.123456, 114.123456" or similar
                    Pattern pattern = Pattern.compile("(\\d+\\.\\d+)°?\\s*[,N]\\s*(\\d+\\.\\d+)°?\\s*[E]");
                    Matcher matcher = pattern.matcher(coordText);
                    if (matcher.find()) {
                        double lat = Double.parseDouble(matcher.group(1));
                        double lng = Double.parseDouble(matcher.group(2));
                        trail.setLatitude(lat);
                        trail.setLongitude(lng);
                        database.trailDao().update(trail);
                        break;
                    }
                }


                Elements mapLinks = doc.select("a span.icon.map").parents();
                for (Element mapLink : mapLinks) {
                    String mapUrl = mapLink.attr("href");
                    trail.setId(trailId);
                    mapContentHandler.handleMapContent(trail, mapUrl);
                }


            } catch (IOException e) {
                Log.e(TAG, "Error downloading images for trail: " + trail.getTrailName(), e);
            }
        });
    }

    private String extractTrailUrlId(String url) {
        // Extract the trail identifier from URL
        // e.g., "https://www.oasistrek.com/po_pin_chau.php" -> "po_pin_chau"
        String[] parts = url.split("/");
        String lastPart = parts[parts.length - 1];
        return lastPart.replace(".php", "");
    }

    private String getImageDescription(Element img, String altText) {
        if (!altText.isEmpty()) {
            return altText;
        }

        // Find parent div
        Element parentDiv = img.parent();
        if (!"div".equals(parentDiv.tagName())) {
            parentDiv = img.parents().select("div").first();
        }

        if (parentDiv == null) {
            return "Unknown";
        }

        // Get all images in this div
        Elements siblingsImages = parentDiv.select("img");
        int imageIndex = siblingsImages.indexOf(img);
        int totalImages = siblingsImages.size();

        // Find the figcaption
        Element figcaption = parentDiv.nextElementSibling();
        if (figcaption == null || !"figcaption".equals(figcaption.tagName())) {
            return "Unknown";
        }

        String captionText = figcaption.text().trim();
        int spaceCount = countSpaces(captionText);

        // Case 1: n-1 spaces
        if (spaceCount == totalImages - 1) {
            String[] parts = captionText.split(" ");
            return (imageIndex < parts.length) ? parts[imageIndex] : "Unknown";
        }

        // Case 2: (n*2)-1 spaces
        if (spaceCount == (totalImages * 2) - 1) {
            String[] parts = captionText.split("  "); // Split by double space
            return (imageIndex < parts.length) ? parts[imageIndex].trim() : "Unknown";
        }

        // Case 3: ≤ 1 space
        if (spaceCount <= 1) {
            return captionText + (imageIndex + 1);
        }

        // Case 4: default case
        return captionText;
    }

    private int countSpaces(String text) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == ' ') count++;
        }
        return count;
    }


    private String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public DownloadState getTrailDownloadState(String trailUrl) {
        Map<String, DownloadState> states = cacheManager.getAllDownloadStates();
        return states.getOrDefault(trailUrl, DownloadState.NOT_STARTED);
    }

    public boolean areAllDownloadsComplete() {
        Map<String, DownloadState> states = cacheManager.getAllDownloadStates();
        return states.values().stream()
                .allMatch(state -> state == DownloadState.COMPLETED);
    }
}