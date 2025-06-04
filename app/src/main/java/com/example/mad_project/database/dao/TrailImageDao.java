package com.example.mad_project.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.mad_project.database.entities.TrailImage;

import java.util.List;

@Dao
public interface TrailImageDao {
    @Insert
    long insert(TrailImage image);

    @Update
    void update(TrailImage image);

    @Delete
    void delete(TrailImage image);

    @Query("SELECT * FROM trail_images WHERE trailId = :trailId")
    LiveData<List<TrailImage>> getImagesForTrail(long trailId);

    @Query("SELECT * FROM trail_images WHERE trailId = :trailId AND isThumbnail = 1 LIMIT 1")
    LiveData<TrailImage> getThumbnailForTrail(long trailId);

    @Query("SELECT * FROM trail_images")
    LiveData<List<TrailImage>> getAllImages();

    @Query("DELETE FROM trail_images WHERE trailId = :trailId")
    void deleteAllImagesForTrail(long trailId);
}