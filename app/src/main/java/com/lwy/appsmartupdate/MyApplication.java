package com.lwy.appsmartupdate;

import android.app.Application;

import com.lwy.smartupdate.Config;
import com.lwy.smartupdate.UpdateManager;
import com.lwy.smartupdate.api.OkhttpManager;

/**
 * @author lwy 2018/9/4
 * @version v1.0.0
 * @name MyApplication
 * @description
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Config config = new Config.Builder()
                .isDebug(true)
//                .isShowInternalDialog(false)     // 需要自定义弹框时打开注释
                .httpManager(new OkhttpManager())
                .build(this);
        UpdateManager.getInstance().init(config);
    }
}
