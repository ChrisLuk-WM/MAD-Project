package com.example.mad_project.ui.pages.sessions;

import com.example.mad_project.database.entities.HikingSessionEntity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SessionFilter {
    public enum DateRange {
        ALL("All Time"),
        LAST_WEEK("Last Week"),
        LAST_MONTH("Last Month"),
        LAST_3_MONTHS("Last 3 Months");

        private final String displayName;

        DateRange(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum DurationRange {
        ALL("All Durations"),
        SHORT("1-15 minutes"),
        MEDIUM("15-30 minutes"),
        LONG("30+ minutes");

        private final String displayName;

        DurationRange(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private DateRange dateRange = DateRange.ALL;
    private DurationRange durationRange = DurationRange.ALL;

    public boolean matches(HikingSessionEntity session) {
        if (session.getStartTime() == null || session.getEndTime() == null) {
            return false;
        }

        // Filter out sessions less than 1 minute
        long durationMinutes = ChronoUnit.MINUTES.between(session.getStartTime(), session.getEndTime());
        if (durationMinutes < 1) {
            return false;
        }

        // Apply date filter
        LocalDateTime now = LocalDateTime.now();
        boolean matchesDate = switch (dateRange) {
            case ALL -> true;
            case LAST_WEEK -> session.getStartTime().isAfter(now.minusWeeks(1));
            case LAST_MONTH -> session.getStartTime().isAfter(now.minusMonths(1));
            case LAST_3_MONTHS -> session.getStartTime().isAfter(now.minusMonths(3));
        };

        // Apply duration filter
        boolean matchesDuration = switch (durationRange) {
            case ALL -> true;
            case SHORT -> durationMinutes >= 1 && durationMinutes <= 15;
            case MEDIUM -> durationMinutes > 15 && durationMinutes <= 30;
            case LONG -> durationMinutes > 30;
        };

        return matchesDate && matchesDuration;
    }

    // Getters and setters
    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public DurationRange getDurationRange() {
        return durationRange;
    }

    public void setDurationRange(DurationRange durationRange) {
        this.durationRange = durationRange;
    }
}