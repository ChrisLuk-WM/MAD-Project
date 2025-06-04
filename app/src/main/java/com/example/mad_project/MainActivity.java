package com.example.mad_project;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_project.content_downloader.HikingTrailImageDownloader;
import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.utils.DownloadManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AppDatabase database;
    private HikingTrailImageDownloader imageDownloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadManager.getInstance(this);

        // Initialize database and downloader
        database = AppDatabase.getDatabase(this);
        imageDownloader = new HikingTrailImageDownloader(this);

        // Load data
        imageDownloader.loadTrailsData();

        // Observe data
        observeTrailsData();
    }

    private void observeTrailsData() {
        database.trailDao().getAllTrails().observe(this, trails -> {
            if (trails != null && !trails.isEmpty()) {
                Log.d(TAG, "Loaded " + trails.size() + " trails from database");
                updateUI(trails);
            }
        });
    }

    private void updateUI(List<TrailEntity> trails) {
        if (!trails.isEmpty()) {
            TrailEntity firstTrail = trails.get(0);
            Log.d(TAG, "First trail: " + firstTrail.getTrailName() +
                    ", Difficulty: " + firstTrail.getDifficultyRating() +
                    ", Sight: " + firstTrail.getSightRating());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageDownloader.shutdown();
    }
}