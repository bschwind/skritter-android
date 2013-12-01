package com.skritter.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.skritter.models.StrokeData;
import com.skritter.models.StudyItem;
import com.skritter.models.Vocab;

import java.util.HashMap;
import java.util.Map;

public class PromptCanvas extends View {
    public interface IGradingButtonListener {
        public void onGradingButtonPressed(int gradingButton);
    }

    private IGradingButtonListener gradingButtonListener;

    public void setEventListener(IGradingButtonListener gradingButtonListener) {
        this.gradingButtonListener = gradingButtonListener;
    }

    // The different Paint styles for drawing the canvas elements
    private Paint statusBorderPaint;

    // The status border is colored depending on the grade of the study item
    private boolean shouldDrawStatusBorder = false;

    private int customWidth, customHeight;

    private Map<String, StudyItemPanel> studyItemPanelMap;
    private StudyItemPanel currentPanel;

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
        studyItemPanelMap = new HashMap<String, StudyItemPanel>();
        studyItemPanelMap.put("rune", new RuneItemPanel());
        studyItemPanelMap.put("rdng", new ReadingItemPanel());
        studyItemPanelMap.put("defn", new DefinitionItemPanel());

        for (StudyItemPanel panel : studyItemPanelMap.values()) {
            panel.setEventListener(new StudyItemPanel.IGradingButtonListener() {
                @Override
                public void onGradingButtonPressed(int gradingButton) {
                    onGrade(gradingButton);
                }
            });
        }

        statusBorderPaint = new Paint();
        statusBorderPaint.setAntiAlias(true);
        statusBorderPaint.setColor(Color.GREEN);
        statusBorderPaint.setStyle(Paint.Style.STROKE);
        statusBorderPaint.setStrokeWidth(6);
    }

    protected void onDraw(Canvas canvas) {
        if (currentPanel != null) {
            currentPanel.draw(canvas);
        }

        if (shouldDrawStatusBorder) {
            drawStatusBorder(canvas);
        }
    }

    private void drawStatusBorder(Canvas canvas) {
        canvas.drawLine(0, 0, customWidth, 0, statusBorderPaint);
        canvas.drawLine(customWidth, 0, customWidth, customHeight, statusBorderPaint);
        canvas.drawLine(customWidth, customHeight, 0, customHeight, statusBorderPaint);
        canvas.drawLine(0, customHeight, 0, 0, statusBorderPaint);
    }

    public void clearStrokes() {
        // The stroke bitmap is overlaid on the background, so set the whole image to
        // transparent to clear the strokes
        invalidate();
    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        customWidth = width;
        customHeight = height;

        for (StudyItemPanel panel : studyItemPanelMap.values()) {
            panel.onSizeChanged(customWidth, customHeight);
        }
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
        if (currentPanel != null) {
            currentPanel.onTouchDown(x, y);
        }
    }

    private void touchMove(float x, float y) {
        if (currentPanel != null) {
            currentPanel.onTouchMove(x, y);
        }
    }

    private void touchUp(float x, float y) {
        if (currentPanel != null) {
            currentPanel.onTouchUp(x, y);
        }
    }

    public void onGrade(int gradingButton) {
        if (gradingButtonListener != null) {
            gradingButtonListener.onGradingButtonPressed(gradingButton);
        }
    }

    public void setStatusBorderColor(int color) {
        statusBorderPaint.setColor(color);
    }

    public void setShouldDrawStatusBorder(boolean shouldDrawStatusBorder) {
        this.shouldDrawStatusBorder = shouldDrawStatusBorder;
    }

    public void setStudyItemAndVocab(StudyItem studyItem, Vocab vocab, StrokeData strokeData) {
        this.currentPanel = studyItemPanelMap.get(studyItem.getPart());
        this.currentPanel.setResources(getContext().getResources());

        currentPanel.reset();
        currentPanel.setStudyItem(studyItem);
        currentPanel.setVocab(vocab == null ? new Vocab() : vocab);
        currentPanel.setStrokeData(strokeData);
        currentPanel.loadAssets(getContext());

        invalidate();
    }
}
