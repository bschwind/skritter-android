package com.skritter.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ItemDetailsView extends View {
    
    private Paint textPaint;
    private Paint writingPaint;
    private Paint definitionPaint;
    public String sentence = "";
    public String itemWriting = "";
    public String definition = "";
    public float interpolation = 0.0f;

    public ItemDetailsView(Context context) {
        super(context);
        setDefaults();
    }
    public ItemDetailsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefaults();
    }
    public ItemDetailsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setDefaults();
    }

    private void setDefaults() {
        textPaint = new Paint(Color.BLACK);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        writingPaint = new Paint(Color.BLACK);
        writingPaint.setAntiAlias(true);
        writingPaint.setTextAlign(Paint.Align.CENTER);

        definitionPaint = new Paint(Color.BLACK);
        definitionPaint.setAntiAlias(true);
        definitionPaint.setTextAlign(Paint.Align.CENTER);
    }

    protected void onDraw(Canvas canvas) {
        textPaint.setTextSize(getWidth() * 0.214f);
        writingPaint.setTextSize(getWidth() * 0.18f);
        definitionPaint.setTextSize(getWidth() * 0.11f);
        
        canvas.drawColor(Color.argb(interpolate(0, 255, interpolation), interpolate(0, 255, interpolation), 0, 0));
        textPaint.setColor(Color.argb(interpolate(0, 255, interpolation), 0, 0, 0));
        
        drawScaledTextCenteredOnPoint(itemWriting, getWidth() / 2.0f, 140.0f, canvas, writingPaint);
        drawScaledTextCenteredOnPoint(definition, getWidth() / 2.0f, 350.0f, canvas, definitionPaint);
        drawScaledTextCenteredOnPoint(sentence, getWidth() / 2.0f, 500.0f, canvas, textPaint);
    }

    protected void drawScaledTextCenteredOnPoint(String text, float x, float y, Canvas canvas, Paint fontPaint) {
        float margin = 0.0482f * getWidth();

        // characterFontPaint must have the text alignment set to CENTER
        float textWidth = fontPaint.measureText(text);
        float textHeight = fontPaint.getFontSpacing();

        y = y - fontPaint.ascent() - (textHeight / 2.0f);

        float left = x - (textWidth / 2.0f);
        float right = x + (textWidth / 2.0f);

        float ratio = (getWidth() - (margin * 2.0f)) / (right - left);
        float newSize = fontPaint.getTextSize() * ratio;
        float originalSize = fontPaint.getTextSize();

        if (ratio < 1.0f) {
            // todo - When the newSize is below a certain size, we should chop
            //        the line in two and do some word wrapping
            fontPaint.setTextSize(newSize);
        }

        canvas.drawText(text, x, y, fontPaint);

        fontPaint.setTextSize(originalSize);
    }
    
    private int interpolate(float a, float b, float t) {
        return (int)(a + (b-a) * t);
    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        textPaint.setTextSize(width * 0.214f);
        writingPaint.setTextSize(width * 0.214f);
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
        
    }

    private void touchMove(float x, float y) {
        
    }

    private void touchUp(float x, float y) {
        
    }
}
