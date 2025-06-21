package com.example.mad_project.ui.pages.sessions;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

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
    protected int getLayoutResourceId() {
        return R.layout.activity_session_history;
    }

    @Override
    protected boolean useNavigationDrawer() {
        return false; // Disable navigation drawer for this activity
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(SessionViewModel.class);
        super.onCreate(savedInstanceState);

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Session History");
        }

        observeSessions();
    }

    @Override
    protected void initViews() {
        recyclerView = findViewById(R.id.sessionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SessionHistoryAdapter(session -> {
            Intent intent = new Intent(this, SessionAnalysisActivity.class);
            intent.putExtra("session_id", session.getId());
            intent.putExtra("source", "history");
            startActivity(intent);
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

    @Override
    protected void setupActions() {
        // Back button handling is automatic through BaseActivity
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}