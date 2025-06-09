package com.example.mad_project.ui.pages.route;

import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.database.entities.TrailImage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrailWithImages {
    private final TrailEntity trail;
    private final List<TrailImage> galleryImages;

    public TrailWithImages(TrailEntity trail, List<TrailImage> images) {
        this.trail = trail;
        this.galleryImages = images.stream()
                .filter(img -> !img.getImagePath().equals(trail.getImagePath()))
                .collect(Collectors.toList());
    }

    public TrailEntity getTrail() {
        return trail;
    }

    public List<TrailImage> getGalleryImages() {
        return galleryImages;
    }

    // Helper method to get thumbnail
    public TrailImage getThumbnail() {
        return galleryImages.stream()
                .filter(TrailImage::isThumbnail)
                .findFirst()
                .orElse(null);
    }
}