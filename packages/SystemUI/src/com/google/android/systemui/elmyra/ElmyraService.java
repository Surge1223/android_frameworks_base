package com.google.android.systemui.elmyra;

import android.content.Context;
import android.metrics.LogMaker;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Dumpable;
import com.google.android.systemui.elmyra.actions.Action;
import com.google.android.systemui.elmyra.actions.LambdaF;
import com.google.android.systemui.elmyra.feedback.FeedbackEffect;
import com.google.android.systemui.elmyra.gates.Gate;
import com.google.android.systemui.elmyra.sensors.GestureSensor;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ElmyraService implements Dumpable {
    private Action.Listener mActionListener;
    private List<Action> mActions;
    private Context mContext;
    private List<FeedbackEffect> mFeedbackEffects;
    private Gate.Listener mGateListener;
    private List<Gate> mGates;
    private GestureSensor.Listener mGestureListener = new GestureListener();
    private GestureSensor mGestureSensor;
    private Action mLastActiveAction;
    private long mLastPrimedGesture;
    private int mLastStage;
    private MetricsLogger mLogger;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    public ElmyraService(final Context mContext, ServiceConfiguration serviceConfiguration) {
        this.mActionListener = action -> ElmyraService.this.updateSensorListener();

        this.mGateListener = gate -> ElmyraService.this.updateSensorListener();

        GestureListener mGestureListener = new GestureListener();
        mLogger = new MetricsLogger();
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        if (mPowerManager != null) {
            mWakeLock = mPowerManager.newWakeLock(1, ":Elmyra/ElmyraService");
        }
        byte id = 0;
        mActions.forEach(new LambdaF(id));
        mFeedbackEffects = new ArrayList<>(serviceConfiguration.getFeedbackEffects());
        byte id2 = 1;
        (mGates = new ArrayList<Gate>(serviceConfiguration.getGates())).forEach(new LambdaF(id2));
        mGestureSensor = serviceConfiguration.getGestureSensor();
        if (mGestureSensor != null) {
            mGestureSensor.setGestureListener(mGestureListener);
        }
        updateSensorListener();
    }

    private void activateGates() {
        for (int i = 0; i < this.mGates.size(); ++i) {
            this.mGates.get(i).activate();
        }
    }

    private Gate blockingGate() {
        for (int i = 0; i < this.mGates.size(); ++i) {
            if (this.mGates.get(i).isBlocking()) {
                return this.mGates.get(i);
            }
        }
        return null;
    }

    private void deactivateGates() {
        for (int i = 0; i < this.mGates.size(); ++i) {
            this.mGates.get(i).deactivate();
        }
    }

    private Action firstAvailableAction() {
        for (int i = 0; i < this.mActions.size(); ++i) {
            if (this.mActions.get(i).isAvailable()) {
                return this.mActions.get(i);
            }
        }
        return null;
    }

    private void startListening() {
        if (this.mGestureSensor != null && !this.mGestureSensor.isListening()) {
            this.mGestureSensor.startListening();
        }
    }

    private void stopListening() {
        if (this.mGestureSensor != null && this.mGestureSensor.isListening()) {
            this.mGestureSensor.stopListening();
            for (int i = 0; i < this.mFeedbackEffects.size(); ++i) {
                this.mFeedbackEffects.get(i).onRelease();
            }
            final Action updateActiveAction = this.updateActiveAction();
            if (updateActiveAction != null) {
                updateActiveAction.onProgress(0.0f, 0);
            }
        }
    }

    private Action updateActiveAction() {
        final Action firstAvailableAction = this.firstAvailableAction();
        if (this.mLastActiveAction != null && firstAvailableAction != this.mLastActiveAction) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Switching action from ");
            sb.append(this.mLastActiveAction);
            sb.append(" to ");
            sb.append(firstAvailableAction);
            Log.i("Elmyra/ElmyraService", sb.toString());
            this.mLastActiveAction.onProgress(0.0f, 0);
        }
        return this.mLastActiveAction = firstAvailableAction;
    }

    @Override
    public void dump(final FileDescriptor fileDescriptor, final PrintWriter printWriter, final String[] array) {
        final StringBuilder sb = new StringBuilder();
        sb.append(ElmyraService.class.getSimpleName());
        sb.append(" state:");
        printWriter.println(sb.toString());
        printWriter.println("  Gates:");
        final int n = 0;
        for (int i = 0; i < this.mGates.size(); ++i) {
            printWriter.print("    ");
            if (this.mGates.get(i).isActive()) {
                String s;
                if (this.mGates.get(i).isBlocking()) {
                    s = "X ";
                }
                else {
                    s = "O ";
                }
                printWriter.print(s);
            }
            else {
                printWriter.print("- ");
            }
            printWriter.println(this.mGates.get(i).toString());
        }
        printWriter.println("  Actions:");
        for (int j = 0; j < this.mActions.size(); ++j) {
            printWriter.print("    ");
            String s2;
            if (this.mActions.get(j).isAvailable()) {
                s2 = "O ";
            }
            else {
                s2 = "X ";
            }
            printWriter.print(s2);
            printWriter.println(this.mActions.get(j).toString());
        }
        final StringBuilder sb2 = new StringBuilder();
        sb2.append("  Active: ");
        sb2.append(this.mLastActiveAction);
        printWriter.println(sb2.toString());
        printWriter.println("  Feedback Effects:");
        for (int k = n; k < this.mFeedbackEffects.size(); ++k) {
            printWriter.print("    ");
            printWriter.println(this.mFeedbackEffects.get(k).toString());
        }
        final StringBuilder sb3 = new StringBuilder();
        sb3.append("  Gesture Sensor: ");
        sb3.append(this.mGestureSensor.toString());
        printWriter.println(sb3.toString());
        if (this.mGestureSensor instanceof Dumpable) {
            ((Dumpable)this.mGestureSensor).dump(fileDescriptor, printWriter, array);
        }
    }

    protected void updateSensorListener() {
        final Action updateActiveAction = this.updateActiveAction();
        if (updateActiveAction == null) {
            Log.i("Elmyra/ElmyraService", "No available actions");
            this.deactivateGates();
            this.stopListening();
            return;
        }
        this.activateGates();
        final Gate blockingGate = this.blockingGate();
        if (blockingGate != null) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Gated by ");
            sb.append(blockingGate);
            Log.i("Elmyra/ElmyraService", sb.toString());
            this.stopListening();
            return;
        }
        final StringBuilder sb2 = new StringBuilder();
        sb2.append("Unblocked; current action: ");
        sb2.append(updateActiveAction);
        Log.i("Elmyra/ElmyraService", sb2.toString());
        this.startListening();
    }

    public class GestureListener implements GestureSensor.Listener {

        public void onGestureDetected(final GestureSensor gestureSensor) {
            ElmyraService.this.mWakeLock.acquire(2000L);
            boolean interactive = ElmyraService.this.mPowerManager.isInteractive();
            LogMaker setType = new LogMaker(999).setType(4);
            int subtype;
            if (interactive) {
                subtype = 1;
            } else {
                subtype = 2;
            }
            LogMaker setSubtype = setType.setSubtype(subtype);
            long latency;
            if (interactive) {
                latency = SystemClock.uptimeMillis() - ElmyraService.this.mLastPrimedGesture;
            } else {
                latency = 0L;
            }
            LogMaker setLatency = setSubtype.setLatency(latency);
            ElmyraService.this.mLastPrimedGesture = 0L;
            Action actionUpdate = ElmyraService.this.updateActiveAction();
            if (actionUpdate != null) {
                Log.i("Elmyra/ElmyraService", "Triggering " + actionUpdate);
                actionUpdate.onTrigger();
                for (int i = 0; i < ElmyraService.this.mFeedbackEffects.size(); ++i) {
                    ElmyraService.this.mFeedbackEffects.get(i).onResolve();
                }
                setLatency.setPackageName(actionUpdate.getClass().getName());
            }
            ElmyraService.this.mLogger.write(setLatency);
        }

        public void onGestureProgress(final GestureSensor gestureSensor, float n, int n2) {
            Action actionUpdate = ElmyraService.this.updateActiveAction();
            if (actionUpdate != null) {
                actionUpdate.onProgress(n, n2);
                for (int i = 0; i < ElmyraService.this.mFeedbackEffects.size(); ++i) {
                    ElmyraService.this.mFeedbackEffects.get(i).onProgress(n, n2);
                }
            }

            if (n2 != ElmyraService.this.mLastStage) {
                long uptimeMillis = SystemClock.uptimeMillis();
                if (n2 == 2) {
                    ElmyraService.this.mLogger.action(998);
                    ElmyraService.this.mLastPrimedGesture = uptimeMillis;
                } else if (n2 == 0 && ElmyraService.this.mLastPrimedGesture != 0L) {
                    ElmyraService.this.mLogger.write(new LogMaker(997).setType(4).setLatency(uptimeMillis - ElmyraService.this.mLastPrimedGesture));
                }
                ElmyraService.this.mLastStage = n2;
            }
        }

    }
}

