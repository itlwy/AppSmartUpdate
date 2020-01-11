package com.lwy.smartupdate.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;

import java.io.File;


public class IntentUtils {
    public static void startActivity(Context ctx, Class clz) {
        Intent intent = new Intent(ctx, clz);
        ctx.startActivity(intent);
    }

    public static void startActivity(Context ctx, Intent intent) {
        ctx.startActivity(intent);
    }

    public static void startActivity(Context ctx, Class clz, Bundle bundle) {
        Intent intent = new Intent(ctx, clz);
        intent.putExtras(bundle);
        ctx.startActivity(intent);
    }

    public static boolean installApk(Context context, String filePath) {
        try {
            File appFile = new File(filePath);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileProvider", appFile);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(appFile), "application/vnd.android.package-archive");
            }
            if (context.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                context.startActivity(intent);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
