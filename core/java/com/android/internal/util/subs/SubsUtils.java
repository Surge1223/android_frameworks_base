/*
 * Copyright (c) 2016-2018 Projekt Substratum
 * This file is part of Substratum.
 *
 * Substratum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Substratum is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Substratum.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.android.internal.util.subs;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.substratum.ISubstratumService;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import android.widget.Toast;

import com.android.internal.statusbar.IStatusBarService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class SubsUtils {
    private static Context mContext;
    private static final String KEYCODE_WINDOW = "input keyevent ";
    private static final String LOG_TAG = "SubsUtils";
    private ISubstratumService  mSubstratumService;

    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(LOG_TAG, "Service connected");
        mSubstratumService = ISubstratumService.Stub.asInterface(service);
        try {
            mSubstratumService.mkdir("/data/system/test");
        } catch (RemoteException e) {
        }
    }

    public static  boolean isAvailableApp(String packageName, Context context) {
        final PackageManager pm = mContext.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            int enabled = pm.getApplicationEnabledSetting(packageName);
            return enabled != PackageManager.COMPONENT_ENABLED_STATE_DISABLED &&
                    enabled != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static String runStringCommand(String input) {
        Process proc = null;
        try {
            Runtime rt = Runtime.getRuntime();
            proc = rt.exec(new String[]{input});
            try (BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()))) {
                return stdInput.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (proc != null) {
                proc.destroy();
            }
        }
        return null;
    }


    public static void  sendShellKeycode(int keycode)  {
        Log.i("Attempted to open window with %s", String.valueOf(keycode));
        for (int i=0; i < keycode; i++) {
            runStringCommand(KEYCODE_WINDOW + String.valueOf(keycode));
        }
    }

    public static final String INTENT_SCREENSHOT = "action_take_screenshot";
    public static final String INTENT_REGION_SCREENSHOT = "action_take_region_screenshot";

    public static void switchScreenOff(Context ctx) {
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        if (pm!= null) {
            pm.goToSleep(SystemClock.uptimeMillis());
        }
    }

    public static boolean deviceHasFlashlight(Context ctx) {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }


    public static void takeScreenshot(boolean full) {
        IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
        try {
            wm.sendCustomAction(new Intent(full? INTENT_SCREENSHOT : INTENT_REGION_SCREENSHOT));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void byeFelicia(String packagename) {
        IActivityManager am = ActivityManagerNative.getDefault();
        PackageManager packageManager = mContext.getPackageManager();
        ComponentName felicia = null;
        felicia = new ComponentName("com.android.systemui", "com" +
                ".android.systemui.SystemUIService");
        try {
            mContext.stopService(new Intent().setComponent(felicia));
            for (ActivityManager.RunningAppProcessInfo app : am.getRunningAppProcesses()) {
                if (packagename.equals(app.processName)) {
                    am.killApplicationProcess(app.processName, app.uid);
                    break;
                }
                Log.d(LOG_TAG, "BYE FELICIA ==> " + packagename);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed felicia :", e);
        }
    }

    void sayByeFelicia() {
        byeFelicia("com.android.systemui");
    }

    //  kanged from ezio
    public static ActivityInfo getRunningActivityInfo(Context context) {
        final ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        final PackageManager pm = context.getPackageManager();

        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (tasks != null && !tasks.isEmpty()) {
            ActivityManager.RunningTaskInfo top = tasks.get(0);
            try {
                return pm.getActivityInfo(top.topActivity, 0);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        return null;
    }
}

