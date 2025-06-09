package com.example.mad_project.ui.pages.route;

import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.database.entities.TrailImage;

public class TrailWithThumbnail {
    private final TrailEntity trail;
    private final TrailImage thumbnail;

    public TrailWithThumbnail(TrailEntity trail, TrailImage thumbnail) {
        this.trail = trail;
        this.thumbnail = thumbnail;
    }

    public TrailEntity getTrail() {
        return trail;
    }

    public TrailImage getThumbnail() {
        return thumbnail;
    }
}