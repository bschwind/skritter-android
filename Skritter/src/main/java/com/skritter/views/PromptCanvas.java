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
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.skritter.R;
import com.skritter.models.LoginStatus;
import com.skritter.models.StudyItem;
import com.skritter.models.Vocab;

public class PromptCanvas extends View {
    public interface IGradingButtonListener {
        public void onGradingButtonPressed(int gradingButton);
    }

    private IGradingButtonListener gradingButtonListener;

    public void setEventListener(IGradingButtonListener gradingButtonListener) {
        this.gradingButtonListener = gradingButtonListener;
    }

    // Bitmap and Canvas for drawing the background grid
    private Bitmap backgroundBitmap;
    private Canvas backgroundCanvas;

    // Bitmap and Canvas for drawing strokes
    private Bitmap strokeBitmap;
    private Canvas strokeCanvas;

    // The different Paint styles for drawing the canvas elements
    private Paint strokePaint, backgroundPaint, gridPaint, statusBorderPaint, fontPaint, gradingNumberFontPaint, gradingTextFontPaint;

    // The status border is red or green, depending on whether or not the user got the correct stroke
    private boolean shouldDrawStatusBorder = false;

    private StudyItem studyItem;
    private Vocab vocab;

    private int customWidth, customHeight;

    // Rune variables
    // The Path object used to draw the user's strokes
    private Path drawPath;

    // Keep track of the last point the user has drawn so we can use smooth quad curves
    private PointF lastPoint = new PointF(-1.0f, -1.0f);

    private boolean currentlyDrawing = false;


    // Reading Variables
    private boolean hasTapped = false;


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
        fontPaint.setAntiAlias(true);
        fontPaint.setTextAlign(Paint.Align.CENTER);

        gradingNumberFontPaint = new Paint(Color.BLACK);
        gradingNumberFontPaint.setAntiAlias(true);
        gradingNumberFontPaint.setTextAlign(Paint.Align.CENTER);

        gradingTextFontPaint = new Paint(Color.BLACK);
        gradingTextFontPaint.setAntiAlias(true);
        gradingTextFontPaint.setTextAlign(Paint.Align.CENTER);

