package com.example.mad_project.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(tableName = "hiking_statistics",
        foreignKeys = @ForeignKey(entity = HikingSessionEntity.class,
                parentColumns = "id",
                childColumns = "sessionId",
                onDelete = ForeignKey.CASCADE))
public class HikingStatisticsEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long sessionId;
    private LocalDateTime dateTime;
    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;  // in m/s
    private double accuracy;  // GPS accuracy in meters
    private int steps;  // steps since last record
    private float bearing;  // direction in degrees

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getSessionId() { return sessionId; }
    public void setSessionId(long sessionId) { this.sessionId = sessionId; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getAltitude() { return altitude; }
    public void setAltitude(double altitude) { this.altitude = altitude; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }

    public float getBearing() { return bearing; }
    public void setBearing(float bearing) { this.bearing = bearing; }
}