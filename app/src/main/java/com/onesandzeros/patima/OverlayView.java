package com.onesandzeros.patima;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.LinkedList;
import java.util.List;

public class OverlayView extends View {

    private List<BoundingBox> results;
    private List<String> labels;
    private Paint boxPaint;
    private Paint textBackgroundPaint;
    private Paint textPaint;
    private Rect bounds;
    private Bitmap guidedBitmap;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        results = new LinkedList<>();
        bounds = new Rect();
        initPaints();
        loadGuidedBitmap(context);  // Load the guided bitmap
    }

    private void loadGuidedBitmap(Context context) {
        guidedBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_guided);  // Adjust the resource name as necessary
    }

    public void clear() {
        textPaint.reset();
        textBackgroundPaint.reset();
        boxPaint.reset();
        results.clear();
        invalidate();
        initPaints();
    }

    private void initPaints() {
        textBackgroundPaint = new Paint();
        textBackgroundPaint.setColor(Color.BLACK);
        textBackgroundPaint.setStyle(Paint.Style.FILL);
        textBackgroundPaint.setTextSize(50f);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(50f);

        boxPaint = new Paint();
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStrokeWidth(8F);
        boxPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the constant image icon
        if (guidedBitmap != null) {
            // Adjust the position (x, y) as needed
            float x = 0; // Example x-coordinate
            float y = 0; // Example y-coordinate
            canvas.drawBitmap(guidedBitmap, x, y, null);
        }

        for (BoundingBox box : results) {
            float left = box.getX1() * getWidth();
            float top = box.getY1() * getHeight();
            float right = box.getX2() * getWidth();
            float bottom = box.getY2() * getHeight();

            canvas.drawRect(left, top, right, bottom, boxPaint);
            String drawableText = box.getClsName();

            //Method for label display while realtime object detection
            /*
            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length(), bounds);
            float textWidth = bounds.width();
            float textHeight = bounds.height();
            canvas.drawRect(
                    left,
                    top,
                    left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                    top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                    textBackgroundPaint
            );
            canvas.drawText(drawableText, left, top + bounds.height(), textPaint);
            */

        }
    }

    public void setResults(List<BoundingBox> boundingBoxes) {
        results = boundingBoxes;
        invalidate();
    }

    private static final int BOUNDING_RECT_TEXT_PADDING = 8;

    public void setResultsGallery(List<BoundingBox> boundingBoxes, List<String> labels) {
        results = boundingBoxes;
        this.labels = labels;
        invalidate();
    }
}


