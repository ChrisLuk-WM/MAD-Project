package com.example.mad_project.content_downloader;

import static com.example.mad_project.utils.Common.getFileName;
import static com.example.mad_project.utils.WeatherWarningUtils.getWarningList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.mad_project.constants.DownloadError;
import com.example.mad_project.constants.DownloadState;
import com.example.mad_project.utils.DownloadManager;
import com.example.mad_project.utils.WeatherWarningUtils;
import com.example.mad_project.utils.WeatherWarningUtils.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherIconDownloader {
    private static final String TAG = "WeatherIconDownloader";
    private static final String WEATHER_ICONS_URL = "https://www.hko.gov.hk/textonly/v2/explain/wxicon_e.htm";
    private static final String BASE_IMAGE_URL = "https://www.hko.gov.hk/images/HKOWxIconOutline/";
    private static WeatherIconDownloader instance;
    private final Context context;
    private final Map<Integer, WeatherIconInfo> iconInfoMap;
    private final ExecutorService executorService;
    private final DownloadManager downloadManager;
    private DownloadManager.DownloadCallback downloadCallback;


    private static final Map<String, WeatherWarningUtils.WarningConfig> WARNING_ICON_NAMES = getWarningList();

    public static class WeatherIconInfo {
        private final Bitmap iconBitmap;
        private final String caption;

        public WeatherIconInfo(Bitmap iconBitmap, String caption) {
            this.iconBitmap = iconBitmap;
            this.caption = caption;
        }

        public Bitmap getIconBitmap() { return iconBitmap; }
        public String getCaption() { return caption; }
    }

    private WeatherIconDownloader(Context context) {
        this.context = context.getApplicationContext();
        this.iconInfoMap = new HashMap<>();
        this.executorService = Executors.newSingleThreadExecutor();
        this.downloadManager = DownloadManager.getInstance(context);
        setupDownloadCallbacks();
    }

    public static synchronized WeatherIconDownloader getInstance(Context context) {
        if (instance == null) {
            instance = new WeatherIconDownloader(context);
        }
        return instance;
    }

    public WeatherIconInfo getWarningIcon(String warningText) {
        // Get the warning config from the utils
        WeatherWarningUtils.WarningConfig warningConfig = WARNING_ICON_NAMES.get(warningText);

        if (warningConfig != null) {
            try {
                // Load bitmap directly from assets using the imagePath from warningConfig
                Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open(warningConfig.imagePath));
                if (bitmap != null) {
                    return new WeatherIconInfo(bitmap, warningText);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error loading warning icon from assets: " + warningConfig.imagePath, e);
            }
        }
        return null;
    }

    private void setupDownloadCallbacks() {
        downloadManager.setDownloadCallback(new DownloadManager.DownloadCallback() {
            @Override
            public void onProgress(String url, int progress) {
                if (downloadCallback != null) {
                    downloadCallback.onProgress(url, progress);
                }
            }

            @Override
            public void onComplete(String url, File file) {
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
                if (downloadCallback != null) {
                    downloadCallback.onPaused(url, bytesDownloaded);
                }
            }

            @Override
            public void onAllDownloadsPaused() {
                if (downloadCallback != null) {
                    downloadCallback.onAllDownloadsPaused();
                }
            }

            @Override
            public void onConnectionLost() {
                if (downloadCallback != null) {
                    downloadCallback.onConnectionLost();
                }
            }

            @Override
            public void onConnectionRestored() {
                if (downloadCallback != null) {
                    downloadCallback.onConnectionRestored();
                }
            }

            @Override
            public void onAllDownloadsFinished() {
                if (downloadCallback != null) {
                    downloadCallback.onAllDownloadsFinished();
                }
            }
        });
    }

    public void updateWeatherIcons(OnUpdateCompleteListener listener) {
        executorService.execute(() -> {
            try {
                String dirPath = "weather_icon";
                File directory = new File(context.getFilesDir(), dirPath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                Document doc = Jsoup.connect(WEATHER_ICONS_URL).get();
                Elements rows = doc.select("table tr");

                for (Element row : rows) {
                    Elements cells = row.select("td");
                    if (cells.size() >= 3) {
                        String numberStr = cells.get(0).text().trim();
                        try {
                            int iconCode = Integer.parseInt(numberStr);
                            String imageUrl = extractImageUrl(cells.get(1));
                            String caption = cells.get(2).text().trim();

                            if (imageUrl != null) {
                                String fileName = "icon_" + iconCode + ".png";
                                File iconFile = new File(directory, fileName);

                                String fullUrl = BASE_IMAGE_URL + imageUrl;
                                downloadManager.downloadFile(fullUrl, iconFile, null);

                                // Load bitmap and store in memory
                                Bitmap bitmap = BitmapFactory.decodeFile(iconFile.getAbsolutePath());
                                if (bitmap != null) {
                                    iconInfoMap.put(iconCode, new WeatherIconInfo(bitmap, caption));
                                }
                            }
                        } catch (NumberFormatException e) {
                            continue;
                        }
                    }
                }

                if (listener != null) {
                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                    mainHandler.post(() -> listener.onUpdateComplete(true, null));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating weather icons", e);
                if (listener != null) {
                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                    mainHandler.post(() -> listener.onUpdateComplete(false, e.getMessage()));
                }
            }
        });
    }

    public WeatherIconInfo getWeatherIcon(int iconCode) {
        WeatherIconInfo iconInfo = iconInfoMap.get(iconCode);
        if (iconInfo == null) {
            // Check if file exists in internal storage
            File iconFile = new File(new File(context.getFilesDir(), "weather_icon"),
                    "icon_" + iconCode + ".png");
            if (iconFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(iconFile.getAbsolutePath());
                if (bitmap != null) {
                    iconInfo = new WeatherIconInfo(bitmap, "");
                    iconInfoMap.put(iconCode, iconInfo);
                }
            } else {
                updateWeatherIcons(null);
                return null;
            }
        }
        return iconInfo;
    }

    private String extractImageUrl(Element cell) {
        Element img = cell.select("img").first();
        if (img != null) {
            String src = img.attr("src");
            return src.substring(src.lastIndexOf("/") + 1);
        }
        return null;
    }

    public void setDownloadCallback(DownloadManager.DownloadCallback callback) {
        this.downloadCallback = callback;
    }

    public interface OnUpdateCompleteListener {
        void onUpdateComplete(boolean success, String error);
    }

    public void shutdown() {
        executorService.shutdown();
        // Clear bitmaps from memory
        for (WeatherIconInfo info : iconInfoMap.values()) {
            if (info.getIconBitmap() != null && !info.getIconBitmap().isRecycled()) {
                info.getIconBitmap().recycle();
            }
        }
        iconInfoMap.clear();
    }
}