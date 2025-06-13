package com.example.mad_project.services;

import android.annotation.SuppressLint;

public class RecommendationDetails {
    @SuppressLint("DefaultLocale")
    public static String generateGeneralRecommendation(float[] probabilities, String weatherText,
                                                       HikingRecommendationHelper.HikerProfile profile) {
        int primaryClass = argmax(probabilities);
        float confidence = probabilities[primaryClass];

        // Determine hiker profile description
        String hikerProfile = getDetailedHikerProfile(profile);

        // Analyze weather conditions
        WeatherAnalysis weather = new WeatherAnalysis(weatherText);

        // Generate general recommendation
        String recommendation = generateGeneralRecommendationText(
                primaryClass, confidence, hikerProfile, profile, weather, probabilities
        );

        // Add confidence-based additional advice
        recommendation += generateConfidenceAdvice(confidence, probabilities);

        // Add general hiking tips based on weather and profile
        if (primaryClass > 0) { // Not "Not Recommended"
            recommendation += generateGeneralHikingTips(profile, weather);
        }

        return recommendation;
    }

    @SuppressLint("DefaultLocale")
    private static String generateGeneralRecommendationText(int primaryClass, float confidence,
                                                            String hikerProfile,
                                                            HikingRecommendationHelper.HikerProfile profile,
                                                            WeatherAnalysis weather,
                                                            float[] probabilities) {
        StringBuilder rec = new StringBuilder();

        if (primaryClass == 0) { // Not Recommended
            rec.append(generateGeneralNotRecommended(confidence, hikerProfile, profile, weather, probabilities));
        } else if (primaryClass == 1) { // Caution Advised
            rec.append(generateGeneralCautionAdvised(confidence, hikerProfile, profile, weather, probabilities));
        } else { // Recommended
            rec.append(generateGeneralRecommended(confidence, hikerProfile, profile, weather, probabilities));
        }

        return rec.toString();
    }

    @SuppressLint("DefaultLocale")
    private static String generateGeneralNotRecommended(float confidence, String hikerProfile,
                                                        HikingRecommendationHelper.HikerProfile profile,
                                                        WeatherAnalysis weather,
                                                        float[] probabilities) {
        StringBuilder rec = new StringBuilder();

        if (confidence > 0.9f) {
            if (weather.hasTyphoon) {
                rec.append(String.format("⚠️ EXTREME DANGER: As a %s, you must not attempt any hiking today. " +
                        "Typhoon conditions with destructive winds make all outdoor activities life-threatening. " +
                        "Stay indoors for your safety.", hikerProfile));
            } else if (weather.hasHeavyRain) {
                rec.append("The current rainstorm conditions make hiking extremely dangerous on any trail. " +
                        "Flash floods and landslides are serious risks in these conditions. " +
                        "Please postpone all hiking plans until the weather improves.");
            } else if (weather.hasExtremeHeat && profile.fitnessLevel < 6) {
                rec.append(String.format("With your fitness level of %.0f/10, the extreme heat poses " +
                                "serious health risks on any hiking trail. Heat exhaustion can develop rapidly " +
                                "in these conditions. Even short, easy trails are inadvisable today.",
                        profile.fitnessLevel));
            } else {
                rec.append(String.format("Current weather conditions are too hazardous for safe hiking. " +
                        "As a %s, it's best to avoid all trails today. Consider indoor exercise " +
                        "alternatives and wait for better conditions.", hikerProfile));
            }
        } else {
            rec.append(String.format("Based on current conditions and your profile as a %s, " +
                            "hiking is not advisable today (%.0f%% certainty). Weather conditions pose " +
                            "risks that outweigh the benefits of outdoor exercise.",
                    hikerProfile, confidence * 100));
        }

        return rec.toString();
    }

