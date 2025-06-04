package com.example.mad_project.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trails")
public class TrailEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String trailName;
    private double sightRating;
    private double difficultyRating;
    private double lengthRating;
    private double durationRating;
    private String sourceUrl;
    private String imagePath;    // Local storage path for trail images
    private String thumbnailPath; // Path for thumbnail image
    private long lastUpdated;    // Timestamp for data freshness

    // Constructor matching the JSON structure
    public TrailEntity(String trailName, double sightRating, double difficultyRating,
                       double lengthRating, double durationRating, String sourceUrl) {
        this.trailName = trailName;
        this.sightRating = sightRating;
        this.difficultyRating = difficultyRating;
        this.lengthRating = lengthRating;
        this.durationRating = durationRating;
        this.sourceUrl = sourceUrl;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTrailName() { return trailName; }
    public void setTrailName(String trailName) { this.trailName = trailName; }

    public double getSightRating() { return sightRating; }
    public void setSightRating(double sightRating) { this.sightRating = sightRating; }

    public double getDifficultyRating() { return difficultyRating; }
    public void setDifficultyRating(double difficultyRating) { this.difficultyRating = difficultyRating; }

    public double getLengthRating() { return lengthRating; }
    public void setLengthRating(double lengthRating) { this.lengthRating = lengthRating; }

    public double getDurationRating() { return durationRating; }
    public void setDurationRating(double durationRating) { this.durationRating = durationRating; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}