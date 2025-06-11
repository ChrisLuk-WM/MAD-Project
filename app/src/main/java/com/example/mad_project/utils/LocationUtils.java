package com.example.mad_project.utils;

public class LocationUtils {
    public static class Place {
        public final String name;
        public final double latitude;
        public final double longitude;

        public Place(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    // District centroids
    public static final Place[] DISTRICTS_LIST = new Place[] {
            new Place("Central & Western District", 22.2819, 114.1546),
            new Place("Eastern District", 22.2831, 114.2260),
            new Place("Kwai Tsing", 22.3570, 114.1270),
            new Place("Islands District", 22.2614, 113.9460),
            new Place("North District", 22.4966, 114.1420),
            new Place("Sai Kung", 22.3835, 114.2736),
            new Place("Sha Tin", 22.3930, 114.2061),
            new Place("Southern District", 22.2379, 114.1616),
            new Place("Tai Po", 22.4500, 114.1700),
            new Place("Tsuen Wan", 22.3700, 114.1200),
            new Place("Tuen Mun", 22.3910, 113.9730),
            new Place("Wan Chai", 22.2783, 114.1756),
            new Place("Yuen Long", 22.4443, 114.0228),
            new Place("Yau Tsim Mong", 22.3167, 114.1700),
            new Place("Sham Shui Po", 22.3292, 114.1595),
            new Place("Kowloon City", 22.3282, 114.1913),
            new Place("Wong Tai Sin", 22.3442, 114.1956),
            new Place("Kwun Tong", 22.3136, 114.2254)
    };

    // Weather station locations
    public static final Place[] WEATHER_STATIONS_LIST = new Place[] {
            new Place("King's Park", 22.3070, 114.1730),
            new Place("Hong Kong Observatory", 22.3020, 114.1740),
            new Place("Wong Chuk Hang", 22.2476, 114.1736),
            new Place("Ta Kwu Ling", 22.5250, 114.1700),
            new Place("Lau Fau Shan", 22.4810, 113.9980),
            new Place("Tai Po", 22.4500, 114.1700),
            new Place("Sha Tin", 22.3930, 114.2061),
            new Place("Tuen Mun", 22.3910, 113.9730),
            new Place("Tseung Kwan O", 22.3086, 114.2614),
            new Place("Sai Kung", 22.3835, 114.2736),
            new Place("Cheung Chau", 22.2020, 114.0280),
            new Place("Chek Lap Kok", 22.3080, 113.9185),
            new Place("Tsing Yi", 22.3580, 114.1040),
            new Place("Shek Kong", 22.4330, 114.0820),
            new Place("Tsuen Wan Ho Koon", 22.3900, 114.1040),
            new Place("Tsuen Wan Shing Mun Valley", 22.3800, 114.1200),
            new Place("Hong Kong Park", 22.2770, 114.1640),
            new Place("Shau Kei Wan", 22.2820, 114.2290),
            new Place("Kowloon City", 22.3282, 114.1913),
            new Place("Happy Valley", 22.2700, 114.1830),
            new Place("Wong Tai Sin", 22.3442, 114.1956),
            new Place("Stanley", 22.2180, 114.2130),
            new Place("Kwun Tong", 22.3136, 114.2254),
            new Place("Sham Shui Po", 22.3292, 114.1595),
            new Place("Yuen Long Park", 22.4443, 114.0228),
            new Place("Tai Mei Tuk", 22.4942, 114.2422)
    };

    // Haversine formula to calculate distance (in meters)
    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters
        double latRad1 = Math.toRadians(lat1);
        double latRad2 = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(latRad1) * Math.cos(latRad2) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Returns the nearest district name to the given latitude and longitude.
     */
    public static String getNearestDistrict(double lat, double lon) {
        Place nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (Place district : DISTRICTS_LIST) {
            double dist = haversine(lat, lon, district.latitude, district.longitude);
            if (dist < minDistance) {
                minDistance = dist;
                nearest = district;
            }
        }
        return nearest != null ? nearest.name : null;
    }

    /**
     * Returns the nearest weather station name to the given latitude and longitude.
     */
    public static String getNearestWeatherStation(double lat, double lon) {
        Place nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (Place station : WEATHER_STATIONS_LIST) {
            double dist = haversine(lat, lon, station.latitude, station.longitude);
            if (dist < minDistance) {
                minDistance = dist;
                nearest = station;
            }
        }
        return nearest != null ? nearest.name : null;
    }
}