package com.example.mad_project.ui.pages.route;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.TrailImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RouteImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_EMPTY = 1;

    private List<TrailImage> images = new ArrayList<>();
    private final OnImageClickListener listener;
    private boolean isEmpty = false;

    public interface OnImageClickListener {
        void onImageClick(TrailImage image);
    }

    public RouteImageAdapter(OnImageClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_EMPTY) {
            View view = inflater.inflate(R.layout.layout_empty_gallery, parent, false);
            return new EmptyViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.layout_route_image_item, parent, false);
            return new ImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).bind(images.get(position), position);
        }
        // EmptyViewHolder doesn't need binding
    }

    @Override
    public int getItemViewType(int position) {
        return isEmpty ? TYPE_EMPTY : TYPE_IMAGE;
    }

    @Override
    public int getItemCount() {
        return isEmpty ? 1 : images.size();
    }

    public void setImages(List<TrailImage> images) {
        if (images == null || images.isEmpty()) {
            this.images.clear();
            this.isEmpty = true;
        } else {
            this.images = images;
            this.isEmpty = false;
        }
        notifyDataSetChanged();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView counterView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.route_image);
            counterView = itemView.findViewById(R.id.image_counter);
        }

        void bind(TrailImage image, int position) {
            try {
                File imgFile = new File(image.getImagePath());
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.ic_hiking);
                }
            } catch (Exception e) {
                e.printStackTrace();
                imageView.setImageResource(R.drawable.ic_hiking);
            }

            counterView.setText(String.format(Locale.getDefault(), "%d/%d", position + 1, images.size()));
            itemView.setOnClickListener(v -> listener.onImageClick(image));
        }
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}