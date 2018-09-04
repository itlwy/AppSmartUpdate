package com.lwy.appsmartupdate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.lwy.smartupdate.UpdateManager;
import com.lwy.smartupdate.api.IUpdateCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private Button mUpdateBtn;
    private String manifestJsonUrl = "https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/UpdateManifest.json";
    private IUpdateCallback mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUpdateBtn = (Button) findViewById(R.id.update_btn);
        mUpdateBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_btn:
                UpdateManager.getInstance().update(this, manifestJsonUrl, null);
                break;

        }
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
