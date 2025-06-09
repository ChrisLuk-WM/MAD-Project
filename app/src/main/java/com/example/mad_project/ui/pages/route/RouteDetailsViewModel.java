package com.example.mad_project.ui.pages.route;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.database.entities.TrailImage;
import com.example.mad_project.sensors.SensorsController;
import com.example.mad_project.statistics.StatisticsCalculator;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RouteDetailsViewModel extends AndroidViewModel {
    private final AppDatabase database;
    private final ExecutorService executor;
    private final MutableLiveData<TrailWithImages> trailWithImages;
    private final long trailId;

    public RouteDetailsViewModel(@NonNull Application application, long trailId) {
        super(application);
        this.trailId = trailId;
        database = AppDatabase.getDatabase(application);
        executor = Executors.newSingleThreadExecutor();
        trailWithImages = new MutableLiveData<>();

        loadTrailWithImages();
    }

    private void loadTrailWithImages() {
        executor.execute(() -> {
            try {
                // Load trail data directly without LiveData wrapper
                TrailEntity trail = database.trailDao().getTrailByIdSync(trailId);
                if (trail != null) {
                    // Load all images for this trail
                    List<TrailImage> images = database.trailImageDao().getTrailImages(trailId);
                    TrailWithImages data = new TrailWithImages(trail, images);

                    // Post value on main thread
                    trailWithImages.postValue(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Handle error
            }
        });
    }

    public LiveData<TrailWithImages> getTrailWithImages() {
        return trailWithImages;
    }

    public void refreshData() {
        loadTrailWithImages();
    }

    // Add method to start hiking session
    public void startHiking(Context context) {
        SensorsController.getInstance(context).startTracking();
        StatisticsCalculator.getInstance(context).startSession();
    }

    // Add method to get personalized suggestions based on user statistics
    public String getPersonalizedSuggestions() {
        // This will be implemented later when we add the calculation logic
        StringBuilder suggestions = new StringBuilder();
        suggestions.append("Based on your hiking history:\n\n");

        TrailEntity trail = trailWithImages.getValue().getTrail();
        if (trail != null) {
            suggestions.append("• This trail's difficulty (")
                    .append(String.format(Locale.getDefault(), "%.1f", trail.getDifficultyRating()))
                    .append(") is suitable for your level\n");

            suggestions.append("• Estimated completion time: ")
                    .append(String.format(Locale.getDefault(), "%.1f hours", trail.getDurationRating()))
                    .append("\n");

            suggestions.append("• Recommended start time: 8:00 AM\n");
            suggestions.append("• Don't forget to bring water and snacks\n");
        }

        return suggestions.toString();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    // Factory class
    public static class Factory extends ViewModelProvider.AndroidViewModelFactory {
        private final Application application;
        private final long trailId;

        public Factory(@NonNull Application application, long trailId) {
            super(application);
            this.application = application;
            this.trailId = trailId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(RouteDetailsViewModel.class)) {
                return (T) new RouteDetailsViewModel(application, trailId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}