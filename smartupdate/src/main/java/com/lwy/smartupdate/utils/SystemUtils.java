package com.lwy.smartupdate.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Administrator on 2016/6/8.
 */
public class SystemUtils {
    /**
     * @param activity
     * @param permissionStr
     * @param packageName
     * @return
     */
    public static boolean hasPermission(Activity activity, String permissionStr, String packageName) {
        PackageManager pm = activity.getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(permissionStr, packageName));
        if (permission) {
            return true;
        } else {
            return false;
        }
//        if (ContextCompat.checkSelfPermission()
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // 判断是否需要解释获取权限原因
//            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
//                    Manifest.permission.READ_CONTACTS)) {
//                // 需要向用户解释
//                // 此处可以弹窗或用其他方式向用户解释需要该权限的原因
//            } else {
//                // 无需解释，直接请求权限
//                ActivityCompat.requestPermissions(thisActivity,
//                        new String[]{Manifest.permission.READ_CONTACTS},
//                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS 是自定义的常量，在回调方法中可以获取到
//            }
//        }
    }

    /**
     * 得到应用程序的版本名
     */

    public static String getAppVersionName(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(ctx.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            //不可能发生；
            return "";
        }
    }

    /**
     * 获取当前本地apk的版本代码
     *
     * @param ctx
     * @return
     */
    public static int getAppVersionCode(Context ctx) {
        int versionCode = 0;
        try {
            versionCode = ctx.getPackageManager().
                    getPackageInfo(ctx.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * Check how much usable space is available at a given path.
     * 获取指定目录仍可用的空间大小
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    @SuppressLint("NewApi")
    public static long getAvailableSpace(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }

    /**
     * Check if external storage is built-in or removable.
     * 检查外部存储器(eg:sd卡)是否被移除
     *
     * @return True if external storage is removable (like an SD card), false
     * otherwise.
     */
    @SuppressLint("NewApi")
    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * Check if OS version has built-in external cache dir method.
     * 检查当前系统版本是否有获得APP的Cache的方法
     *
     * @return
     */
    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    /**
     * Get the external app cache directory.
     * 获取APP目录
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @SuppressLint("NewApi")
    public static File getExternalCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static boolean isWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return info != null && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

}
