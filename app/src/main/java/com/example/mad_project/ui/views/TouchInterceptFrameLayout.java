package com.example.mad_project.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

public class TouchInterceptFrameLayout extends FrameLayout {
    private NestedScrollView parentScrollView;

    public TouchInterceptFrameLayout(@NonNull Context context) {
        super(context);
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Find the parent NestedScrollView by traversing up the view hierarchy
        ViewParent parent = getParent();
        while (parent != null) {
            if (parent instanceof NestedScrollView) {
                parentScrollView = (NestedScrollView) parent;
                break;
            }
            parent = parent.getParent();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Disable scrolling of the parent ScrollView when touching the map
                if (parentScrollView != null) {
                    parentScrollView.requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Re-enable scrolling of the parent ScrollView
                if (parentScrollView != null) {
                    parentScrollView.requestDisallowInterceptTouchEvent(false);
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Re-enable scrolling of the parent ScrollView
                if (parentScrollView != null) {
                    parentScrollView.requestDisallowInterceptTouchEvent(false);
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}