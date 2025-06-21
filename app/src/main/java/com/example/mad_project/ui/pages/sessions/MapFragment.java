package com.example.mad_project.ui.pages.sessions;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mad_project.R;
import com.example.mad_project.statistics.StatisticsManager;
import com.example.mad_project.statistics.StatisticsType;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
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
    private final Runnable locationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateLocation();
            handler.postDelayed(this, 1000); // Update every second
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize osmdroid configuration
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        map = view.findViewById(R.id.map);
        setupMap();
    }

    private void setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(18.0); // Closer zoom for hiking

        // Setup location overlay
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), map);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        map.getOverlays().add(locationOverlay);

        // Initial location from StatisticsManager
        updateLocation();
    }

    private void updateLocation() {
        if (!isAdded()) return;

        StatisticsManager statisticsManager = StatisticsManager.getInstance();
        Location location = statisticsManager.getValue(StatisticsType.LOCATION);

        if (location != null) {
            GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
            map.getController().animateTo(point);

            // Update location overlay
            locationOverlay.onLocationChanged(location, null);
        }
    }

    public void drawPath(List<GeoPoint> points) {
        if (points != null && !points.isEmpty()) {
            Polyline line = new Polyline();
            line.setPoints(points);
            line.setWidth(10f);
            line.setColor(requireContext().getColor(R.color.colorPath));

            map.getOverlays().add(line);
            map.invalidate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
        handler.post(locationUpdateRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
        handler.removeCallbacks(locationUpdateRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(locationUpdateRunnable);
    }
}