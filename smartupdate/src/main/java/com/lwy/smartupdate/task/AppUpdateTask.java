package com.lwy.smartupdate.task;

/**
 * @author lwy 2018/8/30
 * @version v1.0.0
 * @name AppUpdateTask
 * @description
 */
public abstract class AppUpdateTask implements IAppUploadTask {
    protected String dirPath;
    protected CallBack callBack;

    @Override
    public void start(String dirPath, CallBack callBack) {
        this.callBack = callBack;
        this.dirPath = dirPath;
        execute();
    }

    protected abstract void execute();
}
