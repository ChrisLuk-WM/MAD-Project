package com.example.mad_project.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity(tableName = "hiking_sessions",
        foreignKeys = @ForeignKey(entity = TrailEntity.class,
                parentColumns = "id",
                childColumns = "trailId",
                onDelete = ForeignKey.CASCADE))
public class HikingSessionEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long trailId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int steps;
    private double distance;
    private double averageSpeed;
    private int totalElevationGain;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTrailId() { return trailId; }
    public void setTrailId(long trailId) { this.trailId = trailId; }

    public LocalDateTime  getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(double averageSpeed) { this.averageSpeed = averageSpeed; }

    public int getTotalElevationGain() { return totalElevationGain; }
    public void setTotalElevationGain(int totalElevationGain) { this.totalElevationGain = totalElevationGain; }

    public long getDuration() {
        if (startTime != null) {
            LocalDateTime endTimeOrNow = endTime != null ? endTime : LocalDateTime.now();
            return java.time.Duration.between(startTime, endTimeOrNow).toMillis();
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HikingSessionEntity that = (HikingSessionEntity) o;

        return id == that.id &&
                trailId == that.trailId &&
                steps == that.steps &&
                Double.compare(that.distance, distance) == 0 &&
                Double.compare(that.averageSpeed, averageSpeed) == 0 &&
                totalElevationGain == that.totalElevationGain &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, trailId, startTime, endTime, steps, distance,
                averageSpeed, totalElevationGain);
    }
}