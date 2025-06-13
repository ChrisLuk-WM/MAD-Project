package com.example.mad_project.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.mad_project.database.converters.LocalDateTimeConverter;
import com.example.mad_project.database.dao.EmergencyContactDao;
import com.example.mad_project.database.dao.HikingSessionDao;
import com.example.mad_project.database.dao.HikingStatisticsDao;
import com.example.mad_project.database.dao.ProfileDao;
import com.example.mad_project.database.dao.TrailDao;
import com.example.mad_project.database.dao.TrailImageDao;
import com.example.mad_project.database.dao.WeatherHistoryDao;
import com.example.mad_project.database.entities.EmergencyContactEntity;
import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.database.entities.HikingStatisticsEntity;
import com.example.mad_project.database.entities.ProfileEntity;
import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.database.entities.TrailImage;
import com.example.mad_project.database.entities.WeatherHistoryEntity;

@Database(
        entities = {
                TrailEntity.class,
                HikingSessionEntity.class,
                TrailImage.class,
                HikingStatisticsEntity.class,
                ProfileEntity.class,
                EmergencyContactEntity.class,
                WeatherHistoryEntity.class
        },
        version = 6,
        exportSchema = false
)
@TypeConverters({LocalDateTimeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    private static final String FREE_HIKING_TRAIL_NAME = "Free Hiking";

    public abstract TrailDao trailDao();
    public abstract HikingSessionDao hikingSessionDao();
    public abstract TrailImageDao trailImageDao();
    public abstract HikingStatisticsDao hikingStatisticsDao();
    public abstract ProfileDao profileDao();
    public abstract EmergencyContactDao emergencyContactDao();
    public abstract WeatherHistoryDao weatherHistoryDao();

    // Database creation callback
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Insert the default "Free Hiking" trail
            new Thread(() -> {
                TrailEntity freeHikingTrail = new TrailEntity(
                        FREE_HIKING_TRAIL_NAME,
                        0.0,  // sightRating
                        0.0,  // difficultyRating
                        0.0,  // lengthRating
                        0.0,  // durationRating
                        ""    // sourceUrl
                );
                freeHikingTrail.setId(1);
                INSTANCE.trailDao().insert(freeHikingTrail);
            }).start();
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "hiking_app_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(roomCallback)  // Add the callback
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}