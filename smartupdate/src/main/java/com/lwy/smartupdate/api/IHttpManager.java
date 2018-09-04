package com.lwy.smartupdate.api;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author lwy 2018/8/31
 * @version v1.0.0
 * @name IHttpManager
 * @description
 */
public interface IHttpManager {


    IResponse syncGet(@NonNull String url, @NonNull Map<String, String> params) throws IOException;

    /**
     * 异步get
     *
     * @param url      get请求地址
     * @param params   get参数
     * @param callBack 回调
     */
    void asyncGet(@NonNull String url, @NonNull Map<String, String> params, @NonNull Callback callBack);


    /**
     * 异步post
     *
     * @param url      post请求地址
     * @param params   post请求参数
     * @param callBack 回调
     */
    void asyncPost(@NonNull String url, @NonNull Map<String, String> params, @NonNull Callback callBack);

    /**
     * 下载
     *
     * @param url      下载地址
     * @param path     文件保存路径
     * @param fileName 文件名称
     * @param callback 回调
     */
    void download(@NonNull String url, @NonNull String path, @NonNull String fileName, @NonNull FileCallback callback);

    /**
     * 下载回调
     */
    interface FileCallback {
        /**
         * 进度
         *
         * @param percent 进度0.00 - 0.50  - 1.00
         * @param total   文件总大小 单位字节
         */
        void onProgress(float percent, long total);

        /**
         * 错误回调
         *
         * @param error 错误提示
         */
        void onError(String error);

        /**
         * 结果回调
         *
         * @param file 下载好的文件
         */
        void onResponse(File file);

        /**
         * 请求之前
         */
        void onBefore();

        void onRequest(IRequest request);
    }

    /**
     * 网络请求回调
     */
    interface Callback {

        void onRequest(IRequest request);

        /**
         * 结果回调
         *
         * @param result 结果
         */
        void onResponse(String result);

        /**
         * 错误回调
         *
         * @param error 错误提示
         */
        void onError(String error);
    }
}
