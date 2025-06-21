package com.example.mad_project.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "offline_map_metadata",
        foreignKeys = @ForeignKey(entity = TrailEntity.class,
                parentColumns = "id",
                childColumns = "trailId",
                onDelete = ForeignKey.CASCADE))
public class OfflineMapMetadata {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long trailId;
    private String mapBounds;  // JSON string of map bounds
    private int minZoom;
    private int maxZoom;
    private String mapStyle;
    private long downloadTimestamp;
    private boolean isComplete;  // indicates if all tiles are downloaded

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTrailId() {
        return trailId;
    }

    public void setTrailId(long trailId) {
        this.trailId = trailId;
    }

    public String getMapBounds() {
        return mapBounds;
    }

    public void setMapBounds(String mapBounds) {
        this.mapBounds = mapBounds;
    }

    public int getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(int minZoom) {
        this.minZoom = minZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
    }

    public String getMapStyle() {
        return mapStyle;
    }

    public void setMapStyle(String mapStyle) {
        this.mapStyle = mapStyle;
    }

    public long getDownloadTimestamp() {
        return downloadTimestamp;
    }

    public void setDownloadTimestamp(long downloadTimestamp) {
        this.downloadTimestamp = downloadTimestamp;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }
}