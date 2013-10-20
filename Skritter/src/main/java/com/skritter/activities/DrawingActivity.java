package com.skritter.activities;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;

import com.skritter.views.PromptCanvas;
import com.skritter.R;

public class DrawingActivity extends Activity {
    private PromptCanvas canvas;
    private WebView web;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get rid of the app title element
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set our layout
        setContentView(R.layout.activity_drawing);

        canvas = (PromptCanvas) findViewById(R.id.canvas);
        canvas.setBackgroundColor(Color.LTGRAY);
        canvas.setPenColor(Color.BLACK);
        canvas.setGridColor(Color.GRAY);
    }

    public void clearStrokes(View view) {
        canvas.setShouldDrawStatusBorder(true);
        canvas.setStatusBorderColor(Color.RED);
        canvas.clearStrokes();
    }

}
