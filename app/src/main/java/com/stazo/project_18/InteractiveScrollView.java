package com.stazo.project_18;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by isaacwang on 6/25/16.
 */

public class InteractiveScrollView extends ScrollView {
    OnBottomReachedListener mListener;
    private boolean isReady = true;

    public InteractiveScrollView(Context context, AttributeSet attrs,
                                 int defStyle) {
        super(context, attrs, defStyle);
    }

    public InteractiveScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InteractiveScrollView(Context context) {
        super(context);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        View view = (View) getChildAt(getChildCount()-1);
        int diff = (view.getBottom()-(getHeight()+getScrollY()));

        if (diff <= 0 && mListener != null && isReady) {
            isReady = false;
            mListener.onBottomReached();
        }

        super.onScrollChanged(l, t, oldl, oldt);
    }


    // Getters & Setters

    public OnBottomReachedListener getOnBottomReachedListener() {
        return mListener;
    }

    public void setOnBottomReachedListener(
            OnBottomReachedListener onBottomReachedListener) {
        mListener = onBottomReachedListener;
    }


    /**
     * Event listener.
     */
    public interface OnBottomReachedListener{
        public void onBottomReached();
    }

    public void ready() {
        isReady = true;
    }

}

