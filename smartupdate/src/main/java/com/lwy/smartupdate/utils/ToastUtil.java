package com.lwy.smartupdate.utils;

import android.content.Context;
import android.widget.Toast;
/**
 *
 * @author lwy 2018/8/31
 * @version v1.0.0
 * @name ToastUtil
 * @description
 */
public class ToastUtil {

    public static void toast(Context context, String tip) {
        Toast.makeText(context, tip, Toast.LENGTH_LONG).show();
    }
}
