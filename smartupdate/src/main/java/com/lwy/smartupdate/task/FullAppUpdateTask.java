package com.lwy.smartupdate.task;

import com.lwy.smartupdate.UpdateManager;
import com.lwy.smartupdate.api.IHttpManager;
import com.lwy.smartupdate.api.IRequest;
import com.lwy.smartupdate.data.AppUpdateModel;
import com.lwy.smartupdate.utils.SignUtils;

import java.io.File;

/**
 * @author lwy 2018/8/30
 * @version v1.0.0
 * @name FullAppUpdateTask
 * @description 全量更新逻辑执行的任务类
 */
public class FullAppUpdateTask extends AppUpdateTask {


    private AppUpdateModel mAppUpdateModel;
    private String mFileName;
    private IRequest mRequest;


    public FullAppUpdateTask(AppUpdateModel appUpdateModel) {
        mAppUpdateModel = appUpdateModel;
    }

    @Override
    protected void execute() {
        String fileUrl = mAppUpdateModel.getApkURL();
        if (!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://"))) {
            int index = mAppUpdateModel.getManifestURL().lastIndexOf("/");
            String relativeURL = mAppUpdateModel.getManifestURL().substring(0, index);
            fileUrl = relativeURL + "/" + fileUrl;
        }
        mFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        File destFile = new File(dirPath, mFileName);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        } else {
            if (destFile.exists())
                destFile.delete();
        }
        UpdateManager.getInstance().getHttpManager().download(fileUrl, dirPath, mFileName, new IHttpManager.FileCallback() {
            @Override
            public void onProgress(float percent, long total) {
                int progressInt = (int) (percent * 100);
                callBack.onProgress(progressInt, total, 0, 0);
            }

            @Override
            public void onError(String error) {
                callBack.onError(error);
            }

            @Override
            public void onResponse(File file) {
                String filePath = file.getAbsolutePath();
                if (SignUtils.checkMd5(filePath, mAppUpdateModel.getHash())) {
                    callBack.onCompleted(filePath);
                } else {
                    callBack.onError("校验下载的apk文件完整性不通过!!");
                }
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

    @Override
    public void cancel() {
        if (mRequest != null)
            mRequest.cancel();
    }
}
