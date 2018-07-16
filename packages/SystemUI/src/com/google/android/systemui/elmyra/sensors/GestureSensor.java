package com.google.android.systemui.elmyra.sensors;

import java.util.Random;

public interface GestureSensor extends Sensor
{
    void setGestureListener(final Listener p0);

    interface Listener
    {
        void onGestureDetected(final GestureSensor p0);

        void onGestureProgress(final GestureSensor p0, final float p1, final int p2);

    }
}

