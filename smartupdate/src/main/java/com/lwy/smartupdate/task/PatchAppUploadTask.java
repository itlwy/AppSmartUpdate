package com.lwy.smartupdate.task;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

import com.lwy.smartupdate.ConstantValue;
import com.lwy.smartupdate.UpdateManager;
import com.lwy.smartupdate.api.IHttpManager;
import com.lwy.smartupdate.api.IRequest;
import com.lwy.smartupdate.data.AppUpdateModel;
import com.lwy.smartupdate.utils.ApkUtils;
import com.lwy.smartupdate.utils.FileUtils;
import com.lwy.smartupdate.utils.PatchUtils;
import com.lwy.smartupdate.utils.SignUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lwy 2018/8/30
 * @version v1.0.0
 * @name PatchAppUploadTask
 * @description 增量更新逻辑执行的任务类
 */
public class PatchAppUploadTask extends AppUpdateTask {

    private final int mAppVersion;
    private final int mNewestVersion;
    private final String mPatchSuffix = ".patch";
    private int mCurrentVersion;
    private final int mPatchCount;
    private final Context mContext;
    private Map<String, AppUpdateModel.PatchInfoModel> mInfoModelMap;
    private long mTotalLength;
    //合成得到的新版apk
    private AppUpdateModel.PatchInfoModel currentPatch;

    private List<String> patchAPKList = new ArrayList<>(); // 批量自动差分合成的结果apk集合  最后一个是最新的
    private int mCurrentPatchIndex = 0;
    private IRequest mRequest;

    public PatchAppUploadTask(Context context, int appVersion, int newestVersion, Map<String, AppUpdateModel.PatchInfoModel> infoModelMap) {
        mContext = context;
        mAppVersion = appVersion;
        mCurrentVersion = appVersion;
        mNewestVersion = newestVersion;
        mInfoModelMap = infoModelMap;
        for (AppUpdateModel.PatchInfoModel patchInfoModel : mInfoModelMap.values()) {
            mTotalLength += patchInfoModel.getSize();
        }
        mPatchCount = mInfoModelMap.size();
    }

    @Override
    protected void execute() {
        mCurrentPatchIndex = 1;
        currentPatch = mInfoModelMap.get("v" + mAppVersion);
        download(currentPatch);
    }

    private void download(final AppUpdateModel.PatchInfoModel currentPatch) {
        String fileUrl = currentPatch.getPatchURL();
        final String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1, fileUrl.length());
        File destFile = new File(dirPath, fileName);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        } else {
            if (destFile.exists())
                destFile.delete();
        }
        UpdateManager.getInstance().getHttpManager().download(fileUrl, dirPath, fileName, new IHttpManager.FileCallback() {
            @Override
            public void onProgress(float percent, long total) {
                int progressInt = (int) (percent * 100);
                callBack.onProgress(progressInt, total, mCurrentPatchIndex, mPatchCount);
            }

            @Override
            public void onError(String error) {
                callBack.onError(error);
            }

            @Override
            public void onResponse(File file) {
                patchUpdate(file.getAbsolutePath(), currentPatch.getHash());
            }

            @Override
            public void onBefore() {
            }

            @Override
            public void onRequest(IRequest request) {
                mRequest = request;
            }

        });
    }

    private void patchUpdate(String patchFilePath, String hash) {
        PackageInfo packageInfo = ApkUtils.getInstalledApkPackageInfo(mContext, mContext.getPackageName());

        if (packageInfo != null) {

            // TODO: 2018/8/30 检查本地安装的apk的md5是否正常 --防止本地apk被篡改了
            String oldApkSource;
            if (patchAPKList.size()>0) {
                oldApkSource = patchAPKList.get(patchAPKList.size()-1);
            }else{
                oldApkSource= ApkUtils.getSourceApkPath(mContext, mContext.getPackageName());
            }
            if (!TextUtils.isEmpty(oldApkSource)) {
                mCurrentVersion++;
                String newAPKPath = dirPath + mCurrentVersion + ".apk";
                int patchResult = PatchUtils.patch(oldApkSource, newAPKPath, patchFilePath);

                if (patchResult == 0) {
                    if (SignUtils.checkMd5(newAPKPath, hash)) {
                        patchAPKList.add(newAPKPath);
                        if (mCurrentVersion == mNewestVersion) {
                            // 已是最新的
                            handlePatchUpdateResult(ConstantValue.WHAT_SUCCESS);
                        } else {
                            mCurrentPatchIndex++;
                            AppUpdateModel.PatchInfoModel patchInfoModel = mInfoModelMap.get("v" + mCurrentVersion);
                            download(patchInfoModel);
                        }
                    } else {
                        handlePatchUpdateResult(ConstantValue.WHAT_FAIL_GEN_MD5);
                    }
                } else {
                    handlePatchUpdateResult(ConstantValue.WHAT_FAIL_PATCH);
                }
            } else {
                handlePatchUpdateResult(ConstantValue.WHAT_FAIL_GET_SOURCE);
            }
        } else {
            handlePatchUpdateResult(ConstantValue.WHAT_FAIL_UNKNOWN);
        }
    }

    private void handlePatchUpdateResult(int result) {
        String text = "";
        switch (result) {
            case ConstantValue.WHAT_SUCCESS: {
                String installAPKPath = patchAPKList.get(patchAPKList.size() - 1);
                callBack.onCompleted(installAPKPath);
                clearPatchFile();
                return;
            }
            case ConstantValue.WHAT_FAIL_OLD_MD5: {
                text = "现在安装的apk的MD5不对！";
                break;
            }
            case ConstantValue.WHAT_FAIL_GEN_MD5: {
                text = "合成完毕，但是合成得到的apk MD5不对！";
                break;
            }
            case ConstantValue.WHAT_FAIL_PATCH: {
                text = "新apk已合成失败！";
                break;
            }
            case ConstantValue.WHAT_FAIL_GET_SOURCE: {
                text = "无法获取客户端的源apk文件，只能整包更新了！";
                break;
            }
            case ConstantValue.WHAT_FAIL_PATCHDOWNLOAD: {
                text = "下载补丁包出错";
                break;
            }
            case ConstantValue.WHAT_FAIL_FILE_OUTPUT: {
                text = "流输出文件失败";
                break;
            }
        }
        callBack.onError(text);
    }

    private void clearPatchFile() {
        File dir = new File(dirPath);
        for (String fileName : dir.list()) {
            if (fileName.endsWith(mPatchSuffix))
                FileUtils.deleteFile(dirPath, fileName);
        }
    }

    @Override
    public void cancel() {
        if (mRequest != null)
            mRequest.cancel();
    }
}
