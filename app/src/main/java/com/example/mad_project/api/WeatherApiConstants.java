package com.example.mad_project.api;

public class WeatherApiConstants {
    // Weather API Data Types
    public static class DataType {
        // Weather Information API
        public static final String LOCAL_FORECAST = "flw";        // Local Weather Forecast
        public static final String NINE_DAY_FORECAST = "fnd";     // 9-day Weather Forecast
        public static final String CURRENT_WEATHER = "rhrread";   // Current Weather Report
        public static final String WARNING_SUMMARY = "warnsum";   // Weather Warning Summary
        public static final String WARNING_INFO = "warningInfo";  // Weather Warning Information
        public static final String SPECIAL_WEATHER_TIPS = "swt";  // Special Weather Tips

        // Earthquake Information API
        public static final String QUICK_EARTHQUAKE = "qem";          // Quick Earthquake Messages
        public static final String FELT_EARTHQUAKE = "feltearthquake";// Locally Felt Earth Tremor Report

        // Weather & Radiation Level Report
        public static final String WEATHER_RADIATION = "RYES";    // Weather and Radiation Level Report
    }

    // Warning Statement Codes
    public static class WarningCode {
        public static final String FIRE = "WFIRE";           // Fire Danger Warning
        public static final String FROST = "WFROST";         // Frost Warning
        public static final String HOT = "WHOT";            // Hot Weather Warning
        public static final String COLD = "WCOLD";          // Cold Weather Warning
        public static final String MONSOON = "WMSGNL";      // Strong Monsoon Signal
        public static final String PRE_NO8 = "WTCPRE8";     // Pre-no.8 Special Announcement
        public static final String RAIN = "WRAIN";          // Rainstorm Warning Signal
        public static final String FLOODING = "WFNTSA";     // Special Announcement on Flooding in the northern New Territories
        public static final String LANDSLIP = "WL";         // Landslip Warning
        public static final String TYPHOON = "WTCSGNL";     // Tropical Cyclone Warning Signal
        public static final String TSUNAMI = "WTMW";        // Tsunami Warning
        public static final String THUNDERSTORM = "WTS";    // Thunderstorm Warning
    }

    // Warning Subtypes
    public static class WarningSubtype {
        // Fire Danger Warning
        public static final String FIRE_YELLOW = "WFIREY";  // Yellow Fire
        public static final String FIRE_RED = "WFIRER";     // Red Fire

        // Rainstorm Warning
        public static final String RAIN_AMBER = "WRAINA";   // Amber
        public static final String RAIN_RED = "WRAINR";     // Red
        public static final String RAIN_BLACK = "WRAINB";   // Black

        // Tropical Cyclone Warning
        public static final String TC_1 = "TC1";            // No. 1
        public static final String TC_3 = "TC3";            // No. 3
        public static final String TC_8NE = "TC8NE";        // No. 8 North East
        public static final String TC_8SE = "TC8SE";        // No. 8 South East
        public static final String TC_8SW = "TC8SW";        // No. 8 South West
        public static final String TC_8NW = "TC8NW";        // No. 8 North West
        public static final String TC_9 = "TC9";            // No. 9
        public static final String TC_10 = "TC10";          // No. 10
        public static final String TC_CANCEL = "CANCEL";    // Cancel All Signals
    }

    // Warning Action Codes
    public static class ActionCode {
        public static final String ISSUE = "ISSUE";
        public static final String REISSUE = "REISSUE";     // For WCOLD, WHOT and WFNTSA
        public static final String CANCEL = "CANCEL";
        public static final String EXTEND = "EXTEND";       // For WTS
        public static final String UPDATE = "UPDATE";       // For WTS
    }

    // Language Codes
    public static class Language {
        public static final String ENGLISH = "en";
        public static final String TRADITIONAL_CHINESE = "tc";
        public static final String SIMPLIFIED_CHINESE = "sc";
    }

    // Return Format
    public static class Format {
        public static final String JSON = "json";
        public static final String CSV = "csv";
    }

    // Weather Report Station Codes
    public static class Station {
        // Regional Weather Stations
        public static final String CHEUNG_CHAU = "CCH";
        public static final String CHEK_LAP_KOK = "CLK";
        public static final String CHI_MA_WAN = "CMW";
        public static final String KWAI_CHUNG = "KCT";
        public static final String KO_LAU_WAN = "KLW";
        public static final String LOK_ON_PAI = "LOP";
        public static final String MA_WAN = "MWC";
        public static final String QUARRY_BAY = "QUB";
        public static final String SHEK_PIK = "SPW";
        public static final String TAI_O = "TAO";
        public static final String TSIM_BEI_TSUI = "TBT";
        public static final String TAI_MIU_WAN = "TMW";
        public static final String TAI_PO_KAU = "TPK";
        public static final String WAGLAN_ISLAND = "WAG";

        // Radiation Monitoring Stations
        public static final String CLEAR_WATER_BAY = "CWB";
        public static final String HK_AIRPORT = "HKA";
        public static final String HK_OBSERVATORY = "HKO";
        public static final String HK_PARK = "HKP";
        public static final String WONG_CHUK_HANG = "HKS";
        public static final String HAPPY_VALLEY = "HPV";
        public static final String TSEUNG_KWAN_O = "JKB";
        public static final String KOWLOON_CITY = "KLT";
        public static final String KINGS_PARK = "KP";
        public static final String KAU_SAI_CHAU = "KSC";
        public static final String KWUN_TONG = "KTG";
        public static final String LAU_FAU_SHAN = "LFS";
        public static final String NGONG_PING = "NGP";
        public static final String PENG_CHAU = "PEN";
        public static final String TAI_MEI_TUK = "PLC";
        public static final String KAI_TAK = "SE1";
        public static final String SHEK_KONG = "SEK";
        public static final String SHA_TIN = "SHA";
        public static final String SAI_KUNG = "SKG";
        public static final String SHAU_KEI_WAN = "SKW";
        public static final String SHEUNG_SHUI = "SSH";
        public static final String SHAM_SHUI_PO = "SSP";
        public static final String STANLEY = "STY";
        public static final String TATES_CAIRN = "TC";
        public static final String TA_KWU_LING = "TKL";
        public static final String TAI_MO_SHAN = "TMS";
        public static final String TAI_PO = "TPO";
        public static final String TUEN_MUN = "TU1";
        public static final String TSUEN_WAN = "TW";
        public static final String TSUEN_WAN_HO_KOON = "TWN";
        public static final String TSING_YI = "TY1";
        public static final String PAK_TAM_CHUNG = "TYW";
        public static final String THE_PEAK = "VP1";
        public static final String WETLAND_PARK = "WLP";
        public static final String WONG_TAI_SIN = "WTS";
        public static final String YUAN_CHAU_TSAI = "YCT";
        public static final String YUEN_LONG = "YLP";
    }
}