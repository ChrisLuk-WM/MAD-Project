package com.example.mad_project.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.mad_project.services.HikingRecommendationHelper;

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

    private float weeklyExerciseHours; // Direct user input needed
    private float maxAltitudeClimbed;  // Can be tracked/updated automatically
    private float longestHikeKm;       // Can be tracked/updated automatically
    private int fitnessLevel;          // 1-10 scale, user input
    private String profilePhotoPath;

    // Getters and setters for new fields
    public float getWeeklyExerciseHours() {
        return weeklyExerciseHours;
    }

    public void setWeeklyExerciseHours(float weeklyExerciseHours) {
        this.weeklyExerciseHours = weeklyExerciseHours;
    }

    public float getMaxAltitudeClimbed() {
        return maxAltitudeClimbed;
    }

    public void setMaxAltitudeClimbed(float maxAltitudeClimbed) {
        this.maxAltitudeClimbed = maxAltitudeClimbed;
    }

    public float getLongestHikeKm() {
        return longestHikeKm;
    }

    public void setLongestHikeKm(float longestHikeKm) {
        this.longestHikeKm = longestHikeKm;
    }

    public int getFitnessLevel() {
        return fitnessLevel;
    }

    public void setFitnessLevel(int fitnessLevel) {
        this.fitnessLevel = fitnessLevel;
    }

    // Helper method to convert to HikerProfile
    public HikingRecommendationHelper.HikerProfile toHikerProfile() {
        return new HikingRecommendationHelper.HikerProfile(
                (float) this.age,
                this.weight,
                this.height,
                calculateEffectiveFitnessLevel(),
                (float) this.yearsOfExperience,
                this.weeklyExerciseHours,
                this.maxAltitudeClimbed,
                this.longestHikeKm
        );
    }

    // Helper method to calculate effective fitness level (1-10 scale to 0-1 scale)
    private float calculateEffectiveFitnessLevel() {
        float baseLevel = this.fitnessLevel / 10.0f; // Convert 1-10 scale to 0-1

        // Adjust based on other factors
        float experienceAdjustment = Math.min(this.yearsOfExperience / 10.0f, 0.2f); // Max 20% boost from experience
        float frequencyAdjustment = Math.min(this.hikingFrequency / 20.0f, 0.15f); // Max 15% boost from frequency
        float exerciseAdjustment = Math.min(this.weeklyExerciseHours / 20.0f, 0.15f); // Max 15% boost from exercise

        return Math.min(baseLevel + experienceAdjustment + frequencyAdjustment + exerciseAdjustment, 1.0f);
    }

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

    public String getProfilePhotoPath() {
        return profilePhotoPath;
    }

    public void setProfilePhotoPath(String profilePhotoPath) {
        this.profilePhotoPath = profilePhotoPath;
    }
}