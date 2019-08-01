package org.myopenproject.esamu.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class LockableViewPager extends ViewPager {
    private boolean enabled;

    public LockableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        enabled = true;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if (enabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setSwipeEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}