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
import android.graphics.PorterDuff;

import com.skritter.models.Param;
import com.skritter.models.Stroke;
import com.skritter.math.Vector2;
import com.skritter.math.MathUtil;
import com.skritter.utils.ShortStraw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RuneItemPanel extends StudyItemPanel {
    private class StrokeRenderData {
        public int bitmapID;
        public Vector2 position;
        public float rotation;
        public boolean hasBeenDrawn;
        public long t;
    }

    public interface IStrokeListener {
        public void onNewStroke(Vector2[] strokePoints, int numPoints);
    }

    private IStrokeListener strokeListener;

    public void setEventListener(IStrokeListener strokeListener) {
        this.strokeListener = strokeListener;
    }

    private Vector2[] points;
    private int numPoints;
    private static final int maxStrokePoints = 2048;

    private Bitmap[] strokeBitmaps;
    private StrokeRenderData[] strokeRenderData;

    // Bitmap and Canvas for drawing strokes
    private Bitmap strokeBitmap;
    private Canvas strokeCanvas;

    private Paint strokePaint;

    private Path drawPath;

    private List<Vector2> corners = new ArrayList<Vector2>();
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

        points = new Vector2[maxStrokePoints];
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
            strokeRenderData = new StrokeRenderData[strokeData.getStrokes().length];

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

                // Create the stroke render data
                strokeRenderData[i] = new StrokeRenderData();
                strokeRenderData[i].bitmapID = strokeData.getStrokes()[i].strokeID;
                strokeRenderData[i].hasBeenDrawn = false;
//                strokeRenderData[i].position = new Vector2(strokeData.getStrokes()[i].x, strokeData.getStrokes()[i].y);
                strokeRenderData[i].position = new Vector2(0, 0);
                strokeRenderData[i].rotation = 0;
                strokeRenderData[i].t = 0;
            }
        }
    }

    @Override
    public void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);

        strokeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        strokeCanvas = new Canvas(strokeBitmap);
    }

    public void drawNextStroke(Vector2 startPoint) {
        if (strokeRenderData == null) {
            return;
        }

        for (int i = 0; i < strokeRenderData.length; i++) {
            if (!strokeRenderData[i].hasBeenDrawn) {
                strokeRenderData[i].hasBeenDrawn = true;
                strokeRenderData[i].position = startPoint;
                return;
            }
        }
    }

    long animationTime = 300;
    long prevTime;

    @Override
    protected void internalDraw(Canvas canvas) {
        long dt = System.currentTimeMillis() - prevTime;
        prevTime = System.currentTimeMillis();
        drawRuneBackground(canvas);
        canvas.drawBitmap(strokeBitmap, 0, 0, null);
        Paint cornerPaint = new Paint();
        cornerPaint.setColor(Color.RED);

        if (strokeData == null) {
            return;
        }

        for (int i = 0; i < strokeRenderData.length; i++) {
            StrokeRenderData renderData = strokeRenderData[i];
            if (!renderData.hasBeenDrawn) {
                continue;
            }

            if (renderData.t < animationTime) {
                renderData.t += dt;
            } else {
                renderData.t = animationTime;
            }

            float lerpFactor = (float)renderData.t / animationTime;

            Stroke stroke = strokeData.getStrokes()[i];
            float x = stroke.x * customWidth;
            x = MathUtil.easeInOutCubic(renderData.t, renderData.position.x, x - renderData.position.x, animationTime);
            float y = stroke.y * customHeight;
            y = MathUtil.easeInOutCubic(renderData.t, renderData.position.y, y - renderData.position.y, animationTime);

            float scaleX = (stroke.width * customWidth) / strokeBitmaps[i].getWidth();
            float scaleY = (stroke.height * customHeight) / strokeBitmaps[i].getHeight();

            float rotation = -stroke.rotation;
            rotation = MathUtil.lerp(renderData.rotation, rotation, lerpFactor);

            Matrix matrix = new Matrix();
            // Move the bitmap so we can rotate and scale around the center of the image
            matrix.setTranslate(-strokeBitmaps[i].getWidth() * 0.5f, -strokeBitmaps[i].getHeight() * 0.5f);
            matrix.postRotate(rotation);
            matrix.postScale(scaleX, scaleY);
            // Move it back so we draw with the bitmap's upper left corner at (x, y)
            matrix.postTranslate(strokeBitmaps[i].getWidth() * 0.5f * scaleX, strokeBitmaps[i].getHeight() * 0.5f * scaleY);
            matrix.postTranslate(x, y);

            canvas.drawBitmap(strokeBitmaps[i], matrix, null);

            for (int j = 0; j < Param.params.length; j++) {
                if (Param.params[j].bitmapID == strokeRenderData[i].bitmapID) {
                    for (int k = 0; k < Param.params[j].corners.length; k++) {
                        float[] points = new float[]{Param.params[j].corners[k].x, Param.params[j].corners[k].y};
                        matrix.mapPoints(points);

                        canvas.drawCircle(points[0], points[1], 10f, cornerPaint);
                    }
                }
            }
        }

        for (Vector2 corner : corners) {
            if (corner != null) {
                canvas.drawCircle(corner.x, corner.y, 10f, cornerPaint);
            }
        }
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

        numPoints = 0;

        points[numPoints] = new Vector2(x, y);

        numPoints++;

        corners.clear();
    }

    @Override
    protected void internalOnTouchMove(float x, float y) {
        drawPath.quadTo(lastPoint.x, lastPoint.y, (x + lastPoint.x) * 0.5f, (y + lastPoint.y) * 0.5f);
        strokeCanvas.drawPath(drawPath, strokePaint);

        lastPoint.x = x;
        lastPoint.y = y;

        if (numPoints < maxStrokePoints) {
            points[numPoints] = new Vector2(x, y);
        }

        numPoints++;
    }

    @Override
    protected void internalOnTouchUp(float x, float y) {
        drawPath.lineTo(x, y);
        drawPath.reset();

        if (numPoints < maxStrokePoints) {
            points[numPoints] = new Vector2(x, y);
        }

        numPoints++;

        if (strokeListener != null) {
            strokeListener.onNewStroke(points, numPoints);
        }

        strokeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        Vector2[] newCorners = ShortStraw.runShortStraw(points, numPoints);
        for (int i = 0; i < newCorners.length; i++) {
            corners.add(newCorners[i]);
        }
    }
    
    public void clear() {
        strokeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }
}
