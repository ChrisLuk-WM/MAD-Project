package com.example.mad_project.sensors;

import android.content.Context;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.CancellationSignal;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class FingerprintHandler {
    private static final String TAG = "FingerprintHandler";
    private final Context context;
    private final BiometricManager biometricManager;
    private CancellationSignal cancellationSignal;
    private boolean isVerified = false;

    public interface FingerprintCallback {
        void onSuccess();
        void onError(String error);
    }

    public FingerprintHandler(Context context) {
        this.context = context;
        this.biometricManager = BiometricManager.from(context);
    }

    public boolean isAvailable() {
        int result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public void authenticate(@NonNull FragmentActivity activity, FingerprintCallback callback) {
        if (isVerified) {
            callback.onSuccess();
            return;
        }

        if (!isAvailable()) {
            callback.onError("Fingerprint authentication is not available");
            return;
        }

        cancellationSignal = new CancellationSignal();

        BiometricPrompt.AuthenticationCallback authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                isVerified = true;
                callback.onSuccess();
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                callback.onError(errString.toString());
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                callback.onError("Authentication failed");
            }
        };

        androidx.biometric.BiometricPrompt biometricPrompt = new androidx.biometric.BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(context),
                new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        isVerified = true;
                        callback.onSuccess();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        callback.onError(errString.toString());
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        callback.onError("Authentication failed");
                    }
                });

        androidx.biometric.BiometricPrompt.PromptInfo promptInfo = new androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authentication Required")
                .setSubtitle("Please verify your identity to access profile")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    public void reset() {
        isVerified = false;
        if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
            cancellationSignal.cancel();
        }
    }

    public boolean isVerified() {
        return isVerified;
    }
}