package com.example.mad_project.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.mad_project.database.entities.OfflineMapMetadata;
import com.example.mad_project.database.entities.OfflineMapTileEntity;

import java.util.List;

@Dao
public interface OfflineMapDao {
    @Insert
    long insertMetadata(OfflineMapMetadata metadata);

    @Insert
    long insertTile(OfflineMapTileEntity tile);

    @Insert
    List<Long> insertTiles(List<OfflineMapTileEntity> tiles);

    @Query("SELECT * FROM offline_map_metadata WHERE trailId = :trailId")
    LiveData<OfflineMapMetadata> getMapMetadata(long trailId);

    @Query("SELECT * FROM offline_map_tiles WHERE trailId = :trailId AND zoomLevel = :zoom AND x = :x AND y = :y")
    OfflineMapTileEntity getTile(long trailId, int zoom, int x, int y);

    @Query("SELECT * FROM offline_map_tiles WHERE trailId = :trailId AND zoomLevel = :zoom")
    List<OfflineMapTileEntity> getTilesForZoom(long trailId, int zoom);

    @Query("SELECT COUNT(*) FROM offline_map_tiles WHERE trailId = :trailId AND zoomLevel = :zoom")
    int getTileCount(long trailId, int zoom);

    @Query("DELETE FROM offline_map_tiles WHERE trailId = :trailId")
    void deleteTilesForTrail(long trailId);

    @Transaction
    @Query("SELECT * FROM offline_map_metadata WHERE trailId = :trailId")
    OfflineMapMetadata getMapMetadataSync(long trailId);
}