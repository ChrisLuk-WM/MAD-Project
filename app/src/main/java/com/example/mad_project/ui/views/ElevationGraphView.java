// ElevationGraphView.java
package com.example.mad_project.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ElevationGraphView extends View {
    private final Paint linePaint;
    private final Paint fillPaint;
    private final Paint axisPaint;
    private final Paint textPaint;
    private final Path path;
    private List<Entry> entries;
    private float maxElevation = 10f; // Default max elevation in meters
    private float minElevation = 0f;
    private static final int MAX_POINTS = 100;

    public static class Entry {
        public final float timeSeconds;
        public final float elevationMeters;

        public Entry(float timeSeconds, float elevationMeters) {
            this.timeSeconds = timeSeconds;
            this.elevationMeters = elevationMeters;
        }
    }

    public ElevationGraphView(Context context) {
        this(context, null);
    }

    public ElevationGraphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ElevationGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        linePaint = new Paint();
        linePaint.setColor(Color.GREEN);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        linePaint.setAntiAlias(true);

        fillPaint = new Paint();
        fillPaint.setColor(Color.GREEN);
        fillPaint.setAlpha(50);
        fillPaint.setStyle(Paint.Style.FILL);

        axisPaint = new Paint();
        axisPaint.setColor(Color.GRAY);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(2f);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.RIGHT);

        path = new Path();
        entries = new ArrayList<>();
    }

    public void addEntry(float timeSeconds, float elevationMeters) {
        entries.add(new Entry(timeSeconds, elevationMeters));
        if (entries.size() > MAX_POINTS) {
            entries.remove(0);
        }
        maxElevation = Math.max(maxElevation, elevationMeters + 5);
        minElevation = Math.min(minElevation, elevationMeters - 5);
        invalidate();
    }

    public void setEntries(List<Entry> entries) {
        this.entries = new ArrayList<>(entries);
        maxElevation = Float.MIN_VALUE;
        minElevation = Float.MAX_VALUE;
        for (Entry entry : entries) {
            maxElevation = Math.max(maxElevation, entry.elevationMeters);
            minElevation = Math.min(minElevation, entry.elevationMeters);
        }
        maxElevation += 5;
        minElevation -= 5;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (entries.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();
        float padding = 50f;
        float graphWidth = width - 2 * padding;
        float graphHeight = height - 2 * padding;

        // Draw axes
        canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint);
        canvas.drawLine(padding, padding, padding, height - padding, axisPaint);

        // Draw Y-axis labels
        int yLabels = 5;
        for (int i = 0; i <= yLabels; i++) {
            float elevation = (maxElevation - minElevation) * i / yLabels + minElevation;
            float y = height - padding - (graphHeight * i / yLabels);
            canvas.drawText(String.format("%.0fm", elevation), padding - 5, y + 10, textPaint);
        }

        // Draw the graph
        path.reset();
        float firstX = padding;
        float firstY = height - padding - ((entries.get(0).elevationMeters - minElevation) / (maxElevation - minElevation) * graphHeight);
        path.moveTo(firstX, firstY);

        float timeRange = entries.get(entries.size() - 1).timeSeconds - entries.get(0).timeSeconds;
        if (timeRange == 0) timeRange = 1f;

        for (int i = 1; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            float x = padding + ((entry.timeSeconds - entries.get(0).timeSeconds) / timeRange * graphWidth);
            float y = height - padding - ((entry.elevationMeters - minElevation) / (maxElevation - minElevation) * graphHeight);
            path.lineTo(x, y);
        }

        // Create fill path
        Path fillPath = new Path(path);
        fillPath.lineTo(width - padding, height - padding);
        fillPath.lineTo(padding, height - padding);
        fillPath.close();

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(path, linePaint);
    }
}