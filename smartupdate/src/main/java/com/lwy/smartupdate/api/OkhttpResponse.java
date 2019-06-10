package com.lwy.smartupdate.api;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;

/**
 *
 * @author lwy 2018/8/31
 * @version v1.0.0
 * @name OkhttpResponse
 * @description 对IResponse的okhttp的实现类
 */
public class OkhttpResponse implements IResponse {
    @NonNull
    private Response mResponse;

    public OkhttpResponse(Response response) {
        mResponse = response;
    }

    @Override
    public InputStream bodyStream() {
        if (mResponse.isSuccessful())
            return mResponse.body().byteStream();
        else
            return null;
    }

    @Override
    public int resultCode() {
        return mResponse.code();
    }

    @Override
    public String bodyString() throws IOException {
        return mResponse.body().string();
    }

    @Override
    public String message() {
        return mResponse.message();
    }

    @Override
    public void close() {
        mResponse.close();
    }
}
