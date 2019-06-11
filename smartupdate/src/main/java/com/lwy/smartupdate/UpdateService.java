package com.lwy.smartupdate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.lwy.smartupdate.data.AppUpdateModel;
import com.lwy.smartupdate.task.FullAppUpdateTask;
import com.lwy.smartupdate.task.IAppUploadTask;
import com.lwy.smartupdate.task.PatchAppUploadTask;
import com.lwy.smartupdate.utils.FileUtils;
import com.lwy.smartupdate.utils.IntentUtils;
import com.lwy.smartupdate.utils.SystemUtils;
import com.lwy.smartupdate.utils.TraceUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.lwy.smartupdate.UpdateManager.FLAG_NOTIFY_FOREGROUND;

/**
 * @author lwy 2018/8/31
 * @version v1.0.0
 * @name UpdateService
 * @description
 */
public class UpdateService extends Service implements IAppUploadTask.CallBack {
    public static final String INTENT_ACTION = "intent_action";
    public static final int ACTION_UPDATE = 100;  // 发起更新
    public static final int ACTION_CACEL = 101;  // 发起取消

    public static final String PARAM_SHOWFLAG = "showFlag";
    public static final String PARAM_UPDATEMETHODFLAG = "updateMethod";
    public static final String PARAM_ICONRES = "mIconRes";
//    public static final String PARAM_UPDATE_PARAM_MODEL = "update_param_model";


    public static final int FLAG_UPDATE_ALL = 0;  // 全量更新
    public static final int FLAG_UPDATE_PATCH = 1;  // 增量更新

    private static final int NOTIFY_ID = 0;


    private NotificationManager mNotificationManager;
    private Notification mNotification;

    public int updateMethod = -1;   // 0：全量更新，1：增量更新

    private int mIconRes;  // 通知栏提示得图标资源
    private int mCurrentVersion;  // 当前apk版本


    private AppUpdateModel mAppUpdateModel;

