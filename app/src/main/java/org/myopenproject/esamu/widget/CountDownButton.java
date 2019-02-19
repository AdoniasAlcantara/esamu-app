package org.myopenproject.esamu.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.TypedValue;

import org.myopenproject.esamu.R;

@SuppressLint("SetTextI18n")
public class CountDownButton extends android.support.v7.widget.AppCompatButton {
    private CountDownTimer timer;
    private int count;
    private int secondsRemaining;
    private boolean isCounting;
    private float textScaleFactor;
    private float startTextSize;
    private String startText;
    private CountDownListeners listeners;

    public CountDownButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CountDownButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray params = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.CountDownButton, 0, 0);

        try {
            setCount(params.getInteger(R.styleable.CountDownButton_count, 1));
            setTextScaleFactor(params.getFloat(R.styleable.CountDownButton_textScaleFactor, 1.0f));
        } finally {
            params.recycle();
        }

        startText = getText().toString();
        startTextSize = getTextSize();
        listeners = new CountDownListeners() {};
    }

    @Override
    public boolean performClick() {
        if (secondsRemaining < 0)
            return super.performClick();

        if (!isCounting)
           startCount();

        return false;
    }

    public void setCount(final int seconds) {
        this.secondsRemaining = count = seconds;
        timer = new CountDownTimer(seconds * 1000, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (secondsRemaining * 1000 >= millisUntilFinished) {
                    listeners.onTick(secondsRemaining);
                    setText(Integer.toString(secondsRemaining--));
                }
            }

            @Override
            public void onFinish() {
                listeners.onFinish();
                setText(Integer.toString(secondsRemaining--));
                stopCount();
                performClick();
            }
        };
    }

    public void setTextScaleFactor(float factor) {
        textScaleFactor = factor;
    }

    public void setCountDownListeners(CountDownListeners listeners) {
        this.listeners = listeners;
    }

    public boolean isCounting() {
        return isCounting;
    }

    public void reset() {
        if (isCounting)
            stopCount();

        secondsRemaining = count;
        setTextSize(TypedValue.COMPLEX_UNIT_PX, startTextSize);
        setText(startText);
    }

    private void startCount() {
        isCounting = true;
        setTextSize(TypedValue.COMPLEX_UNIT_PX, startTextSize * textScaleFactor);
        setText(Integer.toString(secondsRemaining));
        timer.start();
        listeners.onStartCount();
    }

    private void stopCount() {
        isCounting = false;
        timer.cancel();
        listeners.onStopCount(secondsRemaining + 1);
    }

    public interface CountDownListeners {
        default void onStartCount() {}
        default void onStopCount(int count) {}
        default void onTick(int count) {}
        default void onFinish() {}
    }
}
