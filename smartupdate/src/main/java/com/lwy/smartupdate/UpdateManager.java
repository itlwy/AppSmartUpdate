package com.lwy.smartupdate;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.lwy.smartupdate.api.IHttpManager;
import com.lwy.smartupdate.api.IRequest;
import com.lwy.smartupdate.api.IUpdateCallback;
import com.lwy.smartupdate.data.AppUpdateModel;
import com.lwy.smartupdate.utils.SystemUtils;
import com.lwy.smartupdate.utils.ToastUtil;
import com.lwy.smartupdate.utils.TraceUtil;
import com.lwy.smartupdate.view.UpdateDialog;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.lwy.smartupdate.UpdateService.ACTION_CACEL;
import static com.lwy.smartupdate.UpdateService.ACTION_UPDATE;
import static com.lwy.smartupdate.UpdateService.FLAG_UPDATE_ALL;
import static com.lwy.smartupdate.UpdateService.FLAG_UPDATE_PATCH;
import static com.lwy.smartupdate.UpdateService.INTENT_ACTION;
import static com.lwy.smartupdate.UpdateService.PARAM_ICONRES;
import static com.lwy.smartupdate.UpdateService.PARAM_SHOWFLAG;
import static com.lwy.smartupdate.UpdateService.PARAM_UPDATEMETHODFLAG;

/**
 * @author lwy 2018/8/31
 * @version v1.0.0
 * @name UpdateManager
 * @description 外观类, 暴露给client使用
 */
public class UpdateManager {
    public static final int FLAG_NOTIFY_FOREGROUND = 0;  // 前台通知下载
    public static final int FLAG_NOTIFY_BACKGROUND = 1;  // 后台下载

    private Config mConfig;
    private static UpdateManager singleton = null;
    private Dispatcher mDispatcher;
    private WeakReference<Activity> mActivityTarget;
    private WeakReference<UpdateDialog> mUpdateDialogTarget;
    private String mUpdateInfoUrl;
    private Set<IUpdateCallback> mListener = new CopyOnWriteArraySet<>();

    private AppUpdateModel mAppUpdateModel;
    private volatile boolean isRunning;
    private volatile int notifyFlag = FLAG_NOTIFY_FOREGROUND;   // 0：前台通知下载，1：后台下载
    private int mAppVersionCode;


    public int getNotifyFlag() {
        return notifyFlag;
    }

    public IHttpManager getHttpManager() {
        return mConfig.getHttpManager();
    }

    static class Dispatcher {
        static class MyHandler extends Handler {
        }

        private Handler mHandler;

        public Dispatcher() {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                throw new RuntimeException("UpdateManager.Dispatcher must init in main thread!");
            }
            mHandler = new MyHandler();
        }