    @SuppressLint("DefaultLocale")
    private static String generateGeneralCautionAdvised(float confidence, String hikerProfile,
                                                        HikingRecommendationHelper.HikerProfile profile,
                                                        WeatherAnalysis weather,
                                                        float[] probabilities) {
        StringBuilder rec = new StringBuilder();

        boolean leansNegative = probabilities[0] > probabilities[2];

        if (weather.hasLightning) {
            rec.append(String.format("⚡ LIGHTNING RISK: As a %s, you can hike today but must be extremely cautious:\n" +
                            "• Choose shorter trails close to shelter\n" +
                            "• Start very early to avoid afternoon storms\n" +
                            "• Monitor weather constantly and turn back at first thunder\n" +
                            "• Avoid exposed areas and ridgelines",
                    hikerProfile));
        } else if (weather.hasPoorVisibility) {
            rec.append(String.format("🌫️ LIMITED VISIBILITY: With your %.0f years of experience, hiking is possible but:\n" +
                            "• Stick to familiar, well-marked trails only\n" +
                            "• Keep hikes short and close to trailheads\n" +
                            "• Bring GPS navigation and emergency supplies\n" +
                            "• Consider postponing if visibility worsens",
                    profile.experienceYears));
        } else if (weather.hasModerateConcerns) {
            rec.append(String.format("Current conditions require extra caution for a %s:\n" +
                            "• Choose easier trails than your usual difficulty\n" +
                            "• Keep hikes shorter - under 2 hours recommended\n" +
                            "• Pack extra water and weather protection\n" +
                            "• Have a backup plan if conditions deteriorate",
                    hikerProfile));
        } else {
            rec.append(String.format("Hiking is possible today with proper precautions. As a %s:\n" +
                            "• Start early to avoid weather changes\n" +
                            "• Choose moderate trails within your comfort zone\n" +
                            "• Monitor conditions throughout your hike\n" +
                            "• Inform someone of your plans and expected return",
                    hikerProfile));
        }

        if (leansNegative) {
            rec.append(String.format("\n\n⚠️ Note: Conditions lean toward 'not recommended' (%.0f%% risk). " +
                            "Consider shorter, easier trails or postponing to another day.",
                    probabilities[0] * 100));
        }

        return rec.toString();
    }

    @SuppressLint("DefaultLocale")
    private static String generateGeneralRecommended(float confidence, String hikerProfile,
                                                     HikingRecommendationHelper.HikerProfile profile,
                                                     WeatherAnalysis weather,
                                                     float[] probabilities) {
        StringBuilder rec = new StringBuilder();

        if (confidence > 0.9f && weather.isFineWeather) {
            if (profile.experienceYears < 2) {
                rec.append(String.format("🌟 PERFECT CONDITIONS: Excellent day for hiking! As a %s:\n" +
                                "• Start with easier trails to build experience\n" +
                                "• %s makes this ideal for outdoor activities\n" +
                                "• Your fitness level (%.0f/10) suits moderate trails\n" +
                                "• Consider exploring new trails within your comfort zone",
                        hikerProfile, weather.visibility, profile.fitnessLevel));
            } else if (profile.fitnessLevel >= 8) {
                rec.append(String.format("💪 EXCELLENT CONDITIONS: As a %s, today is perfect for hiking!\n" +
                                "• Weather conditions are ideal for challenging trails\n" +
                                "• Your high fitness level (%.0f/10) opens many options\n" +
                                "• Consider longer or more difficult routes\n" +
                                "• Great day to push your limits safely",
                        hikerProfile, profile.fitnessLevel));
            } else {
                rec.append(String.format("✅ GREAT HIKING WEATHER: Perfect conditions for a %s!\n" +
                                "• %s throughout the day\n" +
                                "• Choose trails matching your fitness level (%.0f/10)\n" +
                                "• Ideal for hikes up to %.0f km based on your experience\n" +
                                "• Enjoy the outdoors - days like this are rare!",
                        hikerProfile, weather.visibility, profile.fitnessLevel,
                        Math.min(profile.longestHikeKm * 0.8, 15)));
            }
        } else if (confidence > 0.75f) {
            rec.append(String.format("Good conditions for hiking! As a %s:\n" +
                            "• Weather is favorable for most trails\n" +
                            "• Your experience level suits moderate hikes\n" +
                            "• Stay aware of any weather changes\n" +
                            "• Great opportunity for outdoor exercise",
                    hikerProfile));
        } else {
            rec.append(String.format("Conditions appear suitable for hiking (%.0f%% confidence):\n" +
                            "• Weather is generally favorable\n" +
                            "• Choose familiar trails for safety\n" +
                            "• Monitor conditions during your hike\n" +
                            "• Enjoy your time outdoors!",
                    confidence * 100));
        }

        if (probabilities[2] > 0.8f && weather.isFineWeather) {
            rec.append("\n\n🎯 Don't miss this opportunity - conditions are nearly perfect for hiking!");
        }

        return rec.toString();
    }

