package com.example.mad_project.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.example.mad_project.R;

public class DraggableContainer extends LinearLayout {
    private View topView;
    private View bottomView;
    private View dragHandle;
    private float lastY;
    private static final float MIN_WEIGHT = 0.22f;
    private static final float MAX_WEIGHT = 0.78f;
    private static final float INITIAL_TOP_WEIGHT = 0.7f;

    private int dragHandleHeight;
    private boolean isDragging = false;
    private float dragStartY;
    private float initialTopWeight;

    public DraggableContainer(Context context) {
        super(context);
        init(null);
    }

    public DraggableContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setOrientation(VERTICAL);
        dragHandleHeight = getResources().getDimensionPixelSize(R.dimen.drag_handle_height);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() >= 3) {
            topView = getChildAt(0);
            dragHandle = getChildAt(1);
            bottomView = getChildAt(2);

            setupInitialWeights();
            setupDragHandle();
            setupBottomViewDrag();
        }
    }

    private void setupInitialWeights() {
        LinearLayout.LayoutParams topParams = (LinearLayout.LayoutParams) topView.getLayoutParams();
        LinearLayout.LayoutParams bottomParams = (LinearLayout.LayoutParams) bottomView.getLayoutParams();

        topParams.weight = INITIAL_TOP_WEIGHT;
        bottomParams.weight = 1 - INITIAL_TOP_WEIGHT;

        topView.setLayoutParams(topParams);
        bottomView.setLayoutParams(bottomParams);
    }

    private void setupDragHandle() {
        // Make drag handle more visible
        dragHandle.setBackgroundResource(R.drawable.drag_handle_background);

        // Set fixed height for drag handle
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, dragHandleHeight);
        dragHandle.setLayoutParams(handleParams);
    }

    private void setupBottomViewDrag() {
        // Create touch listener for both drag handle and bottom view
        View.OnTouchListener dragListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isDragging = true;
                        lastY = event.getRawY();
                        dragStartY = event.getRawY();
                        initialTopWeight = ((LinearLayout.LayoutParams) topView.getLayoutParams()).weight;
                        dragHandle.setBackgroundResource(R.drawable.drag_handle_background_active);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (isDragging) {
                            float delta = event.getRawY() - lastY;
                            updateWeights(delta);
                            lastY = event.getRawY();
                            return true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isDragging = false;
                        dragHandle.setBackgroundResource(R.drawable.drag_handle_background);
                        return true;
                }
                return false;
            }
        };

        // Apply listener to both drag handle and bottom view
        dragHandle.setOnTouchListener(dragListener);
        bottomView.setOnTouchListener(dragListener);
    }

    private void updateWeights(float deltaY) {
        float totalHeight = getHeight() - dragHandleHeight;
        float topHeight = topView.getHeight() + deltaY;
        float bottomHeight = totalHeight - topHeight;

        float topWeight = topHeight / totalHeight;
        topWeight = Math.max(MIN_WEIGHT, Math.min(MAX_WEIGHT, topWeight));
        float bottomWeight = 1 - topWeight;

        LinearLayout.LayoutParams topParams = (LinearLayout.LayoutParams) topView.getLayoutParams();
        LinearLayout.LayoutParams bottomParams = (LinearLayout.LayoutParams) bottomView.getLayoutParams();

        topParams.weight = topWeight;
        bottomParams.weight = bottomWeight;

        topView.setLayoutParams(topParams);
        bottomView.setLayoutParams(bottomParams);
    }
}