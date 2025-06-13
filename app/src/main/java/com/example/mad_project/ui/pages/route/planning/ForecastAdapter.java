package com.example.mad_project.ui.pages.route.planning;

import static com.example.mad_project.ui.pages.route.planning.ForecastAdapter.RecommendationStatus.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {
    private List<ForecastItem> items = new ArrayList<>();
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnForecastSelectedListener listener;

    public interface OnForecastSelectedListener {
        void onForecastSelected(ForecastItem item, int position);
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardView;
        final TextView dayOfWeek;
        final TextView date;
        final TextView recommendation;

        ForecastViewHolder(View view) {
            super(view);
            cardView = (MaterialCardView) view;
            dayOfWeek = view.findViewById(R.id.day_of_week);
            date = view.findViewById(R.id.date);
            recommendation = view.findViewById(R.id.recommendation);
        }

        void bind(ForecastItem item, boolean isSelected, OnForecastSelectedListener listener) {
            Context context = itemView.getContext();

            dayOfWeek.setText(item.getDayOfWeek());
            date.setText(item.getDate());

            // Set recommendation text and color based on status
            if (item.getRecommendation() != null) {
                RecommendationStatus status;
                switch (item.getRecommendation().recommendationClass) {
                    case 2:
                        recommendation.setText("Good");
                        recommendation.setTextColor(context.getColor(R.color.recommendation_safe));
                        cardView.setCardBackgroundColor(context.getColor(R.color.recommendation_safe_light));
                        status = RECOMMENDED;
                        break;

                    case 1:
                        recommendation.setText("Caution");
                        recommendation.setTextColor(context.getColor(R.color.recommendation_caution));
                        cardView.setCardBackgroundColor(context.getColor(R.color.recommendation_caution_light));
                        status = CAUTION;
                        break;

                    case 0:
                    default:
                        recommendation.setText("Avoid");
                        recommendation.setTextColor(context.getColor(R.color.recommendation_unsafe));
                        cardView.setCardBackgroundColor(context.getColor(R.color.recommendation_unsafe_light));
                        status = NOT_RECOMMENDED;
                        break;
                }

                // Handle selection state
                cardView.setChecked(isSelected);
                if (isSelected) {
                    cardView.setStrokeWidth(context.getResources().getDimensionPixelSize(R.dimen.card_selected_stroke_width));
                    cardView.setStrokeColor(getStrokeColorForStatus(context, status));
                } else {
                    cardView.setStrokeWidth(0);
                }
            }

            // Set click listener
            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onForecastSelected(item, position);
                }
            });
        }

        private @ColorInt int getStrokeColorForStatus(Context context, RecommendationStatus status) {
            switch (status) {
                case RECOMMENDED:
                    return context.getColor(R.color.recommendation_safe_dark);
                case CAUTION:
                    return context.getColor(R.color.recommendation_caution_dark);
                case NOT_RECOMMENDED:
                    return context.getColor(R.color.recommendation_unsafe_dark);
                default:
                    return context.getColor(R.color.black);
            }
        }
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_forecast_card, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        holder.bind(items.get(position), position == selectedPosition, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<ForecastItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setOnForecastSelectedListener(OnForecastSelectedListener listener) {
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        int previousSelected = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousSelected);
        notifyItemChanged(selectedPosition);
    }

    public enum RecommendationStatus {
        RECOMMENDED,
        CAUTION,
        NOT_RECOMMENDED
    }
}