        setFontHeights(customWidth, customHeight);
    }

    private void setFontHeights(int width, int height) {
        fontPaint.setTextSize(height * 0.114f);
        gradingNumberFontPaint.setTextSize(height * 0.0514f);
        gradingTextFontPaint.setTextSize(height * 0.0257f);
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

        drawTextCenteredOnPoint(vocab.getWriting(), customWidth / 2.0f, 0.146f * customHeight, canvas, fontPaint);
        drawTextCenteredOnPoint(vocab.getDefinitionByLanguage("en"), customWidth / 2.0f, 0.420f * customHeight, canvas, fontPaint);

        if (hasTapped) {
            drawTextCenteredOnPoint(vocab.getReading(), customWidth / 2.0f, 0.617f * customHeight, canvas, fontPaint);
            drawGradingButtons(canvas);
        } else {
            drawTextCenteredOnPoint(tapToShow, customWidth / 2.0f, 0.617f * customHeight, canvas, fontPaint);
        }
    }

    private void drawDefinitionCanvas(Canvas canvas) {
        drawNonRuneBackground(canvas);

        Resources resources = getResources();
        String tapToShow = resources.getString(R.string.tapToShowDefinition);

        drawTextCenteredOnPoint(vocab.getWriting(), customWidth / 2.0f, 0.11f * customHeight, canvas, fontPaint);
        drawTextCenteredOnPoint(vocab.getReading(), customWidth / 2.0f, 0.411f * customHeight, canvas, fontPaint);

        if (hasTapped) {
            drawTextCenteredOnPoint(vocab.getDefinitionByLanguage("en"), customWidth / 2.0f, 0.551f * customHeight, canvas, fontPaint);
            drawGradingButtons(canvas);
        } else {
            drawTextCenteredOnPoint(tapToShow, customWidth / 2.0f, 0.514f * customHeight, canvas, fontPaint);
        }
    }

    private void drawToneCanvas(Canvas canvas) {
        drawNonRuneBackground(canvas);
        drawTextCenteredOnPoint("tone", customWidth / 2.0f, 200, canvas, fontPaint);
    }

    private void drawTextCenteredOnPoint(String text, float x, float y, Canvas canvas, Paint fontPaint) {
        // fontPaint must have the text alignment set to CENTER
        float textHeight = fontPaint.getFontSpacing();

        y = y - fontPaint.ascent() - (textHeight / 2.0f);
        canvas.drawText(text, x, y, fontPaint);
    }

    private void drawStatusBorder(Canvas canvas) {
        canvas.drawLine(0, 0, customWidth, 0, statusBorderPaint);
        canvas.drawLine(customWidth, 0, customWidth, customHeight, statusBorderPaint);
        canvas.drawLine(customWidth, customHeight, 0, customHeight, statusBorderPaint);
        canvas.drawLine(0, customHeight, 0, 0, statusBorderPaint);
    }

    private void drawRuneBackground(Canvas canvas) {
        // This is only called when the background bitmap is resized in some way
        // Generates a solid colored background with the Skritter grid on top
        canvas.drawColor(backgroundPaint.getColor());
        drawGrid(canvas);
    }

    private void drawGrid(Canvas canvas) {
        // Draw intersecting lines
        canvas.drawLine(0, 0, customWidth, customHeight, gridPaint);
        canvas.drawLine(customWidth, 0, 0, customHeight, gridPaint);
        canvas.drawLine(customWidth * 0.5f, 0, customWidth * 0.5f, customHeight, gridPaint);
        canvas.drawLine(0, customHeight * 0.5f, customWidth, customHeight * 0.5f, gridPaint);

        // Draw a thin border
        canvas.drawLine(0, 0, customWidth, 0, gridPaint);
        canvas.drawLine(customWidth, 0, customWidth, customHeight, gridPaint);
        canvas.drawLine(customWidth, customHeight, 0, customHeight, gridPaint);
        canvas.drawLine(0, customHeight, 0, 0, gridPaint);
    }

    private void drawNonRuneBackground(Canvas canvas) {
        canvas.drawColor(backgroundPaint.getColor());

        // Draw a thin border
        canvas.drawLine(0, 0, customWidth, 0, gridPaint);
        canvas.drawLine(customWidth, 0, customWidth, customHeight, gridPaint);
        canvas.drawLine(customWidth, customHeight, 0, customHeight, gridPaint);
        canvas.drawLine(0, customHeight, 0, 0, gridPaint);
    }

    private void drawGradingButtons(Canvas canvas) {
        float buttonHeightRatio = 0.12f;
        float buttonHeight = customHeight * buttonHeightRatio;
        float buttonWidth = customWidth / 4.0f;
        float halfButtonWidth = buttonWidth / 2.0f;

        String[] gradingTexts = new String[] { "forgot", "so-so", "got it", "too easy" };

        for (int i = 0; i < 4; i++) {
            drawTextCenteredOnPoint("" + (i+1), (i * buttonWidth) + halfButtonWidth, customHeight - (buttonHeight * 0.75f), canvas, gradingNumberFontPaint);
            drawTextCenteredOnPoint(gradingTexts[i], (i * buttonWidth) + halfButtonWidth, customHeight - (buttonHeight * 0.25f), canvas, gradingTextFontPaint);
            canvas.drawRect((i * buttonWidth), customHeight - buttonHeight, ((i+1) * buttonWidth), customHeight, gridPaint);
        }
    }

    public void clearStrokes() {
        // The stroke bitmap is overlaid on the background, so set the whole image to
        // transparent to clear the strokes
        strokeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        customWidth = width;
        customHeight = height;

        // todo - Call bitmap.recycle() on these bitmaps...if that actually helps clear resources

        backgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        backgroundCanvas = new Canvas(backgroundBitmap);
        drawRuneBackground(backgroundCanvas);

        strokeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        strokeCanvas = new Canvas(strokeBitmap);

        setFontHeights(customWidth, customHeight);
    }

    // This is experimental. Currently it is used to enforce a square canvas every time we resize.
    // We'll probably want to consider other ways of doing this, as it could end up making a square
    // larger than we want.
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        // set the dimensions
        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
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
        if (studyItem == null) {
            // Do nothing?
        } else if (studyItem.isRune()) {
            runeTouchDown(x, y);
        } else if (studyItem.isReading()) {
            readingTouchDown(x, y);
        } else if (studyItem.isDefinition()) {
            definitionTouchDown(x, y);
        } else if (studyItem.isTone()) {
            toneTouchDown(x, y);
        } else {
            // Shouldn't be here
        }
    }

    private void runeTouchDown(float x, float y) {
        drawPath.moveTo(x, y);

        lastPoint.x = x;
        lastPoint.y = y;

        currentlyDrawing = true;
    }

    private void readingTouchDown(float x, float y) {

    }

    private void definitionTouchDown(float x, float y) {

    }

    private void toneTouchDown(float x, float y) {

    }

    private void touchMove(float x, float y) {
        if (studyItem == null) {
            // Do nothing?
        } else if (studyItem.isRune()) {
            runeTouchMove(x, y);
        } else if (studyItem.isReading()) {
            readingTouchMove(x, y);
        } else if (studyItem.isDefinition()) {
            definitionTouchMove(x, y);
        } else if (studyItem.isTone()) {
            toneTouchMove(x, y);
        } else {
            // Shouldn't be here
        }
    }

    private void runeTouchMove(float x, float y) {
        drawPath.quadTo(lastPoint.x, lastPoint.y, (x + lastPoint.x) * 0.5f, (y + lastPoint.y) * 0.5f);
        strokeCanvas.drawPath(drawPath, strokePaint);

        lastPoint.x = x;
        lastPoint.y = y;
    }

    private void readingTouchMove(float x, float y) {

    }

    private void definitionTouchMove(float x, float y) {

    }

    private void toneTouchMove(float x, float y) {

    }

    private void touchUp(float x, float y) {
        if (studyItem == null) {
            // Do nothing?
        } else if (studyItem.isRune()) {
            runeTouchUp(x, y);
        } else if (studyItem.isReading()) {
            readingTouchUp(x, y);
        } else if (studyItem.isDefinition()) {
            definitionTouchUp(x, y);
        } else if (studyItem.isTone()) {
            toneTouchUp(x, y);
        } else {
            // Shouldn't be here
        }
    }

    private void runeTouchUp(float x, float y) {
        drawPath.lineTo(x, y);
        drawPath.reset();

        currentlyDrawing = false;
    }

    private void readingTouchUp(float x, float y) {
        if (!hasTapped) {
            hasTapped = true;
            return;
        }

        handleGradingButtonTap(x, y);
    }

    private void definitionTouchUp(float x, float y) {
        if (!hasTapped) {
            hasTapped = true;
            return;
        }

        handleGradingButtonTap(x, y);
    }

    private void toneTouchUp(float x, float y) {

    }

    private void handleGradingButtonTap(float x, float y) {
        float buttonHeightRatio = 0.12f;
        float buttonHeight = customHeight * buttonHeightRatio;
        float buttonWidth = customWidth / 4.0f;
        float halfButtonWidth = buttonWidth / 2.0f;

        String[] gradingTexts = new String[] { "forgot", "so-so", "got it", "too easy" };

        for (int i = 0; i < 4; i++) {
            Rect rect = new Rect((int)(i * buttonWidth), (int)(customHeight - buttonHeight), (int)((i+1) * buttonWidth), customHeight);

            if (rect.contains((int)x, (int)y)) {
                if (gradingButtonListener != null) {
                    gradingButtonListener.onGradingButtonPressed(i);
                }

                break;
            }
        }
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

        hasTapped = false;

        invalidate();
    }
}