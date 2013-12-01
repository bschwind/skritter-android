package com.skritter.views;


import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.skritter.R;

public class ReadingItemPanel extends StudyItemPanel {
    private Paint characterFontPaint, definitionFontPaint, tapToShowFont;
    private boolean hasTapped;

    public ReadingItemPanel() {
        super();

        characterFontPaint = new Paint(Color.BLACK);
        characterFontPaint.setAntiAlias(true);
        characterFontPaint.setTextAlign(Paint.Align.CENTER);

        definitionFontPaint = new Paint(Color.BLACK);
        definitionFontPaint.setAntiAlias(true);
        definitionFontPaint.setTextAlign(Paint.Align.CENTER);

        tapToShowFont = new Paint(Color.BLACK);
        tapToShowFont.setAntiAlias(true);
        tapToShowFont.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void reset() {
        super.reset();

        hasTapped = false;
    }

    @Override
    protected void updateFontHeights() {
        super.updateFontHeights();

        characterFontPaint.setTextSize(customHeight * 0.214f);
        definitionFontPaint.setTextSize(customHeight * 0.0686f);
        tapToShowFont.setTextSize(customHeight * 0.04f);
    }

    @Override
    protected void internalDraw(Canvas canvas) {
        drawNonRuneBackground(canvas);

        Resources resources = getResources();
        String tapToShow = resources.getString(R.string.tapToShowReading);

        drawTextCenteredOnPoint(vocab.getWriting(), customWidth / 2.0f, 0.146f * customHeight, canvas, characterFontPaint);
        drawTextCenteredOnPoint(vocab.getDefinitionByLanguage("en"), customWidth / 2.0f, 0.420f * customHeight, canvas, definitionFontPaint);

        if (hasTapped) {
            drawTextCenteredOnPoint(vocab.getReading(), customWidth / 2.0f, 0.617f * customHeight, canvas, characterFontPaint);
        } else {
            drawTextCenteredOnPoint(tapToShow, customWidth / 2.0f, 0.637f * customHeight, canvas, tapToShowFont);
        }
    }

    @Override
    protected void internalOnTouchDown(float x, float y) {

    }

    @Override
    protected void internalOnTouchMove(float x, float y) {

    }

    @Override
    protected void internalOnTouchUp(float x, float y) {
        if (!hasTapped) {
            hasTapped = true;
            makeGradingButtonsVisible();
        }
    }
}
