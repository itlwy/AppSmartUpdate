package com.lwy.smartupdate.api;

import okhttp3.Call;

/**
 * @author lwy 2018/9/2
 * @version v1.0.0
 * @name OkhttpRequest
 * @description
 */
public class OkhttpRequest implements IRequest {

    private Call mCall;

    public OkhttpRequest(Call call) {
        mCall = call;
    }

    @Override
    public void cancel() {
        mCall.cancel();
    }
}
