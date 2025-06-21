package com.example.mad_project.database.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.OfflineMapDao;
import com.example.mad_project.database.entities.OfflineMapMetadata;
import com.example.mad_project.database.entities.OfflineMapTileEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class OfflineMapRepository {
    private final OfflineMapDao offlineMapDao;
    private final ExecutorService executorService;

    public OfflineMapRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        offlineMapDao = db.offlineMapDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Metadata operations
    public void insertMetadata(OfflineMapMetadata metadata) {
        executorService.execute(() -> offlineMapDao.insertMetadata(metadata));
    }

    public LiveData<OfflineMapMetadata> getMapMetadata(long trailId) {
        return offlineMapDao.getMapMetadata(trailId);
    }

    public OfflineMapMetadata getMapMetadataSync(long trailId) {
        return offlineMapDao.getMapMetadataSync(trailId);
    }

    // Tile operations
    public void insertTile(OfflineMapTileEntity tile) {
        executorService.execute(() -> offlineMapDao.insertTile(tile));
    }

    public void insertTiles(List<OfflineMapTileEntity> tiles) {
        executorService.execute(() -> offlineMapDao.insertTiles(tiles));
    }

    public OfflineMapTileEntity getTile(long trailId, int zoom, int x, int y) {
        // Create a Future to get the result from the background thread
        Future<OfflineMapTileEntity> future = executorService.submit(() ->
                offlineMapDao.getTile(trailId, zoom, x, y)
        );

        try {
            return future.get(); // Wait for the result
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<OfflineMapTileEntity> getTilesForZoom(long trailId, int zoom) {
        Future<List<OfflineMapTileEntity>> future = executorService.submit(() ->
                offlineMapDao.getTilesForZoom(trailId, zoom)
        );

        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getTileCount(long trailId, int zoom) {
        Future<Integer> future = executorService.submit(() ->
                offlineMapDao.getTileCount(trailId, zoom)
        );

        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void deleteTilesForTrail(long trailId) {
        executorService.execute(() -> offlineMapDao.deleteTilesForTrail(trailId));
    }

    // Cleanup
    public void shutdown() {
        executorService.shutdown();
    }
}