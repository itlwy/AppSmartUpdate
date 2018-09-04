package com.lwy.smartupdate.api;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.HttpHeaders;

/**
 * @author lwy 2018/8/31
 * @version v1.0.0
 * @name OkhttpManager
 * @description 对IHttpManager的okhttp实现类
 */
public class OkhttpManager implements IHttpManager {
    public static final int CONNECTTIMEOUT = 60 * 1000;
    public static final int READTIMEOUT = 60 * 1000;
    private final OkHttpClient mOkhttpClient;

    public OkhttpManager() {
        mOkhttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECTTIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READTIMEOUT, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public IResponse syncGet(@NonNull String url, @NonNull Map<String, String> params) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append("?")
                        .append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
        }
        if (sb.length() > 0) {
            String param = URLEncoder.encode(sb.toString(), "utf-8");
            url += param;
        }

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = mOkhttpClient.newCall(request);
        Response response = call.execute();
        IResponse realResponse = new OkhttpResponse(response);
        return realResponse;
    }

    @Override
    public void asyncGet(@NonNull String url, @NonNull Map<String, String> params,
                         @NonNull final Callback callBack) {
        StringBuilder sb = new StringBuilder();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append("?")
                        .append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
        }
        try {
            if (sb.length() > 0) {
                String param = URLEncoder.encode(sb.toString(), "utf-8");
                url += param;
            }

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            Call call = mOkhttpClient.newCall(request);
            callBack.onRequest(new OkhttpRequest(call));
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callBack.onError(e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200)
                        callBack.onResponse(response.body().string());
                    else
                        callBack.onError(String.format(Locale.CHINA,
                                "httpCode:%d,message:%s", response.code(), response.body().string()));
                }
            });
        } catch (UnsupportedEncodingException e) {
            callBack.onError(e.toString());
        }
    }

    @Override
    public void asyncPost(@NonNull String url, @NonNull Map<String, String> params,
                          @NonNull final Callback callBack) {
        StringBuilder paramSB = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramSB.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("&");
        }
        if (paramSB.length() > 0)
            paramSB.setLength(paramSB.length() - 1);
        RequestBody body = null;
        try {
            body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=utf-8"),
                    URLEncoder.encode(paramSB.toString(), "utf-8"));
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Call call = mOkhttpClient.newCall(request);
            callBack.onRequest(new OkhttpRequest(call));
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callBack.onError(e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200)
                        callBack.onResponse(response.body().string());
                    else
                        callBack.onError(String.format("httpCode:%d,message:%s", response.code(), response.body().string()));
                }
            });
        } catch (UnsupportedEncodingException e) {
            callBack.onError(e.toString());
        }

    }

    @Override
    public void download(@NonNull String url, @NonNull final String path,
                         @NonNull final String fileName, @NonNull final FileCallback callback) {

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = mOkhttpClient.newCall(request);
        callback.onRequest(new OkhttpRequest(call));
        callback.onBefore();
        call.enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    long totalLength = HttpHeaders.contentLength(response);
                    InputStream is = response.body().byteStream();
                    File dir = new File(path);
                    if (!dir.exists())
                        dir.mkdirs();
                    File file = new File(path, fileName);
                    if (!file.exists())
                        file.createNewFile();
                    FileOutputStream os = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int length = -1;
                    long currentLength = 0;
                    while ((length = is.read(buffer)) != -1) {
                        os.write(buffer, 0, length);
                        currentLength += length;
                        float progress = (float) currentLength / totalLength;
                        callback.onProgress(progress, totalLength);
                    }
                    response.close();
                    os.close();
                    callback.onResponse(file);
                } else
                    callback.onError(String.format(Locale.CHINA,
                            "httpCode:%d,message:%s", response.code(), response.body().string()));
            }
        });
    }
}
