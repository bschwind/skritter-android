package com.skritter.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.skritter.models.StrokeData;
import com.skritter.models.StudyItem;
import com.skritter.models.Vocab;

public abstract class StudyItemPanel {
    public interface IGradingButtonListener {
        public void onGradingButtonPressed(int gradingButton);
    }

    private IGradingButtonListener gradingButtonListener;

    public void setEventListener(IGradingButtonListener gradingButtonListener) {
        this.gradingButtonListener = gradingButtonListener;
    }

    private Resources resources;
    protected int customWidth, customHeight;

    protected StudyItem studyItem;
    protected Vocab vocab;
    protected StrokeData strokeData;
    protected Paint gridPaint, gradingNumberFontPaint, gradingTextFontPaint, backgroundPaint;

    protected boolean gradingButtonIsVisible;

    public StudyItemPanel() {
        gridPaint = new Paint();
        gridPaint.setAntiAlias(true);
        gridPaint.setColor(Color.BLACK);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2);

        gradingNumberFontPaint = new Paint(Color.BLACK);
        gradingNumberFontPaint.setAntiAlias(true);
        gradingNumberFontPaint.setTextAlign(Paint.Align.CENTER);

        gradingTextFontPaint = new Paint(Color.BLACK);
        gradingTextFontPaint.setAntiAlias(true);
        gradingTextFontPaint.setTextAlign(Paint.Align.CENTER);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
    }

    public void reset() {
        gradingButtonIsVisible = false;
    }

    public void loadAssets(Context context) {

    }

    public void onSizeChanged(int width, int height) {
        customWidth = width;
        customHeight = height;

        updateFontHeights();
    }

    protected void updateFontHeights() {
        gradingNumberFontPaint.setTextSize(customHeight * 0.0514f);
        gradingTextFontPaint.setTextSize(customHeight * 0.0257f);
    }

    public void draw(Canvas canvas) {
        internalDraw(canvas);

        if (gradingButtonIsVisible) {
            drawGradingButtons(canvas);
        }
    }

    public void onTouchDown(float x, float y) {
        internalOnTouchDown(x, y);
    }

    public void onTouchMove(float x, float y) {
        internalOnTouchMove(x, y);
    }

    public void onTouchUp(float x, float y) {
        if (gradingButtonIsVisible) {
            handleGradingButtonTap(x, y);
            return;
        }

        internalOnTouchUp(x, y);
    }

    protected abstract void internalDraw(Canvas canvas);
    protected abstract void internalOnTouchDown(float x, float y);
    protected abstract void internalOnTouchMove(float x, float y);
    protected abstract void internalOnTouchUp(float x, float y);

    public void makeGradingButtonsVisible() {
        gradingButtonIsVisible = true;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public StudyItem getStudyItem() {
        return studyItem;
    }

    public void setStudyItem(StudyItem studyItem) {
        this.studyItem = studyItem;
    }

    public Vocab getVocab() {
        return vocab;
    }

    public void setVocab(Vocab vocab) {
        this.vocab = vocab;
    }

    public StrokeData getStrokeData() {
        return strokeData;
    }

    public void setStrokeData(StrokeData strokeData) {
        this.strokeData = strokeData;
    }

    public int getCustomWidth() {
        return customWidth;
    }

    public void setCustomWidth(int customWidth) {
        this.customWidth = customWidth;
    }

    public int getCustomHeight() {
        return customHeight;
    }

    public void setCustomHeight(int customHeight) {
        this.customHeight = customHeight;
    }

    protected void drawTextCenteredOnPoint(String text, float x, float y, Canvas canvas, Paint fontPaint) {
        // characterFontPaint must have the text alignment set to CENTER
        float textHeight = fontPaint.getFontSpacing();

        y = y - fontPaint.ascent() - (textHeight / 2.0f);
        canvas.drawText(text, x, y, fontPaint);
    }

    protected void drawGrid(Canvas canvas) {
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

    protected void drawNonRuneBackground(Canvas canvas) {
        canvas.drawColor(backgroundPaint.getColor());

        // Draw a thin border
        canvas.drawLine(0, 0, customWidth, 0, gridPaint);
        canvas.drawLine(customWidth, 0, customWidth, customHeight, gridPaint);
        canvas.drawLine(customWidth, customHeight, 0, customHeight, gridPaint);
        canvas.drawLine(0, customHeight, 0, 0, gridPaint);
    }

    protected void drawGradingButtons(Canvas canvas) {
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

    private void handleGradingButtonTap(float x, float y) {
        float buttonHeightRatio = 0.12f;
        float buttonHeight = customHeight * buttonHeightRatio;
        float buttonWidth = customWidth / 4.0f;

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
}