        void dispatch(Runnable runnable) {
            if (Looper.myLooper() == Looper.getMainLooper())
                runnable.run();
            else
                mHandler.post(runnable);
        }
    }

    private UpdateManager() {
    }


    public void dispatch(Runnable runnable) {
        mDispatcher.dispatch(runnable);
    }

    public static Config getConfig() {
        return UpdateManager.getInstance().mConfig;
    }

    public static UpdateManager getInstance() {
        if (singleton == null) {
            synchronized (UpdateManager.class) {
                if (singleton == null) {
                    singleton = new UpdateManager();
                }
            }
        }
        return singleton;
    }

    public AppUpdateModel getAppUpdateModel() {
        return mAppUpdateModel;
    }


    public UpdateManager init(Config config) {
        if (config == null)
            throw new IllegalArgumentException("Config can not be initialized with null");
        if (mConfig != null) {
            TraceUtil.w("have already initialized,you should call clear method first!!");
        } else {
            mConfig = config;
        }
        return this;
    }


    public void clear(IUpdateCallback callback) {
        if (callback != null)
            unRegister(callback);
        if (mUpdateDialogTarget != null && mUpdateDialogTarget.get() != null) {
            mUpdateDialogTarget.get().dismiss();
        }
    }

    public void unRegister(IUpdateCallback callback) {
        if (mListener.contains(callback))
            mListener.remove(callback);
    }

    public void destroy(Context context) {
        mListener.clear();
        clear(null);
        mConfig = null;
        sendCancel2Service(context);
    }

    public void register(IUpdateCallback callback) {
        if (!mListener.contains(callback)) {
            mListener.add(callback);
        }
    }

    /************   分割线   ************/

    /**
     * 发起更新
     *
     * @param activity
     * @param updateInfoUrl 自动更新的清单文件url地址
     * @param callback      更新情况反馈的回调接口,需要回调则传入并自行做相应动作,可不传
     */
    public void update(Activity activity, String updateInfoUrl, final IUpdateCallback callback) {
        if (mConfig == null)
            throw new RuntimeException("you should initialize Config first");

        if (mConfig.isOnlyWifi() && !SystemUtils.isWifi(activity))
            return;

        mAppVersionCode = SystemUtils.getAppVersionCode(activity);
        mActivityTarget = new WeakReference<>(activity);
        mUpdateInfoUrl = updateInfoUrl;

        if (mDispatcher == null)
            mDispatcher = new Dispatcher();

        if (callback != null && !mListener.contains(callback))
            mListener.add(callback);
        if (TextUtils.isEmpty(mUpdateInfoUrl))
            throw new IllegalArgumentException("updateInfoUrl can't be empty!");
        if (isRunning) {
            TraceUtil.i("the updating task is running now");
            return;
        }
        getHttpManager().asyncGet(mUpdateInfoUrl, null, new IHttpManager.Callback() {
            @Override
            public void onRequest(IRequest request) {

            }

            @Override
            public void onResponse(String result) {
                JsonObject jsonObject = new JsonParser().parse(result).getAsJsonObject().get("patchInfo").getAsJsonObject();
                Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                mAppUpdateModel = gson.fromJson(result, AppUpdateModel.class);

                HashMap<String, AppUpdateModel.PatchInfoModel> map;
                mAppUpdateModel.setPatchInfoMap((HashMap<String, AppUpdateModel.PatchInfoModel>)
                        gson.fromJson(jsonObject.toString(), new TypeToken<HashMap<String, AppUpdateModel.PatchInfoModel>>() {
                        }.getType()));
                mDispatcher.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        checkUpdate(mAppUpdateModel);
                    }
                });
            }

            @Override
            public void onError(final String error) {
                mDispatcher.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        for (IUpdateCallback iUpdateCallback : UpdateManager.getInstance().mListener) {
                            iUpdateCallback.onError(error);
                        }
                    }
                });
            }
        });
    }

    private void checkUpdate(AppUpdateModel appUpdateModel) {
        boolean isForceUpdate = false;
        int forceVersion = appUpdateModel.getMinVersion();
        int minPatchVersion = appUpdateModel.getMinAllowPatchVersion();
        int newestVersion = appUpdateModel.getNewVersion();
        if (mAppVersionCode >= newestVersion) {
            noNewApp();
            return;
        }
        if (mAppVersionCode < forceVersion) {
            isForceUpdate = true;
        }
        String tip = appUpdateModel.getTip();
        int updateMethod = FLAG_UPDATE_PATCH; // 差量更新
        if (mAppVersionCode < minPatchVersion) {
            //  使用全量更新
            updateMethod = FLAG_UPDATE_ALL;
        }
        beforeUpdate();
        if (mActivityTarget.get() != null)
            showUpdateDialog(isForceUpdate, updateMethod, tip);
    }

    private void showUpdateDialog(final boolean isForceUpdate, final int method, String tip) {
        UpdateDialog updateDialog = UpdateDialog.createDialog(mActivityTarget.get());
        updateDialog.setCallBack(new UpdateDialog.CallBack() {

            @Override
            public void onIgnored(View view) {
                // TODO: 2018/8/31 忽略此版本更新,不再提示此版本更新
            }

            @Override
            public void onOK(View view) {
                mUpdateDialogTarget.get().setOKBtnEnable(false);
                mUpdateDialogTarget.get().showProgressBar(true);
                mUpdateDialogTarget.get().showIgnoreTextView(false);
                startUpdate(0, method);
                isRunning = true;
                mUpdateDialogTarget.get().setUpdating(true);
//                        showUpdatingDialog();
            }

            @Override
            public void onClosed(View view) {
                if (mUpdateDialogTarget.get().isUpdating()) {
                    notifyFlag = FLAG_NOTIFY_BACKGROUND;
                    onBackgroundTrigger();
                }
            }
        });
        if (isForceUpdate) {
            updateDialog.setCancelable(false);
            updateDialog.showCloseLLT(false);
        } else {
            updateDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (mUpdateDialogTarget.get().isUpdating()) {
                        notifyFlag = FLAG_NOTIFY_BACKGROUND;
                        onBackgroundTrigger();
                    }
                }
            });
        }
        updateDialog.setUpdateTitle(String.format(Locale.CHINA, "是否升级到v%s版本?", mAppUpdateModel.getNewVersion()));
        updateDialog.setText(tip);
        updateDialog.show();

        mUpdateDialogTarget = new WeakReference<>(updateDialog);

    }

    private void sendCancel2Service(Context context) {
        Intent intent = new Intent(context, UpdateService.class);
        intent.putExtra(INTENT_ACTION, ACTION_CACEL);
        context.startService(intent);
    }

    /**
     * @param showFlag 0：前台通知下载，1：后台下载
     * @param method   0：全量更新，1：增量更新
     */
    private void startUpdate(int showFlag, int method) {
        Intent intent = new Intent(mActivityTarget.get(), UpdateService.class);
        intent.putExtra(INTENT_ACTION, ACTION_UPDATE);
        intent.putExtra(PARAM_SHOWFLAG, showFlag);
        intent.putExtra(PARAM_UPDATEMETHODFLAG, method);
        intent.putExtra(PARAM_ICONRES, android.R.drawable.btn_star);
//        intent.putExtra(UPDATE_PARAM_MODEL, mAppUpdateModel);
        mActivityTarget.get().startService(intent);
    }

    /************   分割线   ************/

    void noNewApp() {
        mDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {

                for (IUpdateCallback iUpdateCallback : UpdateManager.getInstance().mListener) {
                    iUpdateCallback.noNewApp();
                }
                isRunning = false;
            }
        });
    }

    void cancelUpdate() {
        mDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                if (UpdateManager.getInstance().mListener.size() == 0
                        && mActivityTarget.get() != null) {
                    ToastUtil.toast(mActivityTarget.get(), "下次启动还可继续更新...");
                } else {
                    for (IUpdateCallback iUpdateCallback : UpdateManager.getInstance().mListener) {
                        iUpdateCallback.onCancelUpdate();
                    }
                }
                isRunning = false;
            }
        });
    }

    void onBackgroundTrigger() {
        if (UpdateManager.getInstance().mListener.size() == 0
                && mActivityTarget.get() != null) {
            ToastUtil.toast(mActivityTarget.get(), "更新程序后台进行,可在通知栏查看进度");
        } else {
            for (IUpdateCallback iUpdateCallback : UpdateManager.getInstance().mListener) {
                iUpdateCallback.onBackgroundTrigger();
            }
        }
    }

    void beforeUpdate() {
        mDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                TraceUtil.d("更新开始");
                for (IUpdateCallback iUpdateCallback : UpdateManager.getInstance().mListener) {
                    iUpdateCallback.beforeUpdate();
                }
            }
        });
    }

    void onProgress(final int percent, final long totalLength, final int patchIndex, final int patchCount) {
        mDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                String tip;
                if (patchCount > 0) {
                    tip = String.format("正在下载补丁%d/%d", patchIndex, patchCount);
                } else {
                    tip = "正在下载更新中...";
                }
                if (mActivityTarget.get() != null) {
                    mUpdateDialogTarget.get().setProgress(percent);
                    mUpdateDialogTarget.get().setText(tip);
                }
                for (IUpdateCallback iUpdateCallback : UpdateManager.getInstance().mListener) {
                    iUpdateCallback.onProgress(percent, totalLength, patchIndex, patchCount);
                }
            }
        });
    }

    void onCompleted() {
        mDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                if (mUpdateDialogTarget.get() != null)
                    mUpdateDialogTarget.get().dismiss();
                mUpdateDialogTarget = null;
                for (IUpdateCallback iUpdateCallback : UpdateManager.getInstance().mListener) {
                    iUpdateCallback.onCompleted();
                }
                isRunning = false;
            }
        });
    }

    void onError(final String error) {
        mDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                if (mUpdateDialogTarget.get() != null)
                    mUpdateDialogTarget.get().dismiss();
                mUpdateDialogTarget = null;
                if (UpdateManager.getInstance().mListener.size() == 0
                        && mActivityTarget.get() != null) {
                    ToastUtil.toast(mActivityTarget.get(), error);
                } else {
                    for (IUpdateCallback iUpdateCallback : UpdateManager.getInstance().mListener) {
                        iUpdateCallback.onError(error);
                    }
                }
                isRunning = false;
            }
        });
    }

}
