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

public class SpeedGraphView extends View {
    private final Paint linePaint;
    private final Paint fillPaint;
    private final Paint axisPaint;
    private final Paint textPaint;
    private final Path path;
    private List<Entry> entries;
    private float maxSpeed = 10f; // Default max speed in km/h
    private float minSpeed = 0f;
    private static final int MAX_POINTS = 100;

    public static class Entry {
        public final float timeSeconds;
        public final float speedKmh;

        public Entry(float timeSeconds, float speedKmh) {
            this.timeSeconds = timeSeconds;
            this.speedKmh = speedKmh;
        }
    }

    public SpeedGraphView(Context context) {
        this(context, null);
    }

    public SpeedGraphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeedGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        linePaint = new Paint();
        linePaint.setColor(Color.BLUE);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        linePaint.setAntiAlias(true);

        fillPaint = new Paint();
        fillPaint.setColor(Color.BLUE);
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

    public void addEntry(float timeSeconds, float speedKmh) {
        entries.add(new Entry(timeSeconds, speedKmh));
        if (entries.size() > MAX_POINTS) {
            entries.remove(0);
        }
        maxSpeed = Math.max(maxSpeed, speedKmh + 2);
        invalidate();
    }

    public void setEntries(List<Entry> entries) {
        this.entries = new ArrayList<>(entries);
        maxSpeed = 0f;
        for (Entry entry : entries) {
            maxSpeed = Math.max(maxSpeed, entry.speedKmh);
        }
        maxSpeed += 2; // Add padding
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
        canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint); // X-axis
        canvas.drawLine(padding, padding, padding, height - padding, axisPaint); // Y-axis

        // Draw Y-axis labels
        int yLabels = 5;
        for (int i = 0; i <= yLabels; i++) {
            float speed = (maxSpeed - minSpeed) * i / yLabels + minSpeed;
            float y = height - padding - (graphHeight * i / yLabels);
            canvas.drawText(String.format("%.1f", speed), padding - 5, y + 10, textPaint);
        }

        // Draw the graph
        path.reset();
        float firstX = padding;
        float firstY = height - padding - (entries.get(0).speedKmh / maxSpeed * graphHeight);
        path.moveTo(firstX, firstY);

        float timeRange = entries.get(entries.size() - 1).timeSeconds - entries.get(0).timeSeconds;
        if (timeRange == 0) timeRange = 1f; // Prevent division by zero

        for (int i = 1; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            float x = padding + ((entry.timeSeconds - entries.get(0).timeSeconds) / timeRange * graphWidth);
            float y = height - padding - (entry.speedKmh / maxSpeed * graphHeight);
            path.lineTo(x, y);
        }

        // Create fill path
        Path fillPath = new Path(path);
        fillPath.lineTo(width - padding, height - padding);
        fillPath.lineTo(padding, height - padding);
        fillPath.close();

        // Draw fill and line
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(path, linePaint);
    }

    public void setTextColor(int color) {
        textPaint.setColor(color);
        invalidate();
    }

    public void setGridColor(int color) {
        axisPaint.setColor(color);
        invalidate();
    }

    public void setLineColor(int color) {
        linePaint.setColor(color);
        fillPaint.setColor(color);
        fillPaint.setAlpha(50); // Keep transparency for fill
        invalidate();
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        invalidate();
    }
}