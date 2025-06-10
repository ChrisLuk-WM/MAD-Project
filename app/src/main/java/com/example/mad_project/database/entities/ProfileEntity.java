package com.example.mad_project.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "profiles")
public class ProfileEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    // Basic Info
    private String name;
    private int age;
    private String gender;
    private String email;
    private String phone;

    // Physical Parameters
    private float height; // in cm
    private float weight; // in kg
    private String bloodType;
    private String medicalConditions;

    // Hiking Experience
    private String experienceLevel; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    private int yearsOfExperience;
    private float averageHikingDistance; // in km
    private int hikingFrequency; // hikes per month

    // Preferences
    private boolean prefersSoloHiking;
    private String preferredTerrainType; // MOUNTAIN, FOREST, COASTAL, etc.
    private int preferredDifficulty; // 1-5 scale
    private float preferredHikingDistance; // in km
    private boolean notificationsEnabled;
    private boolean locationTrackingEnabled;
    private boolean emergencyContactsEnabled;

    // Timestamps
    private long createdAt;
    private long updatedAt;

    public int getPreferredDifficulty() {
        return preferredDifficulty;
    }

    public void setPreferredDifficulty(int preferredDifficulty) {
        this.preferredDifficulty = preferredDifficulty;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getMedicalConditions() {
        return medicalConditions;
    }

    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public float getAverageHikingDistance() {
        return averageHikingDistance;
    }

    public void setAverageHikingDistance(float averageHikingDistance) {
        this.averageHikingDistance = averageHikingDistance;
    }

    public int getHikingFrequency() {
        return hikingFrequency;
    }

    public void setHikingFrequency(int hikingFrequency) {
        this.hikingFrequency = hikingFrequency;
    }

    public boolean isPrefersSoloHiking() {
        return prefersSoloHiking;
    }

    public void setPrefersSoloHiking(boolean prefersSoloHiking) {
        this.prefersSoloHiking = prefersSoloHiking;
    }

    public String getPreferredTerrainType() {
        return preferredTerrainType;
    }

    public void setPreferredTerrainType(String preferredTerrainType) {
        this.preferredTerrainType = preferredTerrainType;
    }

    public float getPreferredHikingDistance() {
        return preferredHikingDistance;
    }

    public void setPreferredHikingDistance(float preferredHikingDistance) {
        this.preferredHikingDistance = preferredHikingDistance;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isLocationTrackingEnabled() {
        return locationTrackingEnabled;
    }

    public void setLocationTrackingEnabled(boolean locationTrackingEnabled) {
        this.locationTrackingEnabled = locationTrackingEnabled;
    }

    public boolean isEmergencyContactsEnabled() {
        return emergencyContactsEnabled;
    }

    public void setEmergencyContactsEnabled(boolean emergencyContactsEnabled) {
        this.emergencyContactsEnabled = emergencyContactsEnabled;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}