package com.google.android.systemui.elmyra.gates;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;

public abstract class Gate {
    private final Context mContext;
    private final Handler mNotifyHandler;
    private boolean mActive;
    @Nullable
    private Listener mListener;

    public Gate(final Context mContext) {
        this.mContext = mContext;
        this.mNotifyHandler = new Handler(mContext.getMainLooper());
        this.mActive = false;
    }

    public void activate() {
        if (!this.isActive()) {
            this.mActive = true;
            this.onActivate();
        }
    }

    public void deactivate() {
        if (this.isActive()) {
            this.mActive = false;
            this.onDeactivate();
        }
    }

    protected Context getContext() {
        return this.mContext;
    }

    public final boolean isActive() {
        return this.mActive;
    }

    protected abstract boolean isBlocked();

    public final boolean isBlocking() {
        return this.isActive() && this.isBlocked();
    }

    protected void notifyListener() {
        if (this.isActive() && this.mListener != null) {
            this.mNotifyHandler.post(this::notifyListener);
        }
    }

    protected abstract void onActivate();

    protected abstract void onDeactivate();

    public void setListener(@Nullable final Listener mListener) {
        this.mListener = mListener;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public interface Listener {
        void onGateChanged(final Gate p0);
    }
}

