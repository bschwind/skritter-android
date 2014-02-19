package com.skritter.views;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;

import com.skritter.R;
import com.skritter.SkritterApplication;

public class DefinitionItemPanel extends StudyItemPanel {
    private Paint characterFontPaint, phoneticFontPaint, tapToShowFont;
    private boolean hasTapped;
    private boolean hideReading;

    public DefinitionItemPanel() {
        super();
        characterFontPaint = new Paint(Color.BLACK);
        characterFontPaint.setAntiAlias(true);
        characterFontPaint.setTextAlign(Paint.Align.CENTER);

        phoneticFontPaint = new Paint(Color.BLACK);
        phoneticFontPaint.setAntiAlias(true);
        phoneticFontPaint.setTextAlign(Paint.Align.CENTER);

        tapToShowFont = new Paint(Color.BLACK);
        tapToShowFont.setAntiAlias(true);
        tapToShowFont.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void loadAssets(Context context) {
        super.loadAssets(context);
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        hideReading = settings.getBoolean(SkritterApplication.PreferenceKeys.HIDE_READING, false);
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
        phoneticFontPaint.setTextSize(customHeight * 0.0628f);
        tapToShowFont.setTextSize(customHeight * 0.04f);
    }

    @Override
    protected void internalDraw(Canvas canvas) {
        drawNonRuneBackground(canvas);

        Resources resources = getResources();
        String tapToShow = resources.getString(R.string.tapToShowDefinition);

        drawScaledTextCenteredOnPoint(vocab.getWriting(), customWidth / 2.0f, 0.146f * customHeight, canvas, characterFontPaint);
        
        if (!hideReading || hasTapped) {
            drawScaledTextCenteredOnPoint(vocab.getReading(), customWidth / 2.0f, 0.411f * customHeight, canvas, phoneticFontPaint);
        }

        if (hasTapped) {
            drawScaledTextCenteredOnPoint(vocab.getDefinitionByLanguage("en"), customWidth / 2.0f, 0.551f * customHeight, canvas, phoneticFontPaint);
        } else {
            drawScaledTextCenteredOnPoint(tapToShow, customWidth / 2.0f, 0.637f * customHeight, canvas, tapToShowFont);
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
