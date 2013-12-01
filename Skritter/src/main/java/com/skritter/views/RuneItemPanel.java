package com.skritter.views;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import com.skritter.models.Stroke;

import java.io.IOException;

public class RuneItemPanel extends StudyItemPanel {
    private Bitmap[] strokeBitmaps;

    // Bitmap and Canvas for drawing strokes
    private Bitmap strokeBitmap;
    private Canvas strokeCanvas;

    private Paint strokePaint;

    private Path drawPath;

    // Keep track of the last point the user has drawn so we can use smooth quad curves
    private PointF lastPoint = new PointF(-1.0f, -1.0f);

    public RuneItemPanel() {
        super();

        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeWidth(10);

        drawPath = new Path();
    }

    @Override
    public void loadAssets(Context context) {
        super.loadAssets(context);
        loadStrokeBitmaps(context);
    }

    private void loadStrokeBitmaps(Context context) {
        if (strokeData != null) {
            if (strokeBitmaps != null && strokeBitmaps.length > 0) {
                for (Bitmap bitmap : strokeBitmaps) {
                    bitmap.recycle();
                }
            }

            strokeBitmaps = new Bitmap[strokeData.getStrokes().length];

            for (int i = 0; i < strokeBitmaps.length; i++) {
                String strokeID = "" + strokeData.getStrokes()[i].strokeID;
                int remainingZeroes = 4 - strokeID.length();

                // Pad the filename with zeroes
                for (int j = 0; j < remainingZeroes; j++) {
                    strokeID = "0" + strokeID;
                }

                String fileName = "strokes/" + strokeID + ".png";
                try {
                    strokeBitmaps[i] = BitmapFactory.decodeStream(context.getAssets().open(fileName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);

        strokeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        strokeCanvas = new Canvas(strokeBitmap);
    }

    @Override
    protected void internalDraw(Canvas canvas) {
        drawRuneBackground(canvas);
        canvas.drawBitmap(strokeBitmap, 0, 0, null);

        if (strokeData == null) {
            return;
        }

        for (int i = 0; i < strokeData.getStrokes().length; i++) {
            Stroke stroke = strokeData.getStrokes()[i];
            float x = stroke.x * customWidth;
            float y = stroke.y * customHeight;

            float scaleX = (stroke.width * customWidth) / strokeBitmaps[i].getWidth();
            float scaleY = (stroke.height * customHeight) / strokeBitmaps[i].getHeight();

            float rotation = -stroke.rotation;

            Matrix matrix = new Matrix();
            // Move the bitmap so we can rotate and scale around the center of the image
            matrix.setTranslate(-strokeBitmaps[i].getWidth() * 0.5f, -strokeBitmaps[i].getHeight() * 0.5f);
            matrix.postRotate(rotation);
            matrix.postScale(scaleX, scaleY);
            // Move it back so we draw with the bitmap's upper left corner at (x, y)
            matrix.postTranslate(strokeBitmaps[i].getWidth() * 0.5f * scaleX, strokeBitmaps[i].getHeight() * 0.5f * scaleY);
            matrix.postTranslate(x, y);

            canvas.drawBitmap(strokeBitmaps[i], matrix, null);
        }

        //Use this code to clear the strokes:
//        strokeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    private void drawRuneBackground(Canvas canvas) {
        // This is only called when the background bitmap is resized in some way
        // Generates a solid colored background with the Skritter grid on top
        canvas.drawColor(backgroundPaint.getColor());
        drawGrid(canvas);
    }

    @Override
    protected void internalOnTouchDown(float x, float y) {
        drawPath.moveTo(x, y);

        lastPoint.x = x;
        lastPoint.y = y;
    }

    @Override
    protected void internalOnTouchMove(float x, float y) {
        drawPath.quadTo(lastPoint.x, lastPoint.y, (x + lastPoint.x) * 0.5f, (y + lastPoint.y) * 0.5f);
        strokeCanvas.drawPath(drawPath, strokePaint);

        lastPoint.x = x;
        lastPoint.y = y;
    }

    @Override
    protected void internalOnTouchUp(float x, float y) {
        drawPath.lineTo(x, y);
        drawPath.reset();
    }
}
