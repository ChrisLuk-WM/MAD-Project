package com.example.mad_project.services;

import android.annotation.SuppressLint;

public class RecommendationDetails {
    @SuppressLint("DefaultLocale")
    public static String generateDetailedRecommendation(float[] probabilities, String weatherText, HikingRecommendationHelper.HikerProfile profile) {
        int primaryClass = argmax(probabilities);
        float confidence = probabilities[primaryClass];

        // Determine hiker profile description
        String hikerProfile;
        if (profile.fitnessLevel >= 8 && profile.experienceYears >= 5) {
            hikerProfile = "experienced and very fit hiker";
        } else if (profile.fitnessLevel >= 6 && profile.experienceYears >= 3) {
            hikerProfile = "moderately experienced hiker";
        } else if (profile.fitnessLevel >= 4) {
            hikerProfile = "recreational hiker";
        } else if (profile.age >= 60) {
            hikerProfile = "senior hiker";
        } else {
            hikerProfile = "beginner";
        }

        // Analyze weather conditions
        String weatherLower = weatherText.toLowerCase();
        boolean hasTyphoon = weatherLower.contains("typhoon") || weatherLower.contains("cyclone") ||
                weatherLower.contains("signal no.");
        boolean hasHeavyRain = weatherLower.contains("rainstorm") || weatherLower.contains("torrential") ||
                weatherLower.contains("heavy rain");
        boolean hasExtremeHeat = weatherLower.contains("extreme heat") || weatherLower.contains("very hot") ||
                weatherLower.contains("35 degrees") || weatherLower.contains("37 degrees");
        boolean hasLightning = weatherLower.contains("thunderstorm") || weatherLower.contains("lightning");
        boolean hasPoorVisibility = weatherLower.contains("fog") || weatherLower.contains("mist") ||
                weatherLower.contains("visibility");
        boolean isFineWeather = weatherLower.contains("fine") || weatherLower.contains("sunny") ||
                weatherLower.contains("clear");

        String recommendation;

        if (primaryClass == 0) { // Not Recommended
            if (confidence > 0.9f) {
                if (hasTyphoon) {
                    recommendation = String.format("As a %s, you should absolutely avoid hiking today. " +
                                    "The typhoon conditions pose life-threatening risks with destructive winds " +
                                    "that could easily knock you off mountain paths. Stay indoors for your safety.",
                            hikerProfile);
                } else if (hasHeavyRain) {
                    recommendation = "The current rainstorm conditions make hiking extremely dangerous. " +
                            "Flash floods can occur suddenly in valleys, while landslides are a real risk " +
                            "on steep slopes. Please postpone your hike until conditions improve.";
                } else if (hasExtremeHeat && profile.fitnessLevel < 6) {
                    recommendation = String.format("With your fitness level of %.0f/10, the extreme heat " +
                            "poses a serious health risk. Heat exhaustion can develop rapidly on exposed trails. " +
                            "Even fit hikers struggle in these conditions.", profile.fitnessLevel);
                } else {
                    recommendation = String.format("The weather conditions are currently too hazardous. " +
                            "As a %s, the risks significantly outweigh any benefits. " +
                            "Wait for better conditions to enjoy a safe outdoor experience.", hikerProfile);
                }
            } else {
                recommendation = String.format("Based on current conditions and your profile as a %s, " +
                                "hiking is not advisable today (%.0f%% certainty). Consider indoor alternatives.",
                        hikerProfile, confidence * 100);
            }
        } else if (primaryClass == 1) { // Caution Advised
            if (hasLightning) {
                recommendation = String.format("As a %s, you could hike today but extreme caution is needed. " +
                        "Thunderstorms pose lightning strike risks, especially on ridges. " +
                        "Start early, avoid exposed areas, and be ready to turn back.", hikerProfile);
            } else if (hasPoorVisibility) {
                recommendation = String.format("Hiking is possible but challenging due to poor visibility. " +
                        "With your experience of %.0f years, stick to well-marked trails you know. " +
                        "Bring navigation aids and move slowly.", profile.experienceYears);
            } else {
                recommendation = String.format("You can hike today but should exercise caution. " +
                        "As a %s, choose easier trails than usual and monitor conditions closely. " +
                        "Pack extra water and emergency supplies.", hikerProfile);
            }
        } else { // Recommended
            if (confidence > 0.9f && isFineWeather) {
                recommendation = String.format("Perfect hiking conditions for a %s! " +
                        "Your fitness level of %.0f/10 means you can tackle most trails comfortably. " +
                        "Enjoy the beautiful weather!", hikerProfile, profile.fitnessLevel);
            } else {
                recommendation = String.format("Good hiking conditions today! " +
                                "As a %s, choose trails matching your experience. " +
                                "Today's conditions are favorable (%.0f%% confidence).",
                        hikerProfile, confidence * 100);
            }
        }

        // Add confidence note if uncertain
        if (confidence < 0.6f) {
            recommendation += String.format("\n\nNote: Model confidence is relatively low (%.0f%%), " +
                    "suggesting mixed conditions. Check latest weather updates.", confidence * 100);
        }

        return recommendation;
    }



    public static int argmax(float[] array) {
        int maxIndex = 0;
        float maxValue = array[0];

        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
