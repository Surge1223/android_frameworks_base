package com.google.android.systemui.elmyra.actions;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

import com.google.android.systemui.elmyra.IElmyraService;
import com.google.android.systemui.elmyra.IElmyraServiceListener;
import com.google.android.systemui.elmyra.IElmyraServiceSettingsListener;

import java.util.NoSuchElementException;

public class ServiceAction extends Action implements IBinder.DeathRecipient {
    protected static IElmyraServiceSettingsListener mElmyraServiceSettingsListener;
    protected static LaunchOpa mLaunchOpa;
    private static IElmyraService mElmyraService;
    private static ElmyraServiceListener mElmyraServiceListener;
    private static IBinder mToken;
    private ElmyraServiceConnection mElmyraServiceConnection;

    public ServiceAction(Context context, LaunchOpa mLaunchOpa) {
        super(context, null);
        mToken = new Binder();
        mElmyraServiceConnection = new ElmyraServiceConnection();
        mElmyraServiceListener = new ElmyraServiceListener();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.systemui", "com.google.android.systemui.elmyra.ElmyraServiceProxy"));
        context.startService(intent);
        bindToElmyraServiceProxy();
    }

    private void bindToElmyraServiceProxy() {
        if (mElmyraService != null) {
            Log.d("Elmyra/ServiceAction", "ElmyraService connected to ElmyraServiceProxy");
            return;
        }
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.systemui", "com.google.android.systemui.elmyra.ElmyraServiceProxy"));
            getContext().getApplicationContext().bindServiceAsUser(intent, mElmyraServiceConnection, Context.BIND_AUTO_CREATE,
                    UserHandle.getUserHandleForUid(1000));
        } catch (SecurityException ex) {
            Log.e("Elmyra/ServiceAction", "Unable to bind to ElmyraServiceProxy", (Throwable) ex);
        }
    }

    public void binderDied() {
        Log.w("Elmyra/ServiceAction", "Binder died");
        mElmyraServiceSettingsListener = null;
        notifyListener();
    }

    @Override
    public boolean isAvailable() {
        return mElmyraServiceSettingsListener != null;
    }

    @Override
    public void onProgress(float n, int n2) {
        updateFeedbackEffects(n, n2);
        if (mElmyraServiceSettingsListener == null) {
            return;
        }
        try {
            mElmyraServiceSettingsListener.onGestureProgress(n, n2);
        } catch (RemoteException ex) {
            Log.e("Elmyra/ServiceAction", "Unable to send progress, setting listener to null", (Throwable) ex);
            mElmyraServiceSettingsListener = null;
            notifyListener();
        }
    }

    @Override
    public void onTrigger() {
        triggerFeedbackEffects();
        if (mElmyraServiceSettingsListener == null) {
            return;
        }
        try {
            mElmyraServiceSettingsListener.onGestureDetected();
        } catch (DeadObjectException ex2) {
            Log.e("Elmyra/ServiceAction", "Settings crashed or closed without unregistering, setting listener to null", (Throwable) ex2);
            mElmyraServiceSettingsListener = null;
            notifyListener();
        } catch (RemoteException ex) {
            Log.e("Elmyra/ServiceAction", "Unable to send onGestureDetected, setting listener to null", (Throwable) ex);
            mElmyraServiceSettingsListener = null;
            notifyListener();
        }
    }

    @Override
    protected void triggerFeedbackEffects() {
        super.triggerFeedbackEffects();
        mLaunchOpa.triggerFeedbackEffects();
    }

    @Override
    protected void updateFeedbackEffects(float n, int n2) {
        super.updateFeedbackEffects(n, n2);
        mLaunchOpa.updateFeedbackEffects(n, n2);
    }

    private class ElmyraServiceConnection implements ServiceConnection {

        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mElmyraService = IElmyraService.Stub.asInterface(binder);
            try {
                mElmyraService.registerListener(mToken, mElmyraServiceListener);
            } catch (RemoteException ex) {
                Log.e("Elmyra/ServiceAction", "Error registering listener", (Throwable) ex);
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            mElmyraService = null;
        }
    }

    class ElmyraServiceListener extends IElmyraServiceListener.Stub {
        public ElmyraServiceListener() {

        }

        public void launchAssistant() {
            if (mLaunchOpa.isAvailable()) {
                mLaunchOpa.launchOpa();
            }
        }

        public void setListener(IBinder binder, IBinder binder2) {
            if (binder2 == null && mElmyraServiceSettingsListener == null) {
                return;
            }
            IElmyraServiceSettingsListener interface1 = IElmyraServiceSettingsListener.Stub.asInterface(binder2);
            if (interface1 != mElmyraServiceSettingsListener) {
                mElmyraServiceSettingsListener = interface1;
                notifyListener();
            }
            if (binder == null) {
                return;
            }
                while (true) {
                    try {
                        binder.unlinkToDeath((DeathRecipient) this, 0);
                        return;
                    } catch (NoSuchElementException ignored) {
                    }
                }
            }
        }
    }

