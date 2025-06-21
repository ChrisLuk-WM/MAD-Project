package com.example.mad_project.ui.pages.sessions;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.ui.BaseActivity;

import java.util.List;
import java.util.stream.Collectors;

public class SessionHistoryActivity extends BaseActivity {
    private SessionViewModel viewModel;
    private RecyclerView sessionsRecycler;
    private SessionHistoryAdapter sessionsAdapter;
    private TextView emptyView;
    private TextView resultsCountText;
    private AutoCompleteTextView dateFilterDropdown;
    private AutoCompleteTextView durationFilterDropdown;
    private final SessionFilter sessionFilter = new SessionFilter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SessionViewModel.class);
        setupFilterDropdowns();
        applyFilters();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_session_history;
    }

    @Override
    protected void initViews() {
        sessionsRecycler = findViewById(R.id.sessions_recycler);
        emptyView = findViewById(R.id.empty_view);
        resultsCountText = findViewById(R.id.text_results_count);
        dateFilterDropdown = findViewById(R.id.date_filter_dropdown);
        durationFilterDropdown = findViewById(R.id.duration_filter_dropdown);

        // Add OnSessionClickListener implementation
        sessionsAdapter = new SessionHistoryAdapter(session -> {
            // Handle session click
            Intent intent = new Intent(this, SessionAnalysisActivity.class);
            intent.putExtra("session_id", session.getId());
            startActivity(intent);
        });

        sessionsRecycler.setLayoutManager(new LinearLayoutManager(this));
        sessionsRecycler.setAdapter(sessionsAdapter);

        findViewById(R.id.btn_clear_filters).setOnClickListener(v -> clearFilters());
    }
    private void setupFilterDropdowns() {
        // Setup date filter
        ArrayAdapter<SessionFilter.DateRange> dateAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                SessionFilter.DateRange.values()
        );
        dateFilterDropdown.setAdapter(dateAdapter);
        dateFilterDropdown.setText(SessionFilter.DateRange.ALL.toString(), false);
        dateFilterDropdown.setOnItemClickListener((parent, view, position, id) -> {
            sessionFilter.setDateRange(SessionFilter.DateRange.values()[position]);
            applyFilters();
        });

        // Setup duration filter
        ArrayAdapter<SessionFilter.DurationRange> durationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                SessionFilter.DurationRange.values()
        );
        durationFilterDropdown.setAdapter(durationAdapter);
        durationFilterDropdown.setText(SessionFilter.DurationRange.ALL.toString(), false);
        durationFilterDropdown.setOnItemClickListener((parent, view, position, id) -> {
            sessionFilter.setDurationRange(SessionFilter.DurationRange.values()[position]);
            applyFilters();
        });
    }

    private void clearFilters() {
        sessionFilter.setDateRange(SessionFilter.DateRange.ALL);
        sessionFilter.setDurationRange(SessionFilter.DurationRange.ALL);
        dateFilterDropdown.setText(SessionFilter.DateRange.ALL.toString(), false);
        durationFilterDropdown.setText(SessionFilter.DurationRange.ALL.toString(), false);
        applyFilters();
    }

    private void applyFilters(List<HikingSessionEntity> allSessions) {
        if (allSessions == null) return;

        List<HikingSessionEntity> filteredSessions = allSessions.stream()
                .filter(sessionFilter::matches)
                .collect(Collectors.toList());

        sessionsAdapter.submitList(filteredSessions);
        updateUIState(filteredSessions);
    }

    // When calling applyFilters from other places (like clearFilters), get the current value
    private void applyFilters() {
        viewModel.getAllSessions().observe(this, sessions -> {
            if (sessions != null) {
                // Pass the sessions directly to applyFilters instead of getting it from LiveData again
                applyFilters(sessions);
            }
        });
    }

    private void updateUIState(List<HikingSessionEntity> filteredSessions) {
        if (filteredSessions.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            sessionsRecycler.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            sessionsRecycler.setVisibility(View.VISIBLE);
        }

        resultsCountText.setText(String.format("%d sessions found", filteredSessions.size()));
    }

    @Override
    protected void setupActions() {
        // No additional actions needed
    }
}