    @SuppressLint("DefaultLocale")
    private static String generateGeneralHikingTips(HikingRecommendationHelper.HikerProfile profile,
                                                    WeatherAnalysis weather) {
        StringBuilder tips = new StringBuilder("\n\n💡 General Hiking Tips:\n");

        // Water recommendations for average hike
        float waterLiters = 1.5f; // Base for 2.5 hour hike
        if (weather.hasExtremeHeat) waterLiters = 2.5f;
        else if (weather.temperature.contains("3") && weather.temperature.contains("degrees")) waterLiters = 2.0f;
        if (profile.fitnessLevel < 5) waterLiters += 0.5f;

        tips.append(String.format("• Water: Bring at least %.1fL for a moderate hike\n", waterLiters));

        // General timing recommendations
        if (weather.hasLightning || weather.hasExtremeHeat) {
            tips.append("• Best time: Start before 6 AM to avoid afternoon weather\n");
        } else if (weather.isFineWeather) {
            tips.append("• Best time: Morning starts offer cooler temperatures\n");
        }

        // Fitness-based recommendations
        if (profile.fitnessLevel < 4) {
            tips.append("• Pace: Take frequent breaks, don't push too hard\n");
            tips.append("• Distance: Start with trails under 5km\n");
        } else if (profile.fitnessLevel >= 7) {
            tips.append("• Options: Your fitness allows for varied trail choices\n");
        }

        // Experience-based tips
        if (profile.experienceYears < 2) {
            tips.append("• Safety: Hike popular, well-marked trails\n");
            tips.append("• Preparation: Download offline maps before starting\n");
        } else if (profile.experienceYears >= 5) {
            tips.append("• Navigation: Your experience allows for more remote trails\n");
        }

        // Weather-specific gear
        if (weather.hasPoorVisibility) {
            tips.append("• Gear: Bring bright clothing and a whistle\n");
        } else if (weather.hasModerateConcerns) {
            tips.append("• Gear: Pack rain protection just in case\n");
        }

        return tips.toString();
    }

    @SuppressLint("DefaultLocale")
    public static String generateDetailedRecommendation(float[] probabilities, String weatherText,
                                                        HikingRecommendationHelper.HikerProfile profile,
                                                        HikingRecommendationHelper.TrailProfile trail) {
        int primaryClass = argmax(probabilities);
        float confidence = probabilities[primaryClass];

        // Determine hiker profile description with more nuance
        String hikerProfile = getDetailedHikerProfile(profile);

        // Analyze weather conditions in detail
        WeatherAnalysis weather = new WeatherAnalysis(weatherText);

        // Analyze trail characteristics
        TrailAnalysis trailInfo = new TrailAnalysis(trail);

        // Generate recommendation based on all factors
        String recommendation = generateRecommendationText(
                primaryClass, confidence, hikerProfile, profile, weather, trailInfo, probabilities, trail
        );

        // Add confidence-based additional advice
        recommendation += generateConfidenceAdvice(confidence, probabilities);

        // Add trail-specific tips if appropriate
        if (primaryClass > 0) { // Not "Not Recommended"
            recommendation += generateTrailTips(trailInfo, profile, weather, trail);
        }

        return recommendation;
    }

    private static String getDetailedHikerProfile(HikingRecommendationHelper.HikerProfile profile) {
        if (profile.fitnessLevel >= 9 && profile.experienceYears >= 10) {
            return "veteran mountaineer";
        } else if (profile.fitnessLevel >= 8 && profile.experienceYears >= 5) {
            return "experienced and very fit hiker";
        } else if (profile.fitnessLevel >= 7 && profile.experienceYears >= 3) {
            return "seasoned hiker with good fitness";
        } else if (profile.fitnessLevel >= 6 && profile.experienceYears >= 3) {
            return "moderately experienced hiker";
        } else if (profile.fitnessLevel >= 5 && profile.experienceYears >= 1) {
            return "casual hiker with some experience";
        } else if (profile.fitnessLevel >= 4) {
            return "recreational hiker";
        } else if (profile.age >= 65) {
            return "senior adventurer";
        } else if (profile.age >= 60) {
            return "senior hiker";
        } else if (profile.experienceYears < 1) {
            return "hiking newcomer";
        } else {
            return "beginner hiker";
        }
    }

