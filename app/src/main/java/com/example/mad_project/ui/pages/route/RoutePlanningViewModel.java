package com.example.mad_project.ui.pages.route;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.database.entities.TrailImage;

public class RoutePlanningViewModel extends AndroidViewModel {  // Change to AndroidViewModel
    private final AppDatabase database;
    private final ExecutorService executor;
    private final MutableLiveData<List<TrailWithThumbnail>> trailsWithThumbnails;

    public RoutePlanningViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getDatabase(application);
        executor = Executors.newSingleThreadExecutor();
        trailsWithThumbnails = new MutableLiveData<>();

        loadTrailsWithThumbnails();
    }

    private void loadTrailsWithThumbnails() {
        database.trailDao().getAllTrails().observeForever(trails -> {
            executor.execute(() -> {
                List<TrailWithThumbnail> trailsList = new ArrayList<>();
                if (trails != null) {
                    for (TrailEntity trail : trails) {
                        TrailImage thumbnail = database.trailImageDao()
                                .getTrailThumbnail(trail.getId());
                        trailsList.add(new TrailWithThumbnail(trail, thumbnail));
                    }
                }
                trailsWithThumbnails.postValue(trailsList);
            });
        });
    }

    public LiveData<List<TrailWithThumbnail>> getTrailsWithThumbnails() {
        return trailsWithThumbnails;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    // Factory class
    public static class Factory extends ViewModelProvider.AndroidViewModelFactory {
        private final Application application;

        public Factory(@NonNull Application application) {
            super(application);
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(RoutePlanningViewModel.class)) {
                return (T) new RoutePlanningViewModel(application);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}