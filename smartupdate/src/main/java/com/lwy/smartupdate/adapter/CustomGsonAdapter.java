package com.lwy.smartupdate.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.lwy.smartupdate.data.AppUpdateModel;

import java.io.IOException;

/**
 * @author lwy 2018/8/31
 * @version v1.0.0
 * @name CustomGsonAdapter
 * @description 自定义gson的序列化Adapter
 */
public class CustomGsonAdapter {

    public static final TypeAdapter<AppUpdateModel> ADAPTER_APPUPDATEMODEL = new TypeAdapter<AppUpdateModel>() {
        @Override
        public void write(JsonWriter out, AppUpdateModel value) throws IOException {
        }

        @Override
        public AppUpdateModel read(JsonReader in) throws IOException {
            return null;
        }
    };
}
