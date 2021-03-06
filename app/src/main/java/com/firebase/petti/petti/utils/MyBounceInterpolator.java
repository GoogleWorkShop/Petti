package com.firebase.petti.petti.utils;

import android.view.animation.Interpolator;

/**
 * Created by roy on 16/03/2017.
 */

public class MyBounceInterpolator implements Interpolator {

    double mAmplitude = 1;
    double mFrequency = 10;

    public MyBounceInterpolator(double amplitude, double frequency) {
        mAmplitude = amplitude;
        mFrequency = frequency;
    }

    public float getInterpolation(float time) {
        return (float) (-1 * Math.pow(Math.E, -time/ mAmplitude) *
                Math.cos(mFrequency * time) + 1);
    }
}
