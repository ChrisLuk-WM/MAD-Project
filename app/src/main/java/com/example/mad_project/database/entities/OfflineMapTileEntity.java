// OfflineMapTileEntity.java
package com.example.mad_project.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "offline_map_tiles",
        foreignKeys = @ForeignKey(entity = TrailEntity.class,
                parentColumns = "id",
                childColumns = "trailId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = {"trailId", "zoomLevel", "x", "y"}, unique = true)})
public class OfflineMapTileEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long trailId;
    private int zoomLevel;
    private int x;  // tile x coordinate
    private int y;  // tile y coordinate
    private String tilePath;  // path to the tile image file
    private long downloadTimestamp;
    private boolean isValid;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTrailId() { return trailId; }
    public void setTrailId(long trailId) { this.trailId = trailId; }

    public int getZoomLevel() { return zoomLevel; }
    public void setZoomLevel(int zoomLevel) { this.zoomLevel = zoomLevel; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public String getTilePath() { return tilePath; }
    public void setTilePath(String tilePath) { this.tilePath = tilePath; }

    public long getDownloadTimestamp() { return downloadTimestamp; }
    public void setDownloadTimestamp(long downloadTimestamp) { this.downloadTimestamp = downloadTimestamp; }

    public boolean isValid() { return isValid; }
    public void setValid(boolean valid) { isValid = valid; }
}