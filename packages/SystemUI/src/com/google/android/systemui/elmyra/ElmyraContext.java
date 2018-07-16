package com.google.android.systemui.elmyra;

import android.content.Context;

public final class ElmyraContext
{
    private Context mContext;

    public ElmyraContext(final Context mContext) {
        this.mContext = mContext;
    }

    public boolean isAvailable() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.sensor.assist");
    }
}

