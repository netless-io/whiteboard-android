package com.herewhite.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatTextView;

public class DragView extends AppCompatTextView {

    private float moveX;
    private float moveY;
    private Listener listener;
    private boolean dragging = false;

    public DragView(Context context) {
        super(context);
    }

    public DragView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                moveX = event.getX();
                moveY = event.getY();
                dragging = true;
                break;
            case MotionEvent.ACTION_MOVE:
                setTranslationX(getX() + (event.getX() - moveX));
                setTranslationY(getY() + (event.getY() - moveY));
                if (listener != null) {
                    listener.onChange(getX() + (event.getX() - moveX), getY() + (event.getY() - moveY));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (listener != null) {
                    listener.onChange(getX() + (event.getX() - moveX), getY() + (event.getY() - moveY));
                }
                dragging = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return true;
    }

    public void setTranslation(float x, float y) {
        setTranslationX(x);
        setTranslationY(y);
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        void onChange(float x, float y);
    }
}