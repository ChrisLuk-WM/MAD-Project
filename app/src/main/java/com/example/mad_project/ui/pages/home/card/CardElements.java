package com.example.mad_project.ui.pages.home.card;

import android.view.View;

public abstract class CardElements {
    protected final View containerView;

    protected CardElements(View containerView) {
        this.containerView = containerView;
    }

    public View getContainerView() {
        return containerView;
    }
}