    private static class WeatherAnalysis {
        boolean hasTyphoon, hasHeavyRain, hasExtremeHeat, hasLightning;
        boolean hasPoorVisibility, isFineWeather, hasModerateConcerns;
        String temperature = "";
        String windConditions = "";
        String visibility = "";

        WeatherAnalysis(String weatherText) {
            String weatherLower = weatherText.toLowerCase();

            // Severe conditions
            hasTyphoon = weatherLower.contains("typhoon") || weatherLower.contains("cyclone") ||
                    weatherLower.contains("signal no.") || weatherLower.contains("hurricane");
            hasHeavyRain = weatherLower.contains("rainstorm") || weatherLower.contains("torrential") ||
                    weatherLower.contains("heavy rain") || weatherLower.contains("flooding");
            hasExtremeHeat = weatherLower.contains("extreme heat") || weatherLower.contains("very hot") ||
                    weatherLower.contains("heatwave") || weatherLower.matches(".*3[5-9] degrees.*") ||
                    weatherLower.matches(".*4[0-9] degrees.*");
            hasLightning = weatherLower.contains("thunderstorm") || weatherLower.contains("lightning") ||
                    weatherLower.contains("thunder");
            hasPoorVisibility = weatherLower.contains("fog") || weatherLower.contains("mist") ||
                    weatherLower.contains("visibility") || weatherLower.contains("haze");

            // Good conditions
            isFineWeather = weatherLower.contains("fine") || weatherLower.contains("sunny") ||
                    weatherLower.contains("clear") || weatherLower.contains("excellent");

            // Moderate concerns
            hasModerateConcerns = weatherLower.contains("shower") || weatherLower.contains("cloudy") ||
                    weatherLower.contains("moderate rain") || weatherLower.contains("windy");

            // Extract specific details
            if (weatherLower.matches(".*\\d+ degrees.*")) {
                int start = Math.max(0, weatherText.toLowerCase().indexOf("degrees") - 10);
                int end = Math.min(weatherText.length(), weatherText.toLowerCase().indexOf("degrees") + 7);
                temperature = weatherText.substring(start, end).trim();
            }

            if (weatherLower.contains("wind")) {
                int windIndex = weatherLower.indexOf("wind");
                int start = Math.max(0, windIndex - 20);
                int end = Math.min(weatherText.length(), windIndex + 30);
                windConditions = weatherText.substring(start, end).trim();
            }

            if (weatherLower.contains("visibility")) {
                visibility = "limited visibility conditions";
            } else if (isFineWeather) {
                visibility = "excellent visibility";
            }
        }
    }

    private static class TrailAnalysis {
        String difficultyDesc;
        String lengthDesc;
        String durationDesc;
        String fitnessRequirement;
        boolean isLongTrail;
        boolean isVeryDifficult;
        boolean isBeginnerfriendly;

        TrailAnalysis(HikingRecommendationHelper.TrailProfile trail) {
            // Difficulty description
            if (trail.difficulty <= 1.5) {
                difficultyDesc = "easy, well-maintained";
                fitnessRequirement = "suitable for all fitness levels";
                isBeginnerfriendly = true;
            } else if (trail.difficulty <= 2.5) {
                difficultyDesc = "moderate with some elevation";
                fitnessRequirement = "requires basic fitness";
                isBeginnerfriendly = true;
            } else if (trail.difficulty <= 3.5) {
                difficultyDesc = "moderately challenging";
                fitnessRequirement = "requires good fitness";
                isBeginnerfriendly = false;
            } else if (trail.difficulty <= 4.5) {
                difficultyDesc = "difficult with steep sections";
                fitnessRequirement = "requires excellent fitness";
                isVeryDifficult = true;
            } else {
                difficultyDesc = "very challenging and technical";
                fitnessRequirement = "only for experienced hikers";
                isVeryDifficult = true;
            }

            // Length description
            if (trail.lengthKm <= 5) {
                lengthDesc = "short";
            } else if (trail.lengthKm <= 10) {
                lengthDesc = "moderate length";
            } else if (trail.lengthKm <= 20) {
                lengthDesc = "long";
                isLongTrail = true;
            } else {
                lengthDesc = "very long";
                isLongTrail = true;
            }

            // Duration description
            if (trail.durationHours <= 2) {
                durationDesc = "quick hike";
            } else if (trail.durationHours <= 4) {
                durationDesc = "half-day adventure";
            } else if (trail.durationHours <= 6) {
                durationDesc = "full-day hike";
            } else {
                durationDesc = "extended expedition";
            }
        }
    }

