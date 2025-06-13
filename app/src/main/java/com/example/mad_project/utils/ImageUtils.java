package com.example.mad_project.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ImageUtils {
    private static final String PROFILE_ICONS_DIR = "profile_icons";
    private static final String TAG = "ImageUtils";

    public static File getProfileIconsDir(Context context) {
        File directory = new File(context.getFilesDir(), PROFILE_ICONS_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    public static String saveProfilePhoto(Context context, Uri imageUri) {
        try {
            // Generate hash from image data
            String hash = generateImageHash(context, imageUri);
            if (hash == null) return null;

            // Create file with hash name
            String fileName = hash + ".jpg";
            File outputFile = new File(getProfileIconsDir(context), fileName);

            // If file already exists, return the filename
            if (outputFile.exists()) {
                return fileName;
            }

            // Save the image
            Bitmap bitmap = getBitmapFromUri(context, imageUri);
            if (bitmap == null) return null;

            FileOutputStream fos = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            bitmap.recycle();

            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "Error saving profile photo", e);
            return null;
        }
    }

    private static String generateImageHash(Context context, Uri imageUri) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            inputStream.close();

            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            Log.e(TAG, "Error generating image hash", e);
            return null;
        }
    }

    public static Bitmap loadProfilePhoto(Context context, String fileName) {
        if (fileName == null) return null;

        File file = new File(getProfileIconsDir(context), fileName);
        if (!file.exists()) return null;

        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    private static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            if (input == null) return null;

            // Decode image size first
            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
            input.close();

            // Calculate sample size
            int sampleSize = calculateInSampleSize(onlyBoundsOptions, 512, 512);

            // Decode with inSampleSize
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = sampleSize;
            input = context.getContentResolver().openInputStream(uri);
            if (input == null) return null;

            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            input.close();
            return bitmap;
        } catch (IOException e) {
            Log.e(TAG, "Error loading bitmap from uri", e);
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static void deleteProfilePhoto(Context context, String fileName) {
        if (fileName == null) return;

        File file = new File(getProfileIconsDir(context), fileName);
        if (file.exists()) {
            file.delete();
        }
    }
}