package com.development.android.commuter;

import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;
import android.content.Context;
import android.view.animation.ScaleAnimation;

public class MyFrameLayout extends FrameLayout {

    private double pivotX;
    private double pivotY;
    public boolean closing;


    public MyFrameLayout(Context context, double x, double y){
        super(context);
        pivotX = x;
        pivotY = y;

    }
    public MyFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ScaleAnimation scaleAnimation = new ScaleAnimation(0,1,0,1, ScaleAnimation.RELATIVE_TO_SELF, (float)pivotX, ScaleAnimation.ABSOLUTE, (float)pivotY);
        scaleAnimation.setDuration(200);
        scaleAnimation.setFillAfter(true);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        this.startAnimation(animationSet);
    }

    void closeViewAnimation(){
        closing = true;
        ScaleAnimation scaleAnimation = new ScaleAnimation(1,0,1,0, ScaleAnimation.RELATIVE_TO_SELF, (float)pivotX, ScaleAnimation.ABSOLUTE, (float)pivotY);
        scaleAnimation.setDuration(200);
        scaleAnimation.setFillAfter(true);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        this.startAnimation(animationSet);
        setVisibility(View.INVISIBLE);
    }
}
