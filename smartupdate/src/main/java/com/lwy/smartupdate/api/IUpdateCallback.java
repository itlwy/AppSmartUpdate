package com.lwy.smartupdate.api;

import com.lwy.smartupdate.UpdateManager;
import com.lwy.smartupdate.data.AppUpdateModel;

/**
 * @author lwy 2018/8/30
 * @version v1.0.0
 * @name IUpdateCallback
 * @description 自动更新接口回调, 用于通知更新情况
 */
public interface IUpdateCallback {

    /**
     * 通知无新版本需要更新,运行在主线程
     */
    void noNewApp();

    /**
     * 检测到有新版本更新,运行在主线程，可用来自定义提示框
     * @param appUpdateModel    更新清单的model类
     * @param updateManager     单例的UpdateManager 同UpdateManager.getInstance()
     * @param updateMethod      更新的方式，增量 or 全量
     */
    void hasNewApp(AppUpdateModel appUpdateModel, UpdateManager updateManager,int updateMethod);

    /**
     * 检测到有更新,自动更新准备开始时回调,运行在主线程，可做一些提示等
     */
    void beforeUpdate();

    /**
     * 自动更新的进度回调（分增量和全量更新）,运行在主线程
     *
     * @param percent     当前总进度百分比
     * @param totalLength 更新总大小(全量为apk大小,增量为全部补丁大小和)
     * @param patchIndex  当前更新的补丁索引(从1开始)
     * @param patchCount  需要更新的总补丁数(当为0时表示是增量更新)
     */
    void onProgress(int percent, long totalLength, int patchIndex, int patchCount);

    /**
     * 下载完成，准备更新,运行在主线程
     */
    void onCompleted();

    /**
     * 异常回调,运行在主线程
     *
     * @param error 异常信息
     */
    void onError(String error);

    /**
     * 用户取消了询问更新对话框
     */
    void onCancelUpdate();

    /**
     * 取消了更新进度对话框,压入后台自动更新,此时由通知栏通知进度
     */
    void onBackgroundTrigger();
}
