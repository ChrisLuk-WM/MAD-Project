package com.example.mad_project.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StatFs;
import android.util.Log;

import com.example.mad_project.constants.DownloadError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadManager {
    public interface DownloadCallback {
        void onProgress(String url, int progress);
        void onComplete(String url, File file);
        void onError(String url, DownloadError error, String message);
        void onPaused(String url, long bytesDownloaded);
        void onAllDownloadsPaused();
        void onConnectionLost();
        void onConnectionRestored();
    }

    private static final String TAG = "DownloadManager";
    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 30000;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY = 5000;
    private static final int MAX_CONCURRENT_DOWNLOADS = 4;

    private static volatile DownloadManager instance;
    private final Context applicationContext;
    private final BlockingQueue<DownloadRequest> downloadQueue;
    private final ConcurrentHashMap<String, DownloadTask> activeDownloads;
    private final AtomicBoolean isPaused;
    private final AtomicInteger activeDownloadsCount;
    private final AtomicBoolean isNetworkAvailable;
    private DownloadCallback downloadCallback;
    private Timer connectionCheckTimer;
    private final Thread[] downloadThreads;
    private final AtomicBoolean isShutdown;
    private static final long AUTO_RESTART_CHECK_INTERVAL = 10000; // 10 seconds
    private Timer autoRestartTimer;

    // Request class to hold download information
    private static class DownloadRequest {
        final String url;
        final File destination;
        final Long resumePosition;

        DownloadRequest(String url, File destination, Long resumePosition) {
            this.url = url;
            this.destination = destination;
            this.resumePosition = resumePosition;
        }
    }

    private DownloadManager(Context context) {
        this.applicationContext = context.getApplicationContext();
        this.downloadQueue = new LinkedBlockingQueue<>();
        this.activeDownloads = new ConcurrentHashMap<>();
        this.isPaused = new AtomicBoolean(false);
        this.activeDownloadsCount = new AtomicInteger(0);
        this.isNetworkAvailable = new AtomicBoolean(true);
        this.isShutdown = new AtomicBoolean(false);
        this.downloadThreads = new Thread[MAX_CONCURRENT_DOWNLOADS];
        this.autoRestartTimer = new Timer();
        startAutoRestartTimer();

        startConnectionMonitoring();
        initializeDownloadThreads();
    }

    private void startAutoRestartTimer() {
        autoRestartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkAndRestartDownloads();
            }
        }, AUTO_RESTART_CHECK_INTERVAL, AUTO_RESTART_CHECK_INTERVAL);
    }

    private void checkAndRestartDownloads() {
        if (isPaused.get()) isPaused.set(false);

        if (!isNetworkAvailable.get() || isPaused.get()) {
            return;
        }

        // Check if we have items in queue but no active downloads
        if (!downloadQueue.isEmpty() && activeDownloads.size() < MAX_CONCURRENT_DOWNLOADS) {
            Log.d(TAG, "Auto-restart: Found pending downloads in queue");
            restartWorkers();
        }
    }

    private void restartWorkers() {
        for (int i = 0; i < downloadThreads.length; i++) {
            Thread thread = downloadThreads[i];
            if (thread == null || !thread.isAlive()) {
                Log.d(TAG, "Restarting worker thread " + i);
                downloadThreads[i] = new Thread(new DownloadWorker(i));
                downloadThreads[i].start();
            }
        }
    }

    public static DownloadManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager(context);
                }
            }
        }
        return instance;
    }

    private void initializeDownloadThreads() {
        for (int i = 0; i < MAX_CONCURRENT_DOWNLOADS; i++) {
            downloadThreads[i] = new Thread(new DownloadWorker(i));
            downloadThreads[i].start();
        }
    }

    private class DownloadWorker implements Runnable {
        private final int workerId;

        DownloadWorker(int id) {
            this.workerId = id;
        }

        @Override
        public void run() {
            while (!isShutdown.get()) {
                try {
                    // If no downloads available, wait for a short time before checking again
                    if (downloadQueue.isEmpty()) {
                        Thread.sleep(1000);
                        continue;
                    }

                    DownloadRequest request = downloadQueue.take();
                    if (isPaused.get()) {
                        downloadQueue.put(request);
                        Thread.sleep(1000);
                        continue;
                    }

                    if (!isNetworkAvailable.get()) {
                        downloadQueue.put(request);
                        Thread.sleep(1000);
                        continue;
                    }

                    Log.d(TAG, "Worker " + workerId + " processing download: " + request.url);
                    DownloadTask task = new DownloadTask(request.url, request.destination, request.resumePosition);

                    synchronized (activeDownloads) {
                        if (activeDownloads.containsKey(request.url)) {
                            Log.d(TAG, "Download already in progress: " + request.url);
                            continue;
                        }
                        activeDownloads.put(request.url, task);
                        activeDownloadsCount.incrementAndGet();
                    }

                    try {
                        Log.d(TAG, "Queue size: " + getQueueSize());
                        task.download();
                    } finally {
                        synchronized (activeDownloads) {
                            activeDownloads.remove(request.url);
                            activeDownloadsCount.decrementAndGet();
                        }
                    }

                } catch (InterruptedException e) {
                    if (isShutdown.get()) break;
                } catch (Exception e) {
                    Log.e(TAG, "Error in download worker " + workerId, e);
                }
            }
        }
    }

    private void startConnectionMonitoring() {
        connectionCheckTimer = new Timer();
        connectionCheckTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkNetworkConnection();
            }
        }, 0, 5000);
    }

    private void checkNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected != isNetworkAvailable.get()) {
            isNetworkAvailable.set(isConnected);
            if (isConnected) {
                if (downloadCallback != null) {
                    downloadCallback.onConnectionRestored();
                }
                resumeAll();
            } else {
                if (downloadCallback != null) {
                    downloadCallback.onConnectionLost();
                }
                pauseAll();
            }
        }
    }

    public void setDownloadCallback(DownloadCallback callback) {
        this.downloadCallback = callback;
    }

    public void downloadFile(String url, File destination, Long resumePosition) {
        try {
            if (activeDownloads.containsKey(url)) {
                Log.d(TAG, "Download already in progress: " + url);
                return;
            }
            DownloadRequest request = new DownloadRequest(url, destination, resumePosition);
            downloadQueue.put(request);
            Log.d(TAG, "Added to download queue: " + url);
        } catch (InterruptedException e) {
            Log.e(TAG, "Failed to add download to queue: " + url, e);
        }
    }

    private void removeDownloadTask(String url) {
        activeDownloads.remove(url);
        if (activeDownloadsCount.decrementAndGet() == 0) {
            if (downloadCallback != null) {
                downloadCallback.onAllDownloadsPaused();
            }
        }
    }

    public void pauseAll() {
        isPaused.set(true);
        for (DownloadTask task : activeDownloads.values()) {
            task.pause();
        }
    }

    public void resumeAll() {
        isPaused.set(false);
//        for (DownloadTask task : activeDownloads.values()) {
//            task.resume();
//        }
    }

    public void cancelAll() {
        downloadQueue.clear();
        for (DownloadTask task : activeDownloads.values()) {
            task.cancel();
        }
        activeDownloads.clear();
    }

    public void shutdown() {
        isShutdown.set(true);
        if (connectionCheckTimer != null) {
            connectionCheckTimer.cancel();
            connectionCheckTimer.purge();
        }
        if (autoRestartTimer != null) {
            autoRestartTimer.cancel();
            autoRestartTimer.purge();
        }
        cancelAll();
        for (Thread thread : downloadThreads) {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
        }
    }

    // Add method to check worker thread status
    public boolean areWorkersActive() {
        for (Thread thread : downloadThreads) {
            if (thread != null && thread.isAlive()) {
                return true;
            }
        }
        return false;
    }

    // Add method to manually trigger restart check
    public void checkAndRestartWorkersNow() {
        checkAndRestartDownloads();
    }

    public boolean isDownloading(String url) {
        return activeDownloads.containsKey(url) || isInQueue(url);
    }

    private boolean isInQueue(String url) {
        return downloadQueue.stream().anyMatch(request -> request.url.equals(url));
    }

    public int getDownloadProgress(String url) {
        DownloadTask task = activeDownloads.get(url);
        if (task != null) {
            return (int) ((task.bytesDownloaded * 100) / task.totalBytes);
        }
        return 0;
    }

    public int getQueueSize() {
        return downloadQueue.size();
    }

    public int getActiveDownloadsCount() {
        return activeDownloadsCount.get();
    }

    private class DownloadTask {
        private final String url;
        private final File destination;
        private long bytesDownloaded;
        private long totalBytes;
        private final AtomicBoolean isCancelled;
        private HttpURLConnection connection;
        private int retryCount = 0;
        private final Object lock = new Object();

        public DownloadTask(String url, File destination, Long resumePosition) {
            this.url = url;
            this.destination = destination;
            this.bytesDownloaded = resumePosition != null ? resumePosition : 0;
            this.isCancelled = new AtomicBoolean(false);
        }

        private void download() throws IOException {
            while (retryCount < MAX_RETRIES && !isCancelled.get()) {
                try {
                    if (!isNetworkAvailable.get()) {
                        handleError(DownloadError.NETWORK_UNAVAILABLE, "Network is unavailable");
                        Thread.sleep(RETRY_DELAY);
                        continue;
                    }

                    synchronized (lock) {
                        connection = (HttpURLConnection) new URL(url).openConnection();
                        connection.setConnectTimeout(CONNECTION_TIMEOUT);
                        connection.setReadTimeout(READ_TIMEOUT);

                        if (bytesDownloaded > 0) {
                            connection.setRequestProperty("Range", "bytes=" + bytesDownloaded + "-");
                        }

                        connection.connect();

                        if (!isStorageAvailable(connection.getContentLength())) {
                            handleError(DownloadError.INSUFFICIENT_STORAGE, "Insufficient storage space");
                            return;
                        }

                        downloadFile();
                        return;
                    }
                } catch (SocketTimeoutException e) {
                    handleError(DownloadError.CONNECTION_TIMEOUT, "Connection timed out");
                } catch (IOException e) {
                    handleError(DownloadError.CONNECTION_LOST, "Connection lost: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    handleError(DownloadError.UNEXPECTED_ERROR, "Download interrupted");
                    break;
                } catch (Exception e) {
                    handleError(DownloadError.UNEXPECTED_ERROR, "Unexpected error: " + e.getMessage());
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }

                retryCount++;
                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            if (retryCount >= MAX_RETRIES) {
                handleError(DownloadError.CONNECTION_LOST, "Max retries exceeded");
            }
        }

        private void downloadFile() throws IOException {
            try (InputStream input = connection.getInputStream();
                 FileOutputStream output = new FileOutputStream(destination, bytesDownloaded > 0)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                totalBytes = connection.getContentLength();
                if (totalBytes <= 0) {
                    totalBytes = Long.MAX_VALUE;
                }
                totalBytes += bytesDownloaded;
                long lastProgressUpdate = System.currentTimeMillis();

                while ((bytesRead = input.read(buffer)) != -1) {
                    if (isCancelled.get() || isPaused.get()) {
                        handlePaused();
                        return;
                    }

                    if (!isNetworkAvailable.get()) {
                        handleError(DownloadError.NETWORK_UNAVAILABLE, "Network connection lost");
                        return;
                    }

                    output.write(buffer, 0, bytesRead);
                    bytesDownloaded += bytesRead;

                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastProgressUpdate >= 500) {
                        int progress = totalBytes == Long.MAX_VALUE ? -1 :
                                (int) ((bytesDownloaded * 100) / totalBytes);
                        notifyProgress(progress);
                        lastProgressUpdate = currentTime;
                    }
                }

                handleComplete();
            }
        }

        private boolean isStorageAvailable(long requiredSpace) {
            StatFs stat = new StatFs(destination.getParentFile().getPath());
            long availableSpace = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
            return availableSpace > requiredSpace;
        }

        private void handleError(DownloadError error, String message) {
            if (downloadCallback != null) {
                downloadCallback.onError(url, error, message);
            }
            if (error != DownloadError.NETWORK_UNAVAILABLE) {
                removeDownloadTask(url);
            }
            pauseDownload();
        }

        private void pauseDownload() {
            synchronized (lock) {
                if (connection != null) {
                    connection.disconnect();
                }
                handlePaused();
            }
        }

        private void handleComplete() {
            if (downloadCallback != null) {
                downloadCallback.onComplete(url, destination);
            }
            removeDownloadTask(url);
        }

        private void handlePaused() {
            if (downloadCallback != null) {
                downloadCallback.onPaused(url, bytesDownloaded);
            }
        }

        private void notifyProgress(int progress) {
            if (downloadCallback != null) {
                downloadCallback.onProgress(url, progress);
            }
        }

        public void pause() {
            isCancelled.set(true);
        }

//        public void resume() {
//            if (isCancelled.get()) {
//                isCancelled.set(false);
//                downloadFile(url, destination, bytesDownloaded);
//            }
//        }

        public void cancel() {
            isCancelled.set(true);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}