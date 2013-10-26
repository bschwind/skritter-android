package com.skritter.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PromptCanvas extends View {
    // Bitmap and Canvas for drawing the background grid
    private Bitmap backgroundBitmap;
    private Canvas backgroundCanvas;

    // Bitmap and Canvas for drawing strokes
    private Bitmap strokeBitmap;
    private Canvas strokeCanvas;

    // The different Paint styles for drawing the canvas elements
    private Paint strokePaint, backgroundPaint, gridPaint, statusBorderPaint;

    // The status border is red or green, depending on whether or not the user got the correct stroke
    private boolean shouldDrawStatusBorder = false;

    // The Path object used to draw the user's strokes
    private Path drawPath;

    // Keep track of the last point the user has drawn so we can use smooth quad curves
    private PointF lastPoint = new PointF(-1.0f, -1.0f);

    public PromptCanvas(Context context) {
        super(context);
        defaults();
    }
    public PromptCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        defaults();
    }
    public PromptCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        defaults();
    }

    protected void onDraw(Canvas canvas) {
        // Draw the background, the pre-existing strokes, and the new strokes into the View's canvas
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(strokeBitmap, 0, 0, null);
        canvas.drawPath(drawPath, strokePaint);

        if (shouldDrawStatusBorder) {
            drawStatusBorder(canvas);
        }
    }

    private void drawStatusBorder(Canvas canvas) {
        canvas.drawLine(0, 0, canvas.getWidth(), 0, statusBorderPaint);
        canvas.drawLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight(), statusBorderPaint);
        canvas.drawLine(canvas.getWidth(), canvas.getHeight(), 0, canvas.getHeight(), statusBorderPaint);
        canvas.drawLine(0, canvas.getHeight(), 0, 0, statusBorderPaint);
    }

    private void drawBackground(Canvas canvas) {
        // This is only called when the background bitmap is resized in some way
        // Generates a solid colored background with the Skritter grid on top
        canvas.drawColor(backgroundPaint.getColor());
        drawGrid(canvas);
    }

    private void drawGrid(Canvas canvas) {
        // Draw intersecting lines
        canvas.drawLine(0, 0, canvas.getWidth(), canvas.getHeight(), gridPaint);
        canvas.drawLine(canvas.getWidth(), 0, 0, canvas.getHeight(), gridPaint);
        canvas.drawLine(canvas.getWidth() * 0.5f, 0, canvas.getWidth() * 0.5f, canvas.getHeight(), gridPaint);
        canvas.drawLine(0, canvas.getHeight() * 0.5f, canvas.getWidth(), canvas.getHeight() * 0.5f, gridPaint);

        // Draw a thin border
        canvas.drawLine(0, 0, canvas.getWidth(), 0, gridPaint);
        canvas.drawLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight(), gridPaint);
        canvas.drawLine(canvas.getWidth(), canvas.getHeight(), 0, canvas.getHeight(), gridPaint);
        canvas.drawLine(0, canvas.getHeight(), 0, 0, gridPaint);
    }

    public void clearStrokes() {
        // The stroke bitmap is overlaid on the background, so set the whole image to
        // transparent to clear the strokes
        strokeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        // todo - Call bitmap.recycle() on these bitmaps...if that actually helps clear resources

        backgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        backgroundCanvas = new Canvas(backgroundBitmap);
        drawBackground(backgroundCanvas);

        strokeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        strokeCanvas = new Canvas(strokeBitmap);
    }

    // This is experimental. Currently it is used to enforce a square canvas every time we resize.
    // We'll probably want to consider other ways of doing this, as it could end up making a square
    // larger than we want.
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size ;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (width > height) {
            size = height;
        } else {
            size = width;
        }
        setMeasuredDimension(size, size);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touchUp(x, y);
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private void touchDown(float x, float y) {
        drawPath.close();
        drawPath.moveTo(x, y);

        lastPoint.x = x;
        lastPoint.y = y;
    }

    private void touchMove(float x, float y) {
        drawPath.quadTo(lastPoint.x, lastPoint.y, (x + lastPoint.x) * 0.5f, (y + lastPoint.y) * 0.5f);

        lastPoint.x = x;
        lastPoint.y = y;
    }

    private void touchUp(float x, float y) {
        drawPath.lineTo(x, y);
        strokeCanvas.drawPath(drawPath, strokePaint);
        drawPath.reset();
    }

    private void defaults() {
        drawPath = new Path();

        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeWidth(10);

        gridPaint = new Paint();
        gridPaint.setAntiAlias(true);
        gridPaint.setColor(Color.BLACK);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2);

        statusBorderPaint = new Paint();
        statusBorderPaint.setAntiAlias(true);
        statusBorderPaint.setColor(Color.GREEN);
        statusBorderPaint.setStyle(Paint.Style.STROKE);
        statusBorderPaint.setStrokeWidth(6);

        backgroundPaint = new Paint(Color.LTGRAY);
    }

    public void setPenColor(int color) {
        strokePaint.setColor(color);
    }

    public void setBackgroundColor(int color) {
        backgroundPaint.setColor(color);
    }

    public void setGridColor(int color) {
        gridPaint.setColor(color);
    }

    public void setStatusBorderColor(int color) {
        statusBorderPaint.setColor(color);
    }

    public void setShouldDrawStatusBorder(boolean shouldDrawStatusBorder) {
        this.shouldDrawStatusBorder = shouldDrawStatusBorder;
    }

    public boolean shouldDrawStatusBorder() {
        return shouldDrawStatusBorder;
    }

    /*******************************/
    /****  JS to Java functions ****/
    /*******************************/

    public void drawStroke(JSONObject strokeJSON) throws JSONException {
        Stroke stroke = new Stroke();

        JSONObject strokeObject = strokeJSON.getJSONObject("stroke");
        stroke.bitmapID = strokeObject.getInt("bitmapId");
        stroke.userDimensions = parseJSONDimension(strokeObject.getJSONObject("userDimensions"));
        stroke.actualDimensions = parseJSONDimension(strokeObject.getJSONObject("actualDimensions"));

        int alpha = strokeJSON.getInt("alpha");

        // todo - tween the stroke bitmap, starting at the user bounding box (and user rotation)
        // and ending at the actual bounding box and actual rotation
        // Use Android's Property Animation (http://developer.android.com/guide/topics/graphics/prop-animation.html)

    }

    public void drawSquig(JSONObject squigJSON) throws JSONException {
        float alpha = (float)squigJSON.getDouble("alpha");
        JSONArray jsonPointsArray = squigJSON.getJSONArray("pointsArray");
        PointF[] points = new PointF[jsonPointsArray.length()];

        for (int i = 0; i < points.length; i++) {
            float x = (float)jsonPointsArray.getJSONObject(i).getDouble("x");
            float y = (float)jsonPointsArray.getJSONObject(i).getDouble("y");

            points[i] = new PointF(x, y);
        }

        drawStrokeFromPoints(points);
    }

    private void drawStrokeFromPoints(PointF[] points) {
        if (points.length <= 1) {
            return;
        }

        PointF tempLastPoint = points[0];
        drawPath.close();
        drawPath.moveTo(tempLastPoint.x, tempLastPoint.y);

        for (int i = 1; i < points.length-1; i++) {
            float x = points[i].x;
            float y = points[i].y;
            drawPath.quadTo(tempLastPoint.x, tempLastPoint.y, (x + tempLastPoint.x) * 0.5f, (y + tempLastPoint.y) * 0.5f);

            tempLastPoint.x = x;
            tempLastPoint.y = y;
        }

        drawPath.lineTo(points[points.length-1].x, points[points.length-1].y);
        strokeCanvas.drawPath(drawPath, strokePaint);
        drawPath.reset();
    }

    public void drawCharacter(JSONObject characterJSON) throws JSONException{
        JSONArray strokeArray = characterJSON.getJSONArray("strokeArray");

        for (int i = 0; i < strokeArray.length(); i++) {
            drawStroke(strokeArray.getJSONObject(i));
        }
    }

    public void drawText(JSONObject textJSON) throws JSONException{
        String text = textJSON.getString("text");
        float x = (float)textJSON.getJSONObject("position").getDouble("x");
        float y = (float)textJSON.getJSONObject("position").getDouble("y");

        // todo - Parse the "style" value and use it to affect how text renders

        int alpha = textJSON.getInt("alpha");

        strokeCanvas.drawText(text, x, y, new Paint(Color.argb(alpha, 0,0,0)));
    }

    private Dimension parseJSONDimension(JSONObject jsonObject) throws JSONException{
        Dimension dimension = new Dimension();

        dimension.x = (float)jsonObject.getDouble("x");
        dimension.y = (float)jsonObject.getDouble("y");
        dimension.width = (float)jsonObject.getDouble("w");
        dimension.height = (float)jsonObject.getDouble("h");
        dimension.rotation = (float)jsonObject.getDouble("rotation");

        return dimension;
    }

    // Data structures to help when dealing with JSON data
    private class Stroke {
        public int bitmapID;
        public Dimension userDimensions;
        public Dimension actualDimensions;
    }

    private class Dimension {
        public float x, y, width, height, rotation;
    }
}