    private IAppUploadTask appUploadTask;
    private int mLastPercent;


    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            notifyMsg("温馨提醒", "更新失败", 0, null);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        int typeIntent = intent.getIntExtra(INTENT_ACTION, -1);
        if (typeIntent == ACTION_CACEL) {
            if (appUploadTask != null) {
                appUploadTask.cancel();
            }
            mNotificationManager.cancel(NOTIFY_ID);
            clearLast();
            stopSelf();
        } else if (typeIntent == ACTION_UPDATE) {
            updateMethod = intent.getIntExtra(PARAM_UPDATEMETHODFLAG, -1);// 更新方式，全量还是增量
            mIconRes = intent.getIntExtra(PARAM_ICONRES, R.mipmap.dialog_tip);// 更新方式，全量还是增量
            mCurrentVersion = SystemUtils.getAppVersionCode(this);// 当前apk版本

            if (updateMethod < 0) {
                Toast.makeText(this, "错误:抱歉更新未能运行,未指定更新方式,请联系系统服务单位,谢谢", Toast.LENGTH_LONG).show();
                return super.onStartCommand(intent, flags, startId);
            }
            if (mCurrentVersion < 0) {
                Toast.makeText(this, "错误:抱歉更新未能运行,未指定当前更新版本号,请联系系统服务单位,谢谢", Toast.LENGTH_LONG).show();
                return super.onStartCommand(intent, flags, startId);
            }
            mAppUpdateModel = UpdateManager.getInstance().getAppUpdateModel();
            clearLast();
//            mAppUpdateModel = intent.getParcelableExtra(UPDATE_PARAM_MODEL);
            prepare();
        } else
            return super.onStartCommand(intent, flags, startId);
        return super.onStartCommand(intent, flags, startId);
    }

    private void prepare() {
        if (updateMethod == FLAG_UPDATE_ALL) {
            appUploadTask = new FullAppUpdateTask(mAppUpdateModel);
        } else {
            HashMap<String, AppUpdateModel.PatchInfoModel> patchMap = mAppUpdateModel.getPatchInfoMap();
            Set<Map.Entry<String, AppUpdateModel.PatchInfoModel>> set = patchMap.entrySet();
            Iterator<Map.Entry<String, AppUpdateModel.PatchInfoModel>> iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, AppUpdateModel.PatchInfoModel> entry = iterator.next();
                String key = entry.getKey();
                int patchVersion = Integer.parseInt(key.substring(1));
                if (patchVersion < mCurrentVersion)
                    iterator.remove();
            }
            appUploadTask = new PatchAppUploadTask(this, mCurrentVersion, mAppUpdateModel.getNewVersion(),
                    mAppUpdateModel.getPatchInfoMap());
        }
        appUploadTask.start(UpdateManager.getConfig().getUpdateDirPath(), this);
    }

    private void clearLast() {
        FileUtils.deleteAllFiles(UpdateManager.getConfig().getUpdateDirPath());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notifyMsg(String title, String content, int progress, PendingIntent pIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);//为了向下兼容，这里采用了v7包下的NotificationCompat来构造
        builder.setSmallIcon(mIconRes).setLargeIcon(BitmapFactory.decodeResource(getResources(), mIconRes)).setContentTitle(title);
        if (progress > 0 && progress < 100) {
            //下载进行中
            builder.setProgress(100, progress, false);
        } else {
            builder.setProgress(0, 0, false);
        }
        builder.setAutoCancel(true);
        if (progress < 0) {
            // 异常情况
            builder.setContentTitle(title);
            builder.setContentText("自动更新发生异常,点击查看详情");
            android.support.v4.app.NotificationCompat.BigTextStyle style = new android.support.v4.app.NotificationCompat.BigTextStyle();
            style.bigText(content);
            style.setBigContentTitle(title);
            builder.setStyle(style);
        } else {
            builder.setContentText(content);
        }
        builder.setOngoing(true);
        if (pIntent != null) {
            //下载完成
            builder.setContentIntent(pIntent);
        }
        mNotification = builder.build();
        mNotificationManager.notify(NOTIFY_ID, mNotification);
    }

    /**
     * 安装apk文件
     *
     * @return
     */
    private PendingIntent getInstallIntent(String apkFilePath) {
        File appFile = new File(apkFilePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri fileUri = FileProvider.getUriForFile(this,
                    this.getApplicationContext().getPackageName() + ".fileProvider", appFile);
            intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(appFile), "application/vnd.android.package-archive");
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /*           分割线                */
    @Override
    public void onProgress(int percent, long totalLength, int patchIndex, int patchCount) {
        if (mLastPercent != percent && percent % 10 == 0) {
            //避免频繁刷新View，这里设置每下载10%提醒更新一次进度
            mLastPercent = percent;
//            TraceUtil.d(String.format(Locale.CHINA, "onProgress-> percent:%d,totalLength:%d,patchIndex:%d,patchCount:%d ",
//                    percent, totalLength, patchIndex, patchCount));

            if (UpdateManager.getInstance().getNotifyFlag() == FLAG_NOTIFY_FOREGROUND) {
                UpdateManager.getInstance().onProgress(percent, totalLength, patchIndex, patchCount);
            } else {
                String tip;
                if (patchCount > 0) {
                    tip = String.format("正在下载补丁%d/%d", patchIndex, patchCount);
                } else {
                    tip = "正在下载更新中...";
                }
                if (patchIndex == patchCount && percent >= 100)
                    notifyMsg("温馨提醒", "下载已完成,处理中.", 100, null);
                else
                    notifyMsg("温馨提醒", tip, percent, null);
            }
        }
    }

    @Override
    public void onCompleted(String apkPath) {
        TraceUtil.d("onCompleted : " + apkPath);
        if (UpdateManager.getInstance().getNotifyFlag() == FLAG_NOTIFY_FOREGROUND) {
            UpdateManager.getInstance().onCompleted();
        } else {
            notifyMsg("温馨提醒", "点击安装", 100, getInstallIntent(apkPath)); // 可说是多余的一步
        }
        mNotificationManager.cancel(NOTIFY_ID);
        IntentUtils.installApk(UpdateService.this, apkPath);
        stopSelf();
    }

    @Override
    public void onError(String error) {
        if (UpdateManager.getInstance().getNotifyFlag() == FLAG_NOTIFY_FOREGROUND) {
            UpdateManager.getInstance().onError(error);
        } else
            notifyMsg("温馨提醒", "更新失败：" + error, -1, null);
        TraceUtil.d("onError: " + error);
        clearLast();
        stopSelf();
    }

}
