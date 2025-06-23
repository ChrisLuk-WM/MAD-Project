package com.example.mad_project.sensors;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlashlightHandler {
    private static final String TAG = "FlashlightHandler";

    private final Context context;
    private final CameraManager cameraManager;
    private final Handler mainHandler;
    private String cameraId;
    private boolean isSOSRunning = false;
    private Camera camera; // Add legacy camera support
    private boolean useLegacyCamera = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final long DOT_DURATION = 200;
    private static final long DASH_DURATION = DOT_DURATION * 3;
    private static final long SIGNAL_GAP = DOT_DURATION;
    private static final long LETTER_GAP = DOT_DURATION * 3;
    private static final long WORD_GAP = DOT_DURATION * 7;
    public FlashlightHandler(Context context) {
        this.context = context;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        this.mainHandler = new Handler(Looper.getMainLooper());
        initializeCamera();
    }

    private void initializeCamera() {
        // Try Camera2 API first
        try {
            if (cameraManager != null) {
                String[] cameraIds = cameraManager.getCameraIdList();
                for (String id : cameraIds) {
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                    Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    if (hasFlash != null && hasFlash) {
                        this.cameraId = id;
                        useLegacyCamera = false;
                        return;
                    }
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera2 API not available, trying legacy camera", e);
        }

        // Fallback to legacy camera API
        try {
            camera = Camera.open();
            Camera.Parameters params = camera.getParameters();
            if (params.getFlashMode() != null) {
                useLegacyCamera = true;
                camera.release(); // Release for now, we'll reopen when needed
                camera = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize legacy camera", e);
            useLegacyCamera = false;
            if (camera != null) {
                camera.release();
                camera = null;
            }
        }
    }

    private void flashLight(boolean enable) {
        mainHandler.post(() -> {
            if (useLegacyCamera) {
                flashLightLegacy(enable);
            } else {
                flashLightCamera2(enable);
            }
        });
    }

    private void flashLightCamera2(boolean enable) {
        try {
            if (cameraId == null) {
                Log.e(TAG, "No camera with flash available");
                stopSOS();
                return;
            }
            cameraManager.setTorchMode(cameraId, enable);
        } catch (Exception e) {
            Log.e(TAG, "Failed to toggle flashlight using Camera2 API", e);
            // Try legacy method as fallback
            flashLightLegacy(enable);
        }
    }

    private void flashLightLegacy(boolean enable) {
        try {
            if (camera == null) {
                camera = Camera.open();
            }
            Camera.Parameters params = camera.getParameters();
            params.setFlashMode(enable ?
                    Camera.Parameters.FLASH_MODE_TORCH :
                    Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            if (enable) {
                camera.startPreview();
            } else {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to toggle flashlight using legacy API", e);
            if (camera != null) {
                camera.release();
                camera = null;
            }
            stopSOS();
        }
    }

    public void startSOS() {
        if (!isSOSRunning) {
            isSOSRunning = true;
            executorService.execute(this::runSOSPattern);
        }
    }

    public void stopSOS() {
        isSOSRunning = false;
        mainHandler.post(() -> {
            try {
                flashLight(false);
                if (camera != null) {
                    camera.release();
                    camera = null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error stopping SOS", e);
            }
        });
    }

    private void runSOSPattern() {
        while (isSOSRunning) {
            try {
                // S = ... (3 dots)
                for (int i = 0; i < 3 && isSOSRunning; i++) {
                    flashLight(true);
                    Thread.sleep(DOT_DURATION);
                    flashLight(false);
                    if (i < 2) Thread.sleep(SIGNAL_GAP);
                }
                if (!isSOSRunning) break;
                Thread.sleep(LETTER_GAP);

                // O = --- (3 dashes)
                for (int i = 0; i < 3 && isSOSRunning; i++) {
                    flashLight(true);
                    Thread.sleep(DASH_DURATION);
                    flashLight(false);
                    if (i < 2) Thread.sleep(SIGNAL_GAP);
                }
                if (!isSOSRunning) break;
                Thread.sleep(LETTER_GAP);

                // S = ... (3 dots)
                for (int i = 0; i < 3 && isSOSRunning; i++) {
                    flashLight(true);
                    Thread.sleep(DOT_DURATION);
                    flashLight(false);
                    if (i < 2) Thread.sleep(SIGNAL_GAP);
                }
                if (!isSOSRunning) break;
                Thread.sleep(WORD_GAP);

            } catch (InterruptedException e) {
                Log.e(TAG, "SOS pattern interrupted", e);
                break;
            } catch (Exception e) {
                Log.e(TAG, "Error in SOS pattern", e);
                break;
            }
        }
        stopSOS();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        executorService.shutdown();
    }

    public boolean isSOSRunning() {
        return isSOSRunning;
    }
}