// StepsGraphView.java
package com.example.mad_project.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.HashMap;
import java.util.Map;

public class StepsGraphView extends View {
    private final Paint barPaint;
    private final Paint axisPaint;
    private final Paint textPaint;
    private Map<Integer, Integer> stepsPerMinute; // minute -> steps
    private int maxSteps = 10; // Default max steps per minute
    private static final int BAR_SPACING = 10; // pixels between bars

    public StepsGraphView(Context context) {
        this(context, null);
    }

    public StepsGraphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepsGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        barPaint = new Paint();
        barPaint.setColor(Color.BLUE);
        barPaint.setStyle(Paint.Style.FILL);

        axisPaint = new Paint();
        axisPaint.setColor(Color.GRAY);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(2f);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.RIGHT);

        stepsPerMinute = new HashMap<>();
    }

    public void updateSteps(Map<Integer, Integer> newStepsData) {
        stepsPerMinute = new HashMap<>(newStepsData);
        maxSteps = 10;
        for (int steps : newStepsData.values()) {
            maxSteps = Math.max(maxSteps, steps + 5);
        }
        invalidate();
    }

    public void addSteps(int minute, int steps) {
        stepsPerMinute.put(minute, steps);
        maxSteps = Math.max(maxSteps, steps + 5);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (stepsPerMinute.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();
        float padding = 50f;
        float graphWidth = width - 2 * padding;
        float graphHeight = height - 2 * padding;

        // Draw axes
        canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint);
        canvas.drawLine(padding, padding, padding, height - padding, axisPaint);

        // Draw Y-axis labels (steps)
        int yLabels = 5;
        for (int i = 0; i <= yLabels; i++) {
            float steps = maxSteps * i / yLabels;
            float y = height - padding - (graphHeight * i / yLabels);
            canvas.drawText(String.format("%.0f", steps), padding - 5, y + 10, textPaint);
        }

        // Calculate bar width
        int barCount = stepsPerMinute.size();
        float barWidth = (graphWidth - (barCount + 1) * BAR_SPACING) / barCount;

        // Draw bars
        int i = 0;
        for (Map.Entry<Integer, Integer> entry : stepsPerMinute.entrySet()) {
            float x = padding + BAR_SPACING + i * (barWidth + BAR_SPACING);
            float barHeight = (entry.getValue() / (float) maxSteps) * graphHeight;
            float y = height - padding - barHeight;

            // Draw bar
            canvas.drawRect(x, y, x + barWidth, height - padding, barPaint);

            // Draw minute label
            canvas.drawText(String.valueOf(entry.getKey()), x + barWidth/2, height - padding + 30, textPaint);

            i++;
        }
    }

    public void setTextColor(int color) {
        textPaint.setColor(color);
        invalidate();
    }

    public void setGridColor(int color) {
        axisPaint.setColor(color);
        invalidate();
    }

    public void setBarColor(int color) {  // Instead of setLineColor for bar graph
        barPaint.setColor(color);
        invalidate();
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        invalidate();
    }
}