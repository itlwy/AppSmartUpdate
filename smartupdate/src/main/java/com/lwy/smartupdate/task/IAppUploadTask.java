package com.lwy.smartupdate.task;

/**
 * @author lwy 2018/8/30
 * @version v1.0.0
 * @name IAppUploadTask
 * @description
 */
public interface IAppUploadTask {

    void start(String dirPath, CallBack callBack);

    void cancel();


    interface CallBack {

        void onProgress(int percent, long totalLength, int patchIndex, int patchCount);

        void onCompleted(String apkPath);

        void onError(String error);

    }
}
