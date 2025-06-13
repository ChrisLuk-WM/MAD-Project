package com.example.mad_project.utils;

import androidx.annotation.NonNull;

import com.example.mad_project.api.models.CurrentWeather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherWarningUtils {

    public static class WarningInfo {
        private final String type;
        private final String level;
        private final String reminder;
        private final int severity; // 1-5, 5 being most severe

        public WarningInfo(String type, String level, String reminder, int severity) {
            this.type = type;
            this.level = level;
            this.reminder = reminder;
            this.severity = severity;
        }

        public String getType() { return type; }
        public String getLevel() { return level; }
        public String getReminder() { return reminder; }
        public int getSeverity() { return severity; }
    }

    public static class WarningConfig {
        public final String reminder;
        public final int severity;
        public final String imagePath;

        WarningConfig(String reminder, int severity, String imagePath) {
            this.reminder = reminder;
            this.severity = severity;
            this.imagePath = "warning_icon/" + imagePath + ".gif";
        }
    }

    private static final Map<String, WarningConfig> TYPHOON_SIGNALS = new HashMap<String, WarningConfig>() {{
        put("NO. 1", new WarningConfig(
                "Be prepared. Secure loose objects.",
                2,
                "tc1"
        ));
        put("NO. 3", new WarningConfig(
                "Strong winds expected. Stay away from shoreline.",
                3,
                "tc3"
        ));
        put("NO. 8", new WarningConfig(
                "Gale or storm force winds. Stay indoors.",
                5,
                "tc8"
        ));
        put("NO. 9", new WarningConfig(
                "Severe gale or storm. All outdoor activities dangerous.",
                5,
                "tc9"
        ));
        put("NO. 10", new WarningConfig(
                "Hurricane force winds. Stay inside and away from windows.",
                5,
                "tc10"
        ));
    }};

    private static final Map<String, WarningConfig> RAINSTORM_SIGNALS = new HashMap<String, WarningConfig>() {{
        put("AMBER", new WarningConfig(
                "Heavy rain. Be prepared for possible road flooding.",
                4,
                "wraina"
        ));
        put("RED", new WarningConfig(
                "Very heavy rain. Flash floods possible. Avoid water sports.",
                5,
                "wrainr"
        ));
        put("BLACK", new WarningConfig(
                "Extremely heavy rain. Serious flooding. Stay indoor.",
                5,
                "wrainb"
        ));
    }};

    private static final Map<String, WarningConfig> SPECIAL_WARNINGS = new HashMap<String, WarningConfig>() {{
        put("VERY HOT", new WarningConfig(
                "Stay hydrated, avoid direct sunlight, rest in shade frequently.",
                2,
                "vhot"
        ));
        put("COLD", new WarningConfig(
                "Wear warm clothes, be aware of hypothermia risk.",
                1,
                "cold"
        ));
        put("FROST", new WarningConfig(
                "Protect plants, be cautious of slippery surfaces.",
                1,
                "frost"
        ));
        put("STRONG MONSOON", new WarningConfig(
                "Strong gusty winds. Small boats should stay in port.",
                1,
                "sms"
        ));
        put("FIRE DANGER", new WarningConfig(
                "High fire risk. No open fires in country parks.",
                2,
                "fire"
        ));
    }};

    private static final Map<String, WarningConfig> OTHER_WARNINGS = new HashMap<String, WarningConfig>() {{
        put("THUNDERSTORM", new WarningConfig(
                "Lightning risk. Stay indoors, avoid open areas.",
                1,
                "ts"
        ));
        put("LANDSLIP", new WarningConfig(
                "Avoid hillsides and retaining walls.",
                3,
                "landslip"
        ));
        put("FLOODING IN THE NORTHERN NEW TERRITORIES", new WarningConfig(
                "Flooding possible in low-lying areas.",
                3,
                "flood"
        ));
    }};

    private static Map<String, WarningConfig> comblineWarnings(Map<String, WarningConfig>... arrays) {
        Map<String, WarningConfig> result = new HashMap<>();
        for (Map<String, WarningConfig> array : arrays) {
            result.putAll(array);
        }
        return result;
    }

    public static Map<String, WarningConfig> getWarningList() {
        return comblineWarnings(
            TYPHOON_SIGNALS,
            RAINSTORM_SIGNALS,
            SPECIAL_WARNINGS,
            OTHER_WARNINGS
        );
    };

    @NonNull
    public static List<WarningInfo> parseWarnings(List<String> warningMessages) {
        List<WarningInfo> warnings = new ArrayList<>();

        if (warningMessages == null) return warnings;

        for (String message : warningMessages) {
            String upperMessage = message.toUpperCase();

            // Check Tropical Cyclone Signals
            if (upperMessage.contains("TROPICAL CYCLONE") || upperMessage.contains("SIGNAL")) {
                for (Map.Entry<String, WarningConfig> signal : TYPHOON_SIGNALS.entrySet()) {
                    if (upperMessage.contains(signal.getKey())) {
                        warnings.add(new WarningInfo(
                                "Tropical Cyclone",
                                signal.getKey(),
                                signal.getValue().reminder,
                                signal.getValue().severity
                        ));
                        break;
                    }
                }
            }

            // Check Rainstorm Warnings
            if (upperMessage.contains("RAINSTORM")) {
                for (Map.Entry<String, WarningConfig> signal : RAINSTORM_SIGNALS.entrySet()) {
                    if (upperMessage.contains(signal.getKey())) {
                        warnings.add(new WarningInfo(
                                "Rainstorm",
                                signal.getKey(),
                                signal.getValue().reminder,
                                signal.getValue().severity
                        ));
                        break;
                    }
                }
            }

            // Check Special Warnings
            for (Map.Entry<String, WarningConfig> special : SPECIAL_WARNINGS.entrySet()) {
                if (upperMessage.contains(special.getKey())) {
                    warnings.add(new WarningInfo(
                            "Special Warning",
                            special.getKey(),
                            special.getValue().reminder,
                            special.getValue().severity
                    ));
                    break;
                }
            }

            // Check Other Warnings
            for (Map.Entry<String, WarningConfig> other : OTHER_WARNINGS.entrySet()) {
                if (upperMessage.contains(other.getKey())) {
                    warnings.add(new WarningInfo(
                            "Weather Warning",
                            other.getKey(),
                            other.getValue().reminder,
                            other.getValue().severity
                    ));
                    break;
                }
            }
        }

        return warnings;
    }

    public static String parseWarningMessages(CurrentWeather weather){
        if (weather == null) return "";

        StringBuilder warningMessages = new StringBuilder();
        if (weather.getWarningMessage() != null) {
            for (String msg : weather.getWarningMessage()) {
                warningMessages.append(msg);
            }
        }

        if (weather.getTcmessage() != null) {
            for (String msg : weather.getTcmessage()) {
                warningMessages.append(msg);
            }
        }

        return warningMessages.toString();
    }

    public static String getHikingAdvice(List<WarningInfo> warnings) {
        if (warnings.isEmpty()) {
            return "Current conditions are suitable for hiking.";
        }

        // Find highest severity warning
        WarningInfo mostSevere = warnings.stream()
                .max((a, b) -> Integer.compare(a.getSeverity(), b.getSeverity()))
                .orElse(null);

        if (mostSevere == null) return "Exercise normal caution while hiking.";

        switch (mostSevere.getSeverity()) {
            case 5:
                return "DANGEROUS CONDITIONS. DO NOT HIKE. Stay indoors.";
            case 4:
                return "Very dangerous conditions. Hiking strongly discouraged.";
            case 3:
                return "Hazardous conditions. Consider postponing hiking activities.";
            case 2:
                return "Exercise extra caution if hiking. Be prepared to change plans.";
            default:
                return "Be aware of weather conditions while hiking.";
        }
    }

    public static boolean isHikingRecommended(List<WarningInfo> warnings) {
        if (warnings.isEmpty()) return true;

        // Check if any severe warnings exist
        return warnings.stream()
                .noneMatch(warning -> warning.getSeverity() >= 3);
    }
}