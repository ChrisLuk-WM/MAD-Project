package com.example.mad_project.statistics;

import static com.example.mad_project.utils.Common.convertToDouble;
import static com.example.mad_project.utils.Common.convertToFloat;
import static com.example.mad_project.utils.Common.convertToInt;

import android.content.Context;
import android.location.Location;

import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.HikingSessionDao;
import com.example.mad_project.database.dao.HikingStatisticsDao;
import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.database.entities.HikingStatisticsEntity;

import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatisticsCalculator extends Thread {
    private static volatile StatisticsCalculator instance;
    private final StatisticsManager statisticsManager;
    private final AtomicBoolean isRunning;
    private final HikingSessionDao sessionDao;
    private final HikingStatisticsDao statisticsDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private WeakReference<Location> previousLocation; // Use WeakReference
    private WeakReference<HikingSessionEntity> currentSession; // Use WeakReference
    private static final int STATISTICS_INTERVAL = 5000; // 5 seconds
    private final float[] distanceResults = new float[1]; // Reuse array

    private StatisticsCalculator(Context context) {
        super("StatisticsCalculator-Thread");
        AppDatabase db = AppDatabase.getDatabase(context);
        this.sessionDao = db.hikingSessionDao();
        this.statisticsDao = db.hikingStatisticsDao();
        this.isRunning = new AtomicBoolean(false);
        this.statisticsManager = StatisticsManager.getInstance();
    }

    public static StatisticsCalculator getInstance(Context context) {
        if (instance == null) {
            synchronized (StatisticsCalculator.class) {
                if (instance == null) {
                    instance = new StatisticsCalculator(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void startSession() {
        executor.execute(() -> {
            try {
                HikingSessionEntity session = findActiveSession();
                if (session == null) {
                    session = createNewSession();
                }
                currentSession = new WeakReference<>(session);
                isRunning.set(true);
                if (!isAlive()) {
                    start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private HikingSessionEntity findActiveSession() {
        // Find session with no end time
        return sessionDao.getActiveSessionSync();
    }

    private HikingSessionEntity createNewSession() {
        HikingSessionEntity session = new HikingSessionEntity();
        session.setStartTime(LocalDateTime.now());
        session.setTrailId(1); // 1 is the free hiking
        session.setDistance(0);
        session.setSteps(0);
        session.setAverageSpeed(0);
        session.setTotalElevationGain(0);

        try {
            long sessionId = sessionDao.insert(session);
            session.setId(sessionId);
            return session;
        } catch (Exception e) {
            e.printStackTrace();
            // Handle failure
            return null;
        }
    }

    public void stopSession() {
        executor.execute(() -> {
            try {
                HikingSessionEntity session = currentSession != null ? currentSession.get() : null;
                if (session != null) {
                    session.setEndTime(LocalDateTime.now());
                    sessionDao.update(session);
                }
                isRunning.set(false);
                saveStatistics();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void run() {
        while (isRunning.get()) {
            try {
                saveStatistics();
                Thread.sleep(STATISTICS_INTERVAL); // 5 seconds interval
            } catch (InterruptedException e) {
                if (!isRunning.get()) break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveStatistics() {
        HikingSessionEntity session = currentSession != null ? currentSession.get() : null;
        if (session == null){
            startSession();
            if (session == null) return;
        }

        Location location = statisticsManager.getValue(StatisticsType.LOCATION);
        if (location != null) {
            HikingStatisticsEntity statistics = createStatistics(session, location);
            try {
                statisticsDao.insert(statistics);
                updateAccumulatedValues(location);
                updateSession(session);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private HikingStatisticsEntity createStatistics(HikingSessionEntity session, Location location) {
        HikingStatisticsEntity statistics = new HikingStatisticsEntity();
        statistics.setSessionId(session.getId());
        statistics.setDateTime(LocalDateTime.now());
        statistics.setLatitude(location.getLatitude());
        statistics.setLongitude(location.getLongitude());

        try {
            // Handle each type separately with proper conversion
            Object altitudeObj = statisticsManager.getValue(StatisticsType.ALTITUDE);
            statistics.setAltitude(convertToDouble(altitudeObj));

            Object speedObj = statisticsManager.getValue(StatisticsType.SPEED);
            statistics.setSpeed(convertToDouble(speedObj));

            Object accuracyObj = statisticsManager.getValue(StatisticsType.ACCURACY);
            statistics.setAccuracy(convertToDouble(accuracyObj));

            Object stepsObj = statisticsManager.getValue(StatisticsType.STEPS);
            statistics.setSteps(convertToInt(stepsObj));

            Object bearingObj = statisticsManager.getValue(StatisticsType.BEARING);
            statistics.setBearing(convertToFloat(bearingObj));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return statistics;
    }

    private void updateAccumulatedValues(Location location) {
        Location prev = previousLocation != null ? previousLocation.get() : null;
        if (prev != null) {
            Location.distanceBetween(
                    prev.getLatitude(), prev.getLongitude(),
                    location.getLatitude(), location.getLongitude(),
                    distanceResults
            );

            Object currentDistanceObj = statisticsManager.getValue(StatisticsType.TOTAL_DISTANCE);
            double currentDistance = convertToDouble(currentDistanceObj);
            statisticsManager.setValue(StatisticsType.TOTAL_DISTANCE, currentDistance + distanceResults[0]);

            double elevDiff = location.getAltitude() - prev.getAltitude();
            if (elevDiff > 0) {
                Object currentGainObj = statisticsManager.getValue(StatisticsType.TOTAL_ELEVATION_GAIN);
                double currentGain = convertToDouble(currentGainObj);
                statisticsManager.setValue(StatisticsType.TOTAL_ELEVATION_GAIN, currentGain + elevDiff);
            }
        }
        previousLocation = new WeakReference<>(location);
    }

    private void updateSession(HikingSessionEntity session) {
        executor.execute(() -> {
            try {
                Object distanceObj = statisticsManager.getValue(StatisticsType.TOTAL_DISTANCE);
                session.setDistance(convertToDouble(distanceObj));

                Object stepsObj = statisticsManager.getValue(StatisticsType.STEPS);
                session.setSteps(convertToInt(stepsObj));

                Object speedObj = statisticsManager.getValue(StatisticsType.SPEED);
                session.setAverageSpeed(convertToDouble(speedObj));

                Object elevationGainObj = statisticsManager.getValue(StatisticsType.TOTAL_ELEVATION_GAIN);
                session.setTotalElevationGain((int) convertToDouble(elevationGainObj));

                sessionDao.update(session);

                // clear step count after updated
                statisticsManager.setValue(StatisticsType.STEPS, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void finalize() throws Throwable {
        executor.shutdown();
        super.finalize();
    }
}