    private static String generateRecommendationText(int primaryClass, float confidence,
                                                     String hikerProfile,
                                                     HikingRecommendationHelper.HikerProfile profile,
                                                     WeatherAnalysis weather,
                                                     TrailAnalysis trailInfo,
                                                     float[] probabilities,
                                                     HikingRecommendationHelper.TrailProfile trail) {
        String recommendation;

        if (primaryClass == 0) { // Not Recommended
            recommendation = generateNotRecommendedText(confidence, hikerProfile, profile, weather, trailInfo, probabilities, trail);
        } else if (primaryClass == 1) { // Caution Advised
            recommendation = generateCautionAdvisedText(confidence, hikerProfile, profile, weather, trailInfo, probabilities, trail);
        } else { // Recommended
            recommendation = generateRecommendedText(confidence, hikerProfile, profile, weather, trailInfo, probabilities, trail);
        }

        return recommendation;
    }

    @SuppressLint("DefaultLocale")
    private static String generateNotRecommendedText(float confidence, String hikerProfile,
                                                     HikingRecommendationHelper.HikerProfile profile,
                                                     WeatherAnalysis weather, TrailAnalysis trailInfo,
                                                     float[] probabilities,
                                                     HikingRecommendationHelper.TrailProfile trail) {
        StringBuilder rec = new StringBuilder();

        // Calculate how close it was to being "Caution Advised"
        float cautionProb = probabilities[1];
        boolean closeCall = cautionProb > 0.3f;

        if (confidence > 0.9f) {
            // Very confident "not recommended"
            if (weather.hasTyphoon) {
                rec.append(String.format("⚠️ EXTREME DANGER: As a %s, you must not attempt this %.1f km %s trail today. " +
                                "Typhoon conditions with destructive winds make any outdoor activity life-threatening. " +
                                "The %s trail (%s) would be impossible to navigate safely even in good weather, " +
                                "let alone in these extreme conditions.",
                        hikerProfile, trail.lengthKm, trailInfo.difficultyDesc, trailInfo.durationDesc, trailInfo.fitnessRequirement));
            } else if (weather.hasHeavyRain && trailInfo.isVeryDifficult) {
                rec.append(String.format("This %s %.1f km trail is already challenging in perfect conditions. " +
                                "Combined with torrential rain, the steep sections become treacherous mud slides. " +
                                "Flash floods are likely in the valleys along this route. As a %s, this combination " +
                                "of difficult terrain and severe weather is simply too dangerous.",
                        trailInfo.difficultyDesc, trail.lengthKm, hikerProfile));
            } else if (weather.hasExtremeHeat && trailInfo.isLongTrail) {
                rec.append(String.format("This %.1f km trail typically takes %.1f hours - far too long " +
                                "to be exposed to extreme heat. With your fitness level of %.0f/10, attempting this %s " +
                                "trail in %s would risk severe heat exhaustion or worse. The lack of shade on major " +
                                "portions of this route makes it particularly dangerous.",
                        trail.lengthKm, trail.durationHours, profile.fitnessLevel, trailInfo.difficultyDesc,
                        weather.temperature));
            } else if (trailInfo.isVeryDifficult && profile.fitnessLevel < 5) {
                rec.append(String.format("This %s trail (difficulty %.1f/5) significantly exceeds your " +
                                "current capabilities. The %.1f km route %s, which is beyond what's safe for " +
                                "someone with your fitness level of %.0f/10. Combined with %s, this creates " +
                                "an unacceptable risk.",
                        trailInfo.difficultyDesc, trail.difficulty, trail.lengthKm, trailInfo.fitnessRequirement,
                        profile.fitnessLevel, getWeatherSummary(weather)));
            } else {
                rec.append(String.format("Current conditions make this %.1f km %s trail unsafe. " +
                                "As a %s, the combination of %s and the trail's characteristics " +
                                "(%s) present too many risks. Safety must come first.",
                        trail.lengthKm, trailInfo.difficultyDesc, hikerProfile,
                        getWeatherSummary(weather), trailInfo.durationDesc));
            }
        } else if (confidence > 0.75f) {
            // Fairly confident
            rec.append(String.format("Strong recommendation against hiking this %.1f km trail today. " +
                            "Multiple factors including %s and the %s nature of this route suggest " +
                            "significant risks for a %s.",
                    trail.lengthKm, getWeatherSummary(weather), trailInfo.difficultyDesc, hikerProfile));
        } else {
            // Less confident
            rec.append(String.format("Based on current analysis (%.0f%% certainty), this %.1f km %s trail " +
                            "is not recommended. Weather conditions (%s) combined with trail difficulty may pose risks.",
                    confidence * 100, trail.lengthKm, trailInfo.difficultyDesc, getWeatherSummary(weather)));
        }

        // Add note if it was close to "Caution Advised"
        if (closeCall && !weather.hasTyphoon && !weather.hasHeavyRain) {
            rec.append(String.format("\n\n💡 Note: Conditions are borderline (%.0f%% chance it might be " +
                            "manageable with extreme caution). Check for weather updates - conditions may improve.",
                    cautionProb * 100));
        }

        return rec.toString();
    }

