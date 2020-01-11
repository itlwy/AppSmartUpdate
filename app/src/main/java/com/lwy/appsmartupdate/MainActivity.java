package com.lwy.appsmartupdate;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.lwy.smartupdate.UpdateManager;
import com.lwy.smartupdate.api.IUpdateCallback;
import com.lwy.smartupdate.data.AppUpdateModel;

public class MainActivity extends AppCompatActivity {


    private static final int INSTALL_PACKAGES_REQUESTCODE = 101;
    private static final int GET_UNKNOWN_APP_SOURCES = 102;
    private String manifestJsonUrl = "https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/app/UpdateManifest.json";
    //    private String manifestJsonUrl = "http://192.168.2.107:8000/app/UpdateManifest.json";
    private IUpdateCallback mCallback;
    private TextView mVersionTV;
    private ProgressDialog mProgressDialog;
    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVersionTV = (TextView) findViewById(R.id.version_tv);
        mVersionTV.setText(BuildConfig.VERSION_CODE + "");
        checkAndroidOUnknowSource();
//        registerUpdateCallbak();  // 需要自定义弹框时打开注释
        checkUpdate();

    }

    private void checkUpdate() {
        UpdateManager.getInstance().update(this, manifestJsonUrl);
    }


    public void registerUpdateCallbak() {
        mCallback = new IUpdateCallback() {
            @Override
            public void noNewApp() {
                Toast.makeText(MainActivity.this, "当前已是最新版本!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void hasNewApp(AppUpdateModel appUpdateModel, UpdateManager updateManager, final int updateMethod) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                mDialog = builder.setTitle("自动更新提示")
                        .setMessage(appUpdateModel.getTip())
                        .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UpdateManager.getInstance().startUpdate(updateMethod);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create();
                mDialog.show();
            }

            @Override
            public void beforeUpdate() {
                // 更新开始
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setTitle("更新中...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setMessage("正在玩命更新中...");
                mProgressDialog.setMax(100);
                mProgressDialog.setProgress(0);
                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // 退到后台自动更新，进度由通知栏显示
                        if (UpdateManager.getInstance().isRunning()) {
                            UpdateManager.getInstance().onBackgroundTrigger();
                        }
                    }
                });
                mProgressDialog.show();
            }

            @Override
            public void onProgress(int percent, long totalLength, int patchIndex, int patchCount) {
                String tip;
                if (patchCount > 0) {
                    tip = String.format("正在下载补丁%d/%d", patchIndex, patchCount);
                } else {
                    tip = "正在下载更新中...";
                }
                mProgressDialog.setProgress(percent);
                mProgressDialog.setMessage(tip);
            }

            @Override
            public void onCompleted() {
                mProgressDialog.dismiss();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelUpdate() {

            }

            @Override
            public void onBackgroundTrigger() {
                Toast.makeText(MainActivity.this, "转为后台更新，进度由通知栏提示!", Toast.LENGTH_LONG).show();
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
