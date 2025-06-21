package com.example.mad_project.ui.pages.sessions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mad_project.R;
import com.example.mad_project.database.entities.HikingSessionEntity;
import com.google.android.material.card.MaterialCardView;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SessionHistoryAdapter extends RecyclerView.Adapter<SessionHistoryAdapter.SessionViewHolder> {
    private List<HikingSessionEntity> sessions = new ArrayList<>();
    private OnSessionClickListener listener;
    private static final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public interface OnSessionClickListener {
        void onSessionClick(HikingSessionEntity session);
    }

    public SessionHistoryAdapter(OnSessionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_history, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
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

    class SessionViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView dateText;
        private final TextView durationText;
        private final TextView distanceText;
        private final TextView elevationGainText;
        private final Button analysisButton;

        SessionViewHolder(View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            dateText = itemView.findViewById(R.id.text_date);
            durationText = itemView.findViewById(R.id.text_duration);
            distanceText = itemView.findViewById(R.id.text_distance);
            elevationGainText = itemView.findViewById(R.id.text_elevation_gain);
            analysisButton = itemView.findViewById(R.id.btn_analysis);
        }


        void bind(HikingSessionEntity session) {
            dateText.setText(session.getStartTime().format(dateFormatter));
            durationText.setText(formatDuration(session.getDuration()));
            distanceText.setText(String.format(Locale.getDefault(),
                    "%.2f km", session.getDistance() / 1000.0));
            elevationGainText.setText(String.format(Locale.getDefault(),
                    "%d m", session.getTotalElevationGain()));

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSessionClick(session);
                }
            });

            analysisButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSessionClick(session);
                }
            });
        }

        private String formatDuration(long milliseconds) {
            long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60;
            return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
        }
    }
}