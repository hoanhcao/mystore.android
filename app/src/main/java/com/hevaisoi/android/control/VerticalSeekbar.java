package com.hevaisoi.android.control;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by ERP on 6/7/2017.
 */

public class VerticalSeekbar extends android.support.v7.widget.AppCompatSeekBar {

    public VerticalSeekbar(Context context) {
        super(context);
    }

    public VerticalSeekbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        float x = (getHeight() - event.getY()) * getWidth() / getHeight();
        float y = event.getX();
        MotionEvent verticalEvent = MotionEvent
                .obtain(event.getDownTime(), event.getEventTime(), event.getAction(), x, y,
                        event.getPressure(), event.getSize(), event.getMetaState(),
                        event.getYPrecision(), event.getXPrecision(), event.getDeviceId(),
                        event.getEdgeFlags());
        return super.onTouchEvent(verticalEvent);
    }

    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);
        super.onDraw(c);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
        updateThumb();
    }

    private void updateThumb() {
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }
}