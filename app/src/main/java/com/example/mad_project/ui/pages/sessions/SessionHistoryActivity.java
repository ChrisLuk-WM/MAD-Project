package com.example.mad_project.ui.pages.sessions;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.ui.BaseActivity;

public class SessionHistoryActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private SessionHistoryAdapter adapter;
    private SessionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SessionViewModel.class);
        observeSessions();
    }

    @Override
    protected void initViews() {
        recyclerView = findViewById(R.id.sessionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create adapter with click listener
        adapter = new SessionHistoryAdapter(new SessionHistoryAdapter.OnSessionClickListener() {
            @Override
            public void onSessionClick(HikingSessionEntity session) {
                // Handle session click
                showSessionDetails(session);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void observeSessions() {
        viewModel.getAllSessions().observe(this, sessions -> {
            if (sessions != null) {
                adapter.updateSessions(sessions);
            }
        });
    }

    private void showSessionDetails(HikingSessionEntity session) {
        // Navigate to session details activity
        Intent intent = new Intent(this, SessionDetailsActivity.class);
        intent.putExtra("session_id", session.getId());
        startActivity(intent);
    }

    @Override
    protected void setupActions() {
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_session_history;
    }
}