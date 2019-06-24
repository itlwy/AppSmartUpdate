package com.lwy.smartupdate;

import android.content.Context;
import android.text.TextUtils;

import com.lwy.smartupdate.api.IHttpManager;
import com.lwy.smartupdate.api.OkhttpManager;

/**
 * @author lwy 2018/9/2
 * @version v1.0.0
 * @name Config
 * @description 存储配置
 */
public class Config {

    private boolean isDebug;
    private String updateDirPath;
    private boolean isOnlyWifi;
    private boolean isShowDialog;
    private IHttpManager httpManager;

    private Config(Builder builder) {
        isDebug = builder.isDebug;
        updateDirPath = builder.updateDirPath;
        isOnlyWifi = builder.isOnlyWifi;
        isShowDialog = builder.isShowDialog;
        httpManager = builder.httpManager;
        if (httpManager == null)
            httpManager = new OkhttpManager();
    }

    public boolean isDebug() {
        return isDebug;
    }

    public String getUpdateDirPath() {
        return updateDirPath;
    }

    public boolean isOnlyWifi() {
        return isOnlyWifi;
    }

    public boolean isShowDialog() {
        return isShowDialog;
    }

    public IHttpManager getHttpManager() {
        return httpManager;
    }

    public static class Builder {
        private boolean isDebug;
        private String updateDirPath;
        private boolean isOnlyWifi;
        private boolean isShowDialog = true;
        private IHttpManager httpManager;

        public Builder() {
        }

        public Builder isDebug(boolean debug) {
            isDebug = debug;
            return this;
        }

        public Builder updateDirPath(String dirPath) {
            updateDirPath = dirPath;
            return this;
        }

        public Builder isOnlyWifi(boolean flag) {
            isOnlyWifi = flag;
            return this;
        }

        public Builder isShowInternalDialog(boolean flag) {
            isShowDialog = flag;
            return this;
        }

        public Builder httpManager(IHttpManager httpManager) {
            this.httpManager = httpManager;
            return this;
        }

        public Config build(Context context) {
            if (TextUtils.isEmpty(updateDirPath))
                updateDirPath = context.getExternalFilesDir("update").getAbsolutePath() + "/";
            return new Config(this);
        }
    }

}
