package com.example.mad_project.ui.pages.sessions.fragments;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mad_project.R;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.statistics.StatisticsType;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {
    private MapView map;
    private MyLocationNewOverlay locationOverlay;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private List<GeoPoint> pathPoints = new ArrayList<>();
    private Polyline pathLine;
    private GeoPoint lastPoint;
    private static final float MIN_DISTANCE_METERS = 5.0f;
    private boolean isRealTimeTracking = false;
    private ImageButton centerLocationButton;

    private final Runnable locationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRealTimeTracking) {
                updateLocation();
                handler.postDelayed(this, 1000);
            }
        }
    };

    public void setRealTimeTracking(boolean isRealTime) {
        this.isRealTimeTracking = isRealTime;
        if (locationOverlay != null) {
            locationOverlay.enableMyLocation();
            if (isRealTime) {
                locationOverlay.enableFollowLocation();
            } else {
                locationOverlay.disableFollowLocation();
            }
        }

        // Update center button icon/behavior based on mode
        if (centerLocationButton != null) {
            centerLocationButton.setImageResource(R.drawable.ic_my_location);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Create and add center location button
        centerLocationButton = new ImageButton(requireContext());
        centerLocationButton.setImageResource(R.drawable.ic_my_location);
        centerLocationButton.setBackgroundResource(R.drawable.circle_button_background);

        // Set fixed size for the button to ensure it's perfectly circular
        int buttonSize = (int) (48 * getResources().getDisplayMetrics().density); // 48dp
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(buttonSize, buttonSize);
        params.gravity = Gravity.END | Gravity.BOTTOM;
        params.setMargins(0, 0,
                (int) (16 * getResources().getDisplayMetrics().density), // 16dp right margin
                (int) (16 * getResources().getDisplayMetrics().density)  // 16dp bottom margin
        );

        // Add elevation/shadow to the button
        centerLocationButton.setElevation(6 * getResources().getDisplayMetrics().density); // 6dp elevation

        ((FrameLayout) view).addView(centerLocationButton, params);

        setupCenterButton();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        map = view.findViewById(R.id.map);
        initializePath();
        setupMap();
    }

    private void setupCenterButton() {
        centerLocationButton.setOnClickListener(v -> {
            if (isRealTimeTracking) {
                centerOnCurrentLocation();
            } else {
                centerOnPathStart();
            }
        });
    }

    private void centerOnPathStart() {
        if (!pathPoints.isEmpty()) {
            GeoPoint startPoint = pathPoints.get(0);
            map.getController().animateTo(startPoint);
        }
    }

    private void initializePath() {
        pathPoints = new ArrayList<>();
        pathLine = new Polyline(map);
        pathLine.setWidth(10f);
        pathLine.setColor(requireContext().getColor(R.color.colorPath));
    }

    private void setupMap() {
        if (map == null) return;

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(18.0);

        // Setup location overlay
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), map);
        locationOverlay.enableMyLocation();
        map.getOverlays().add(locationOverlay);
        map.getOverlays().add(pathLine);

        if (isRealTimeTracking) {
            updateLocation();
        }
    }

    private void centerOnCurrentLocation() {
        Location location = StatisticsManager.getInstance().getValue(StatisticsType.LOCATION);
        if (location != null) {
            GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
            map.getController().animateTo(point);
        }
    }

    private void updateLocation() {
        if (!isAdded() || map == null) return;

        StatisticsManager statisticsManager = StatisticsManager.getInstance();
        Location location = statisticsManager.getValue(StatisticsType.LOCATION);

        if (location != null) {
            GeoPoint newPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

            if (shouldAddPoint(newPoint)) {
                addPointToPath(newPoint);
            }
        }
    }

    private boolean shouldAddPoint(GeoPoint newPoint) {
        if (lastPoint == null) {
            return true;
        }

        float[] results = new float[1];
        Location.distanceBetween(
                lastPoint.getLatitude(), lastPoint.getLongitude(),
                newPoint.getLatitude(), newPoint.getLongitude(),
                results
        );

        return results[0] >= MIN_DISTANCE_METERS;
    }

    private void addPointToPath(GeoPoint point) {
        if (pathPoints == null) {
            pathPoints = new ArrayList<>();
        }
        pathPoints.add(point);
        lastPoint = point;
        updatePath();
    }

    private void updatePath() {
        if (pathLine != null && pathPoints != null) {
            pathLine.setPoints(new ArrayList<>(pathPoints));
            map.invalidate();
        }
    }

    public void drawPath(List<GeoPoint> points) {
        if (points != null && !points.isEmpty() && map != null) {
            pathPoints = new ArrayList<>(points);
            lastPoint = points.get(points.size() - 1);
            updatePath();

            // Center map on the first point and zoom to show the entire path
            if (points.size() > 1) {
                // Calculate bounds of the path
                BoundingBox bounds = BoundingBox.fromGeoPoints(points);
                // Add some padding
                map.zoomToBoundingBox(bounds.increaseByScale(1.2f), true);
            } else {
                map.getController().setCenter(points.get(0));
            }
            map.invalidate();
        }
    }

    public void clearPath() {
        if (pathPoints != null) {
            pathPoints.clear();
            lastPoint = null;
            updatePath();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
        if (isRealTimeTracking) {
            handler.post(locationUpdateRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
        handler.removeCallbacks(locationUpdateRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(locationUpdateRunnable);
        map = null;
        pathLine = null;
    }

    public List<GeoPoint> getPathPoints() {
        return pathPoints != null ? new ArrayList<>(pathPoints) : new ArrayList<>();
    }
}