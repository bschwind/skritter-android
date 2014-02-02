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
import java.util.HashMap;
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

    private HashMap<Integer, Bitmap> strokeBitmaps;
    private HashMap<Stroke, StrokeRenderData> strokeRenderDataMap;

    // Bitmap and Canvas for drawing strokes
    private Bitmap strokeBitmap;
    private Canvas strokeCanvas;

    private Paint strokePaint;
    private float strokeWidth;

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
        strokePaint.setStrokeWidth(20);

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
            if (strokeBitmaps != null && strokeBitmaps.size() > 0) {
                for (Bitmap bitmap : strokeBitmaps.values()) {
                    bitmap.recycle();
                }
                strokeBitmaps.clear();
            } else {
                strokeBitmaps = new HashMap<Integer, Bitmap>();
            }
            
            if (strokeRenderDataMap != null && strokeRenderDataMap.size() > 0) {
                strokeRenderDataMap.clear();
            } else {
                strokeRenderDataMap = new HashMap<Stroke, StrokeRenderData>();
            }
            
            for (int i = 0; i < strokeData.getStrokes().length; i++) {
                for (int j = 0; j < strokeData.getStrokes()[i].length; j++) {
                    Stroke stroke = strokeData.getStrokes()[i][j];
                    
                    if (!strokeBitmaps.containsKey(stroke.strokeID)) {
                        strokeBitmaps.put(stroke.strokeID, loadBitmapFromBitmapID(stroke.strokeID, context));
                    }
                    
                    if (!strokeRenderDataMap.containsKey(stroke)) {
                        StrokeRenderData newStrokeRenderData = new StrokeRenderData();
                        newStrokeRenderData.bitmapID = stroke.strokeID;
                        newStrokeRenderData.hasBeenDrawn = false;
                        newStrokeRenderData.position = new Vector2(0, 0);
                        newStrokeRenderData.rotation = 0;
                        newStrokeRenderData.t = 0;
                        
                        strokeRenderDataMap.put(stroke, newStrokeRenderData);
                    }
                }
            }
        }
    }
    
    private Bitmap loadBitmapFromBitmapID(int bitmapID, Context context) {
        String strokeID = "" + bitmapID;
        int remainingZeroes = 4 - strokeID.length();

        // Pad the filename with zeroes
        for (int j = 0; j < remainingZeroes; j++) {
            strokeID = "0" + strokeID;
        }

        String fileName = "strokes/" + strokeID + ".png";
        try {
            return BitmapFactory.decodeStream(context.getAssets().open(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    @Override
    public void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);

        strokeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        strokeCanvas = new Canvas(strokeBitmap);
        
        strokeWidth = 0.026f * width;
        strokePaint.setStrokeWidth(strokeWidth);
    }

    public void drawNextStroke(Stroke stroke, Vector2 startPoint, float startAngle) {
        if (strokeRenderDataMap == null) {
            return;
        }
        
        StrokeRenderData renderData = strokeRenderDataMap.get(stroke);

        renderData.hasBeenDrawn = true;
        renderData.position = startPoint;
        renderData.rotation = (float)(startAngle * (180.0f / (Math.PI)));

        float targetAngle = 0.0f;

        for (int j = 0; j < Param.params.length; j++) {
            if (Param.params[j].bitmapID == renderData.bitmapID) {
                Vector2 start = Param.params[j].corners[0];
                Vector2 end = Param.params[j].corners[Param.params[j].corners.length-1];

                Vector2 dir = new Vector2(end.x - start.x, end.y - start.y);
                targetAngle = Vector2.angleBetweenVectors(new Vector2(1.0f, 0.0f), dir);
                targetAngle = (float)(targetAngle * (180.0f / (Math.PI)));
                break;
            }
        }

        renderData.rotation = renderData.rotation - targetAngle;
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

        for (Stroke stroke : strokeRenderDataMap.keySet()) {
            StrokeRenderData renderData = strokeRenderDataMap.get(stroke);
            
            if (!renderData.hasBeenDrawn) {
                continue;
            }

            if (renderData.t < animationTime) {
                renderData.t += dt;
            } else {
                renderData.t = animationTime;
                
                // possibly continue here, so we don't have to do any calculations
            }

            float lerpFactor = (float)renderData.t / animationTime;

            Bitmap strokeBitmap = strokeBitmaps.get(renderData.bitmapID);
            
            float x = stroke.x * customWidth;
            x = MathUtil.easeInOutCubic(renderData.t, renderData.position.x, x - renderData.position.x, animationTime);
            float y = stroke.y * customHeight;
            y = MathUtil.easeInOutCubic(renderData.t, renderData.position.y, y - renderData.position.y, animationTime);

            float scaleX = (stroke.width * customWidth) / strokeBitmap.getWidth();
            float scaleY = (stroke.height * customHeight) / strokeBitmap.getHeight();

            float rotation = -stroke.rotation;
            rotation = MathUtil.easeInOutCubic(renderData.t, renderData.rotation, rotation - renderData.rotation, animationTime);

            Matrix matrix = new Matrix();
            // Move the bitmap so we can rotate and scale around the center of the image
            matrix.setTranslate(-strokeBitmap.getWidth() * 0.5f, -strokeBitmap.getHeight() * 0.5f);
            matrix.postRotate(rotation);
            matrix.postScale(scaleX, scaleY);
            // Move it back so we draw with the bitmap's upper left corner at (x, y)
            matrix.postTranslate(strokeBitmap.getWidth() * 0.5f * scaleX, strokeBitmap.getHeight() * 0.5f * scaleY);
            matrix.postTranslate(x, y);

            canvas.drawBitmap(strokeBitmap, matrix, null);

//            for (int j = 0; j < Param.params.length; j++) {
//                if (Param.params[j].bitmapID == strokeRenderDataMap[i].bitmapID) {
//                    for (int k = 0; k < Param.params[j].corners.length; k++) {
//                        float[] points = new float[]{Param.params[j].corners[k].x, Param.params[j].corners[k].y};
//                        matrix.mapPoints(points);
//
//                        canvas.drawCircle(points[0], points[1], 10f, cornerPaint);
//                    }
//                }
//            }
        }

//        for (Vector2 corner : corners) {
//            if (corner != null) {
//                canvas.drawCircle(corner.x, corner.y, 10f, cornerPaint);
//            }
//        }
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

        clear();

        Vector2[] newCorners = ShortStraw.runShortStraw(points, numPoints);
        for (int i = 0; i < newCorners.length; i++) {
            corners.add(newCorners[i]);
        }
    }
    
    public void clear() {
        strokeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }
}
