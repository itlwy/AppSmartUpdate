package com.lwy.smartupdate.api;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author lwy 2018/8/31
 * @version v.1.0.0
 * @name IResponse
 * @description
 */
public interface IResponse {

    InputStream bodyStream();

    int resultCode();

    String bodyString() throws IOException;

    String message();

    void close();
}
