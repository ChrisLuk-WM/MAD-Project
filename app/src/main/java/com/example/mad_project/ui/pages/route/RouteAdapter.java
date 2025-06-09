package com.example.mad_project.ui.pages.route;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.database.entities.TrailImage;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {
    private List<TrailWithThumbnail> trails = new ArrayList<>();
    private final Context context;
    private final OnRouteClickListener listener;

    public interface OnRouteClickListener {
        void onRouteStart(TrailEntity trail);
        void onRouteClick(TrailEntity trail);
    }

    public RouteAdapter(Context context, OnRouteClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.layout_route_item, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        TrailWithThumbnail trailWithThumbnail = trails.get(position);
        holder.bind(trailWithThumbnail);
    }

    @Override
    public int getItemCount() {
        return trails.size();
    }

    public void setTrails(List<TrailWithThumbnail> trails) {
        this.trails = trails;
        notifyDataSetChanged();
    }

    class RouteViewHolder extends RecyclerView.ViewHolder {
        private final ImageView routeImage;
        private final TextView routeName;
        private final TextView difficultyRating;
        private final TextView lengthRating;
        private final TextView durationRating;
        private final MaterialButton btnStartRoute;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            routeImage = itemView.findViewById(R.id.route_image);
            routeName = itemView.findViewById(R.id.route_name);
            difficultyRating = itemView.findViewById(R.id.difficulty_rating);
            lengthRating = itemView.findViewById(R.id.length_rating);
            durationRating = itemView.findViewById(R.id.duration_rating);
            btnStartRoute = itemView.findViewById(R.id.btn_start_route);
        }

        void bind(TrailWithThumbnail trailWithThumbnail) {
            TrailEntity trail = trailWithThumbnail.getTrail();
            TrailImage thumbnail = trailWithThumbnail.getThumbnail();

            routeName.setText(trail.getTrailName());
            difficultyRating.setText(String.format(Locale.getDefault(), "%.1f", trail.getDifficultyRating()));
            lengthRating.setText(String.format(Locale.getDefault(), "%.1f km", trail.getLengthRating()));
            durationRating.setText(String.format(Locale.getDefault(), "%.1f h", trail.getDurationRating()));

            // Load thumbnail
            if (thumbnail != null && thumbnail.getImagePath() != null) {
                try {
                    File imgFile = new File(thumbnail.getImagePath());
                    if (imgFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        routeImage.setImageBitmap(bitmap);
                    } else {
                        routeImage.setImageResource(R.drawable.ic_hiking);
                    }
                } catch (Exception e) {
                    routeImage.setImageResource(R.drawable.ic_hiking);
                }
            } else {
                routeImage.setImageResource(R.drawable.ic_hiking);
            }

            itemView.setOnClickListener(v -> listener.onRouteClick(trail));
            btnStartRoute.setOnClickListener(v -> listener.onRouteStart(trail));
        }
    }
}