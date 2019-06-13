package com.lwy.smartupdate.utils;


import android.util.Log;

import com.lwy.smartupdate.UpdateManager;

public class TraceUtil {
    public static final String TAG = "com.lwy.smartupdate";

    public static void d(String msg) {
        if (UpdateManager.getConfig().isDebug())
            Log.d(TAG, msg);
    }

    public static void e(String msg) {
        if (UpdateManager.getConfig().isDebug())
            Log.e(TAG, msg);
    }


    public static void i(String msg) {
        if (UpdateManager.getConfig().isDebug())
            Log.i(TAG, msg);
    }

    public static void w(String msg) {
        if (UpdateManager.getConfig().isDebug())
            Log.w(TAG, msg);
    }
}
