package com.lwy.appsmartupdate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.lwy.smartupdate.UpdateManager;
import com.lwy.smartupdate.api.IUpdateCallback;

public class MainActivity extends AppCompatActivity {


    private static final int INSTALL_PACKAGES_REQUESTCODE = 101;
    private static final int GET_UNKNOWN_APP_SOURCES = 102;
    private String manifestJsonUrl = "https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/app/UpdateManifest.json";
    //    private String manifestJsonUrl = "http://192.168.2.107:8000/app/UpdateManifest.json";
    private IUpdateCallback mCallback;
    private TextView mVersionTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVersionTV = (TextView) findViewById(R.id.version_tv);
        mVersionTV.setText(BuildConfig.VERSION_CODE + "");
        checkAndroidOUnknowSource();
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case INSTALL_PACKAGES_REQUESTCODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES);
                }
                break;

        }
    }

    /**
     * 8.0需要处理未知应用来源权限问题
     */
    private void checkAndroidOUnknowSource() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean b = this.getPackageManager().canRequestPackageInstalls();
            if (!b) {
                //请求安装未知应用来源的权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, INSTALL_PACKAGES_REQUESTCODE);
            }
        }

    }

}
