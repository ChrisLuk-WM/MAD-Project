package com.example.mad_project.ui.pages.sessions;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.HikingSessionEntity;
import com.example.mad_project.database.repository.TrailRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlannedSessionAdapter extends RecyclerView.Adapter<PlannedSessionAdapter.ViewHolder> {
    private List<HikingSessionEntity> sessions = new ArrayList<>();
    private final OnSessionActionListener listener;

    public interface OnSessionActionListener {
        void onStartSession(HikingSessionEntity session);
    }

    public PlannedSessionAdapter(OnSessionActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_planned_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(sessions.get(position));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public void updateSessions(List<HikingSessionEntity> newSessions) {
        sessions.clear();
        sessions.addAll(newSessions);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView trailNameText;
        private final TextView difficultyText;
        private final TextView durationText;
        private final Button startButton;

        ViewHolder(View itemView) {
            super(itemView);
            trailNameText = itemView.findViewById(R.id.text_trail_name);
            difficultyText = itemView.findViewById(R.id.text_difficulty);
            durationText = itemView.findViewById(R.id.text_duration);
            startButton = itemView.findViewById(R.id.btn_start);
        }

        void bind(HikingSessionEntity session) {
            // Get trail information using Activity context
            Context context = itemView.getContext();
            if (context instanceof Activity) {
                TrailRepository trailRepository = new TrailRepository((Application) context.getApplicationContext());
                trailRepository.getTrailById(session.getTrailId()).observe((LifecycleOwner) context, trail -> {
                    if (trail != null) {
                        trailNameText.setText(trail.getTrailName());
                        difficultyText.setText(String.format(Locale.getDefault(), "%.1f", trail.getDifficultyRating()));
                        durationText.setText(String.format(Locale.getDefault(), "%.1f h", trail.getDurationRating()));
                    }
                });
            }

            startButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStartSession(session);
                }
            });
        }
    }
}