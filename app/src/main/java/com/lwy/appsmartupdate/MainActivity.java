package com.lwy.appsmartupdate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.lwy.smartupdate.UpdateManager;
import com.lwy.smartupdate.api.IUpdateCallback;

public class MainActivity extends AppCompatActivity {


    private String manifestJsonUrl = "https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/UpdateManifest.json";
    private IUpdateCallback mCallback;
    private TextView mVersionTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVersionTV = (TextView) findViewById(R.id.version_tv);
        mVersionTV.setText(BuildConfig.VERSION_CODE + "");
        checkUpdate();
    }

    private void checkUpdate() {
        UpdateManager.getInstance().update(this, manifestJsonUrl, null);
    }


    public void registerUpdateCallbak() {
        mCallback = new IUpdateCallback() {
            @Override
            public void noNewApp() {

            }

            @Override
            public void beforeUpdate() {

            }

            @Override
            public void onProgress(int percent, long totalLength, int patchIndex, int patchCount) {

            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(String error) {

            }

            @Override
            public void onCancelUpdate() {

            }

            @Override
            public void onBackgroundTrigger() {

            }
        };
        UpdateManager.getInstance().register(mCallback);
    }

    @Override
    protected void onDestroy() {
        if (mCallback != null)
            UpdateManager.getInstance().unRegister(mCallback);
        super.onDestroy();

    }
}
