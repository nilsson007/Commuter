package com.development.android.commuter;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ResizeAnimation extends Animation {

    private final int targetHeight;
    private ListView view;
    private int startHeight;

    ResizeAnimation(ListView view, int targetHeight, int startHeight) {
        this.view = view;
        this.targetHeight = targetHeight;
        this.startHeight = startHeight;
        this.setDuration(100);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        //int newHeight = (int) (startHeight + targetHeight * interpolatedTime);
        //to support decent animation, change new heigt as Nico S. recommended in comments
        //int newHeight = (int) (startHeight+(targetHeight - startHeight) * interpolatedTime);
        view.getLayoutParams().height = (int) (startHeight+(targetHeight - startHeight) * interpolatedTime);
        view.requestLayout();
        if (view.getLayoutParams().height == 0 && interpolatedTime != 0) {
            view.setAdapter(null);
        }
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}