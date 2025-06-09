package com.example.mad_project.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.mad_project.R;

import java.io.File;

// MapUtil.java
public class MapUtil {
    public static void loadRouteMap(ImageView mapView, String mapPath, Context context) {
        // Set the background color first
        mapView.setBackgroundColor(context.getResources().getColor(R.color.map_placeholder_background));

        if (isMapServiceAvailable()) {
            loadFromMapService(mapView, mapPath);
        } else if (mapPath != null) {
            try {
                File imgFile = new File(mapPath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    mapView.setImageBitmap(bitmap);
                } else {
                    showPlaceholder(mapView, context);
                }
            } catch (Exception e) {
                showPlaceholder(mapView, context);
            }
        } else {
            showPlaceholder(mapView, context);
        }
    }

    private static boolean isMapServiceAvailable() {
        // To be implemented when map service is added
        return false;
    }

    private static void loadFromMapService(ImageView mapView, String mapPath) {
        // To be implemented when map service is added
    }

    private static void showPlaceholder(ImageView mapView, Context context) {
        // Set a smaller placeholder icon
        ImageView placeholderIcon = new ImageView(context);
        placeholderIcon.setImageResource(R.drawable.ic_map_placeholder);
        placeholderIcon.setLayoutParams(new ViewGroup.LayoutParams(
                dpToPx(context, 48), // 48dp width
                dpToPx(context, 48)  // 48dp height
        ));

        // Center the placeholder icon
        mapView.setScaleType(ImageView.ScaleType.CENTER);
        mapView.setImageDrawable(placeholderIcon.getDrawable());
    }

    private static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