    @SuppressLint("DefaultLocale")
    private static String generateCautionAdvisedText(float confidence, String hikerProfile,
                                                     HikingRecommendationHelper.HikerProfile profile,
                                                     WeatherAnalysis weather, TrailAnalysis trailInfo,
                                                     float[] probabilities,
                                                     HikingRecommendationHelper.TrailProfile trail) {
        StringBuilder rec = new StringBuilder();

        // Check how it leans (toward not recommended or recommended)
        boolean leansNegative = probabilities[0] > probabilities[2];

        if (weather.hasLightning) {
            rec.append(String.format("⚡ LIGHTNING RISK: This %.1f km %s trail can be attempted by a %s, " +
                            "but thunderstorms demand extreme vigilance. Key safety measures:\n" +
                            "• Start before dawn to avoid afternoon storms\n" +
                            "• Monitor weather constantly - turn back at first thunder\n" +
                            "• Avoid the exposed ridges between km %d-%d\n" +
                            "• The %s typically takes %.1f hours - you must be prepared to abort",
                    trail.lengthKm, trailInfo.difficultyDesc, hikerProfile,
                    (int)(trail.lengthKm * 0.3), (int)(trail.lengthKm * 0.7),
                    trailInfo.durationDesc, trail.durationHours));
        } else if (weather.hasPoorVisibility) {
            rec.append(String.format("🌫️ LIMITED VISIBILITY: The %.1f km trail is shrouded in %s. " +
                            "With your %.0f years experience, you can attempt this %s route but must:\n" +
                            "• Use GPS navigation (phone + backup)\n" +
                            "• Stay on marked paths - this trail has confusing junctions at km %.1f and %.1f\n" +
                            "• Allow extra time - expect %.1f hours instead of the normal %.1f\n" +
                            "• Consider the shorter alternative loop if visibility worsens",
                    trail.lengthKm, weather.visibility, profile.experienceYears, trailInfo.difficultyDesc,
                    trail.lengthKm * 0.4, trail.lengthKm * 0.8,
                    trail.durationHours * 1.5, trail.durationHours));
        } else if (trailInfo.isLongTrail && profile.longestHikeKm < trail.lengthKm) {
            rec.append(String.format("📏 DISTANCE CHALLENGE: At %.1f km, this trail is %.0f%% longer than " +
                            "your previous longest hike (%.1f km). As a %s, you can attempt it but:\n" +
                            "• Pack 50%% more water than usual (%.1f liters minimum)\n" +
                            "• Plan rest stops every %.1f km\n" +
                            "• The %s terrain means you'll be working harder than on easier trails\n" +
                            "• Have an exit strategy - know the bailout points at km %.1f and %.1f",
                    trail.lengthKm, ((trail.lengthKm / profile.longestHikeKm) - 1) * 100,
                    profile.longestHikeKm, hikerProfile,
                    trail.durationHours * 0.5 + 1, trail.lengthKm / 5,
                    trailInfo.difficultyDesc, trail.lengthKm * 0.3, trail.lengthKm * 0.6));
        } else {
            rec.append(String.format("This %.1f km %s trail is manageable with proper precautions. " +
                            "Current %s requires extra care. As a %s:\n" +
                            "• Start early to complete the %.1f hour hike before conditions worsen\n" +
                            "• The trail %s - pace yourself accordingly\n" +
                            "• Monitor conditions and be ready to turn back\n" +
                            "• Inform someone of your route and expected return",
                    trail.lengthKm, trailInfo.difficultyDesc, getWeatherSummary(weather), hikerProfile,
                    trail.durationHours, trailInfo.fitnessRequirement));
        }

        if (leansNegative) {
            rec.append(String.format("\n\n⚠️ Conditions lean toward 'not recommended' (%.0f%% risk). " +
                            "Only proceed if you're feeling strong and well-prepared.",
                    probabilities[0] * 100));
        }

        return rec.toString();
    }

