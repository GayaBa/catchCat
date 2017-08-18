package com.company.circlethecat;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class MainActivity extends Activity implements View.OnTouchListener {
    Board mBoard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);

        super.onCreate(savedInstanceState);

        int BoardSize = getIntent().getExtras().getInt("BoardSize");
        mBoard = new Board(this, BitmapFactory.decodeResource(getResources(), R.drawable.cat_sprite), BoardSize);
        mBoard.setOnTouchListener(this);

        RelativeLayout layout = new RelativeLayout(this);
        layout.addView(mBoard);
        setContentView(layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBoard.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBoard.resume();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mBoard.addTouchEvent(event.getX(), event.getY());
        return true;
    }
}
