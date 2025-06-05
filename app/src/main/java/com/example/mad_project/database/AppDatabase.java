package com.example.mad_project.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mad_project.database.dao.HikingSessionDao;
import com.example.mad_project.database.dao.HikingStatisticsDao;
import com.example.mad_project.database.dao.TrailDao;
import com.example.mad_project.database.dao.TrailImageDao;
import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.database.entities.HikingStatisticsEntity;
import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.database.entities.TrailImage;

@Database(
        entities = {
                TrailEntity.class,
                HikingSessionEntity.class,
                TrailImage.class,
                HikingStatisticsEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract TrailDao trailDao();
    public abstract HikingSessionDao hikingSessionDao();
    public abstract TrailImageDao trailImageDao();
    public abstract HikingStatisticsDao hikingStatisticsDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "hiking_app_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}