    @SuppressLint("DefaultLocale")
    private static String generateRecommendedText(float confidence, String hikerProfile,
                                                  HikingRecommendationHelper.HikerProfile profile,
                                                  WeatherAnalysis weather, TrailAnalysis trailInfo,
                                                  float[] probabilities,
                                                  HikingRecommendationHelper.TrailProfile trail) {
        StringBuilder rec = new StringBuilder();

        if (confidence > 0.9f && weather.isFineWeather) {
            if (trailInfo.isBeginnerfriendly && profile.experienceYears < 2) {
                rec.append(String.format("🌟 PERFECT BEGINNER CONDITIONS: This %.1f km %s trail is ideal " +
                                "for building your hiking experience! With %s and the trail being %s, " +
                                "you couldn't ask for better conditions. The %.1f hour journey will showcase:\n" +
                                "• Gentle elevation gains perfect for your fitness level (%.0f/10)\n" +
                                "• Well-marked paths with clear signage\n" +
                                "• Beautiful viewpoints at km %.1f and %.1f\n" +
                                "• Shaded rest areas every few kilometers",
                        trail.lengthKm, trailInfo.difficultyDesc, weather.visibility, trailInfo.fitnessRequirement,
                        trail.durationHours, profile.fitnessLevel,
                        trail.lengthKm * 0.3, trail.lengthKm * 0.7));
            } else if (trailInfo.isVeryDifficult && profile.fitnessLevel >= 8) {
                rec.append(String.format("💪 PERFECT CHALLENGE CONDITIONS: As a %s, this %.1f km %s trail " +
                                "in today's %s weather is an excellent match for your abilities! The route:\n" +
                                "• Tests your skills with technical sections perfect for your experience level\n" +
                                "• Rewards with spectacular views after the steep climbs\n" +
                                "• The %.1f hour duration fits well within your endurance (longest: %.1f km)\n" +
                                "• Weather window is ideal - make the most of these conditions!",
                        hikerProfile, trail.lengthKm, trailInfo.difficultyDesc, weather.temperature,
                        trail.durationHours, profile.longestHikeKm));
            } else {
                rec.append(String.format("✅ EXCELLENT HIKING CONDITIONS: This %.1f km %s trail is perfectly " +
                                "suited for a %s today! Expect:\n" +
                                "• %s throughout your %.1f hour journey\n" +
                                "• Trail conditions are optimal with %s\n" +
                                "• Your fitness level (%.0f/10) is well-matched to this route\n" +
                                "• Popular rest spots at the waterfall (km %.1f) and summit viewpoint",
                        trail.lengthKm, trailInfo.difficultyDesc, hikerProfile,
                        weather.visibility, trail.durationHours, getWeatherSummary(weather),
                        profile.fitnessLevel, trail.lengthKm * 0.6));
            }
        } else if (confidence > 0.75f) {
            rec.append(String.format("Good conditions for hiking this %.1f km trail! As a %s, " +
                            "you're well-prepared for the %s route. Current weather (%s) is favorable " +
                            "for the %.1f hour hike.",
                    trail.lengthKm, hikerProfile, trailInfo.difficultyDesc,
                    getWeatherSummary(weather), trail.durationHours));
        } else {
            rec.append(String.format("Conditions appear suitable (%.0f%% confidence) for this %.1f km %s trail. " +
                            "Weather is generally favorable, though stay alert for changes during your %.1f hour hike.",
                    confidence * 100, trail.lengthKm, trailInfo.difficultyDesc, trail.durationHours));
        }

        // Add encouragement if close to perfect conditions
        if (probabilities[2] > 0.8f && weather.isFineWeather) {
            rec.append("\n\n🎯 Today is one of those rare perfect hiking days - enjoy every moment!");
        }

        return rec.toString();
    }

