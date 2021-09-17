package com.herewhite.demo.test.window;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.herewhite.demo.R;

import cn.iwgang.countdownview.CountdownView;

public class DragViewPlugin extends FrameLayout {

    private View root;
    private FrameLayout dragView;
    private CountdownView countdownView;

    private Listener listener;
    private DragViewState state = new DragViewState();
    private float moveX;
    private float moveY;
    private boolean dragging = false;

    public DragViewPlugin(Context context) {
        super(context);
        init(context);
    }

    public DragViewPlugin(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DragViewPlugin(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        root = LayoutInflater.from(context).inflate(R.layout.layout_drag_view, this, true);
        dragView = root.findViewById(R.id.dragView);
        countdownView = root.findViewById(R.id.countdownView);

        dragView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    moveX = event.getX();
                    moveY = event.getY();
                    dragging = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    dragView.setTranslationX(dragView.getX() + (event.getX() - moveX));
                    dragView.setTranslationY(dragView.getY() + (event.getY() - moveY));

                    if (listener != null) {
                        state.offX = Float.valueOf(dragView.getX() + (event.getX() - moveX)) / getWidth();
                        state.offY = Float.valueOf(dragView.getY() + (event.getY() - moveY)) / getHeight();
                        listener.onChange(state);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (listener != null) {
                        state.offX = Float.valueOf(dragView.getX() + (event.getX() - moveX)) / getWidth();
                        state.offY = Float.valueOf(dragView.getY() + (event.getY() - moveY)) / getHeight();
                        listener.onChange(state);
                    }
                    dragging = false;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
            }

            return true;
        });

        countdownView.setOnCountdownIntervalListener(1000, (cv, remainTime) -> {
            state.leftTime = remainTime;
            if (listener != null) {
                listener.onChange(state);
            }
        });
        countdownView.setOnCountdownEndListener(cv -> {
            state.leftTime = 0;
            if (listener != null) {
                listener.onChange(state);
            }
        });
    }

    public void startTimer(long ms) {
        countdownView.start(ms);
    }

    public void updateState(DragViewState newState) {
        if (dragging) {
            return;
        }
        dragView.setTranslationX(newState.offX * getWidth());
        dragView.setTranslationY(newState.offY * getHeight());

        if (newState.w != state.w || newState.h != state.h) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) dragView.getLayoutParams();
            layoutParams.width = (int) (newState.w * getWidth());
            layoutParams.height = (int) (newState.h * getHeight());
            dragView.setLayoutParams(layoutParams);
        }
        countdownView.updateShow(newState.leftTime);
        state = newState;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        void onChange(DragViewState state);
    }
}