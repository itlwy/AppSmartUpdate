package com.lwy.smartupdate;

import android.content.Context;
import android.text.TextUtils;

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

    private Config(Builder builder) {
        isDebug = builder.isDebug;
        updateDirPath = builder.updateDirPath;
        isOnlyWifi = builder.isOnlyWifi;
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

    public static class Builder {
        private boolean isDebug;
        private String updateDirPath;
        private boolean isOnlyWifi;

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

        public Config build(Context context) {
            if (TextUtils.isEmpty(updateDirPath))
                updateDirPath = context.getExternalFilesDir("update").getAbsolutePath() + "/";
            return new Config(this);
        }
    }

}