    private static String generateConfidenceAdvice(float confidence, float[] probabilities) {
        StringBuilder advice = new StringBuilder();

        // More nuanced confidence interpretation
        if (confidence >= 0.9f) {
            advice.append("\n\n📊 Very high confidence (").append(String.format("%.0f%%", confidence * 100))
                    .append(") in this assessment.");
        } else if (confidence >= 0.75f) {
            advice.append("\n\n📊 High confidence (").append(String.format("%.0f%%", confidence * 100))
                    .append(") - conditions are fairly clear.");
        } else if (confidence >= 0.6f) {
            advice.append("\n\n📊 Moderate confidence (").append(String.format("%.0f%%", confidence * 100))
                    .append(") - some factors are borderline.");
        } else if (confidence >= 0.45f) {
            advice.append("\n\n📊 Mixed signals (").append(String.format("%.0f%%", confidence * 100))
                    .append(") - conditions are genuinely borderline. ")
                    .append("Alternative assessment: ")
                    .append(String.format("%.0f%% not recommended, %.0f%% caution, %.0f%% recommended.",
                            probabilities[0] * 100, probabilities[1] * 100, probabilities[2] * 100));
        } else {
            advice.append("\n\n📊 Low confidence (").append(String.format("%.0f%%", confidence * 100))
                    .append(") - multiple conflicting factors. Consider postponing for clearer conditions.");
        }

        return advice.toString();
    }

    @SuppressLint("DefaultLocale")
    private static String generateTrailTips(TrailAnalysis trailInfo,
                                            HikingRecommendationHelper.HikerProfile profile,
                                            WeatherAnalysis weather,
                                            HikingRecommendationHelper.TrailProfile trail) {
        StringBuilder tips = new StringBuilder("\n\n💡 Trail-Specific Tips:\n");

        // Water recommendations based on trail and weather
        float waterLiters = calculateWaterNeeds(trailInfo, weather, profile, trail);
        tips.append(String.format("• Water: Bring %.1fL (", waterLiters));
        if (weather.hasExtremeHeat) tips.append("extra for heat");
        else if (trailInfo.isLongTrail) tips.append("extra for distance");
        else tips.append("standard amount");
        tips.append(")\n");

        // Timing recommendations
        if (weather.hasLightning || weather.hasExtremeHeat) {
            tips.append("• Start time: Before 6 AM to avoid afternoon weather\n");
        } else if (trail.durationHours > 5) {
            tips.append("• Start time: Before 8 AM to ensure daylight return\n");
        }

        // Gear recommendations
        if (trailInfo.isVeryDifficult) {
            tips.append("• Gear: Trekking poles highly recommended for steep sections\n");
        }
        if (weather.hasPoorVisibility) {
            tips.append("• Navigation: Download offline maps, bring compass\n");
        }

        // Pace recommendations
        float paceMultiplier = (10.0f - profile.fitnessLevel) / 10.0f * 0.3f + 1.0f;
        float expectedTime = trail.durationHours * paceMultiplier;
        if (Math.abs(expectedTime - trail.durationHours) > 0.5f) {
            tips.append(String.format("• Pace: Allow %.1f hours (%.0f%% slower than average)\n",
                    expectedTime, (paceMultiplier - 1) * 100));
        }

        return tips.toString();
    }

    private static float calculateWaterNeeds(TrailAnalysis trailInfo, WeatherAnalysis weather,
                                             HikingRecommendationHelper.HikerProfile profile,
                                             HikingRecommendationHelper.TrailProfile trail) {
        float baseWater = trail.durationHours * 0.5f; // 0.5L per hour base

        if (weather.hasExtremeHeat) baseWater *= 1.5f;
        else if (weather.temperature.contains("3") && weather.temperature.contains("degrees")) baseWater *= 1.3f;

        if (trailInfo.isVeryDifficult) baseWater *= 1.2f;
        if (profile.fitnessLevel < 5) baseWater *= 1.1f;

        return Math.max(1.0f, Math.min(4.0f, baseWater)); // Between 1-4 liters
    }

    private static String getWeatherSummary(WeatherAnalysis weather) {
        if (weather.hasTyphoon) return "typhoon conditions";
        if (weather.hasHeavyRain) return "heavy rain";
        if (weather.hasExtremeHeat) return "extreme heat";
        if (weather.hasLightning) return "thunderstorms";
        if (weather.hasPoorVisibility) return "poor visibility";
        if (weather.isFineWeather) return "excellent weather";
        if (weather.hasModerateConcerns) return "variable conditions";
        return "current conditions";
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