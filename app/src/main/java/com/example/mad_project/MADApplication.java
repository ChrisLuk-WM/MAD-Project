package com.example.mad_project;

import android.app.Application;

import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.example.mad_project.statistics.StatisticsManager;

public class MADApplication extends Application {@Override
    public void onCreate() {
        super.onCreate();

        // Initialize WorkManager once
        WorkManager.initialize(
                this,
                new Configuration.Builder()
                        .setMinimumLoggingLevel(android.util.Log.INFO)
                        .build()
        );

        // Initialize other components
        StatisticsManager.init(this);
    }

}
