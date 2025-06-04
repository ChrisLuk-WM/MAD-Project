package com.example.mad_project.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "trail_images",
        foreignKeys = @ForeignKey(
                entity = TrailEntity.class,
                parentColumns = "id",
                childColumns = "trailId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("trailId")})
public class TrailImage {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long trailId;
    private String imagePath;
    private String imageUrl;
    private long downloadTime;
    private boolean isThumbnail;
    private String description;

    // Constructor
    public TrailImage() {
        // Empty constructor required by Room
    }

    // Full constructor
    public TrailImage(long trailId, String imagePath, String imageUrl, boolean isThumbnail, String description) {
        this.trailId = trailId;
        this.imagePath = imagePath;
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail;
        this.downloadTime = System.currentTimeMillis();
        this.description = description;
    }

    // Getters and Setters
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(long downloadTime) {
        this.downloadTime = downloadTime;
    }

    public boolean isThumbnail() {
        return isThumbnail;
    }

    public void setThumbnail(boolean thumbnail) {
        isThumbnail = thumbnail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}