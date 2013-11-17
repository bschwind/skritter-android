package com.skritter.views;

import android.content.Context;
import android.content.res.Resources;
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

import com.skritter.R;
import com.skritter.models.StudyItem;
import com.skritter.models.Vocab;

public class PromptCanvas extends View {
    // Bitmap and Canvas for drawing the background grid
    private Bitmap backgroundBitmap;
    private Canvas backgroundCanvas;

    // Bitmap and Canvas for drawing strokes
    private Bitmap strokeBitmap;
    private Canvas strokeCanvas;

    // The different Paint styles for drawing the canvas elements
    private Paint strokePaint, backgroundPaint, gridPaint, statusBorderPaint, fontPaint;

    // The status border is red or green, depending on whether or not the user got the correct stroke
    private boolean shouldDrawStatusBorder = false;

    // The Path object used to draw the user's strokes
    private Path drawPath;

    // Keep track of the last point the user has drawn so we can use smooth quad curves
    private PointF lastPoint = new PointF(-1.0f, -1.0f);

    private boolean currentlyDrawing = false;

    private StudyItem studyItem;
    private Vocab vocab;

    public PromptCanvas(Context context) {
        super(context);
        setDefaults();
    }
    public PromptCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefaults();
    }
    public PromptCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDefaults();
    }

    private void setDefaults() {
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

        backgroundPaint = new Paint(Color.WHITE);

        fontPaint = new Paint(Color.BLACK);
        fontPaint.setTextSize(80);
        fontPaint.setAntiAlias(true);
        fontPaint.setTextAlign(Paint.Align.CENTER);
    }

    protected void onDraw(Canvas canvas) {
        if (studyItem == null) {
            drawDefault(canvas);
        } else if (studyItem.isRune()) {
            drawRuneCanvas(canvas);
        } else if (studyItem.isReading()) {
            drawReadingCanvas(canvas);
        } else if (studyItem.isDefinition()) {
            drawDefinitionCanvas(canvas);
        } else if (studyItem.isTone()) {
            drawToneCanvas(canvas);
        } else {
            // This study item doesn't have a part, we shouldn't be here...
        }

        if (shouldDrawStatusBorder) {
            drawStatusBorder(canvas);
        }
    }

    private void drawDefault(Canvas canvas) {
        drawRuneBackground(canvas);
    }

    private void drawRuneCanvas(Canvas canvas) {
        drawRuneBackground(canvas);
        canvas.drawBitmap(strokeBitmap, 0, 0, null);
    }

    private void drawReadingCanvas(Canvas canvas) {
        drawNonRuneBackground(canvas);

        Resources resources = getResources();
        String tapToShow = resources.getString(R.string.tapToShowReading);

        drawTextCenteredOnPoint(vocab.getWriting(), getWidth() / 2.0f, 100, canvas, fontPaint);

        boolean showReading = false;

        if (showReading) {
            drawTextCenteredOnPoint(vocab.getReading(), getWidth() / 2.0f, 400, canvas, fontPaint);
        } else {
            drawTextCenteredOnPoint(tapToShow, getWidth() / 2.0f, 400, canvas, fontPaint);
        }
    }

    private void drawDefinitionCanvas(Canvas canvas) {
        drawNonRuneBackground(canvas);

        Resources resources = getResources();
        String tapToShow = resources.getString(R.string.tapToShowDefinition);

        drawTextCenteredOnPoint(vocab.getWriting(), getWidth() / 2.0f, 100, canvas, fontPaint);
        drawTextCenteredOnPoint(vocab.getReading(), getWidth() / 2.0f, 200, canvas, fontPaint);

        boolean showDefinition = false;

        if (showDefinition) {
            drawTextCenteredOnPoint(vocab.getDefinitionByLanguage("en"), getWidth() / 2.0f, 400, canvas, fontPaint);
        } else {
            drawTextCenteredOnPoint(tapToShow, getWidth() / 2.0f, 400, canvas, fontPaint);
        }
    }

    private void drawToneCanvas(Canvas canvas) {
        drawNonRuneBackground(canvas);
        drawTextCenteredOnPoint("tone", getWidth() / 2.0f, 200, canvas, fontPaint);
    }

    private void drawTextCenteredOnPoint(String text, float x, float y, Canvas canvas, Paint fontPaint) {
        // fontPaint must have the text alignment set to CENTER
        float textHeight = fontPaint.getFontSpacing();

        y = y - fontPaint.ascent() - (textHeight / 2.0f);
        canvas.drawText(text, x, y, fontPaint);
    }

    private void drawStatusBorder(Canvas canvas) {
        canvas.drawLine(0, 0, canvas.getWidth(), 0, statusBorderPaint);
        canvas.drawLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight(), statusBorderPaint);
        canvas.drawLine(canvas.getWidth(), canvas.getHeight(), 0, canvas.getHeight(), statusBorderPaint);
        canvas.drawLine(0, canvas.getHeight(), 0, 0, statusBorderPaint);
    }

    private void drawRuneBackground(Canvas canvas) {
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

    private void drawNonRuneBackground(Canvas canvas) {
        canvas.drawColor(backgroundPaint.getColor());

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
        drawRuneBackground(backgroundCanvas);

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
        drawPath.moveTo(x, y);

        lastPoint.x = x;
        lastPoint.y = y;

        currentlyDrawing = true;
    }

    private void touchMove(float x, float y) {
        drawPath.quadTo(lastPoint.x, lastPoint.y, (x + lastPoint.x) * 0.5f, (y + lastPoint.y) * 0.5f);
        strokeCanvas.drawPath(drawPath, strokePaint);

        lastPoint.x = x;
        lastPoint.y = y;
    }

    private float lerp(float start, float end, float amount) {
        return start + (end - start) * amount;
    }

    private void touchUp(float x, float y) {
        drawPath.lineTo(x, y);
        drawPath.reset();

        currentlyDrawing = false;
    }

    public boolean isCurrentlyDrawing() {
        return currentlyDrawing;
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

    public void setStudyItemAndVocab(StudyItem studyItem, Vocab vocab) {
        this.studyItem = studyItem;
        this.vocab = vocab;
        invalidate();
    }
}