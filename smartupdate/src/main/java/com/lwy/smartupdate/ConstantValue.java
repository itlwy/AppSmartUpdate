package com.lwy.smartupdate;

/**
 *
 * @author lwy 2018/8/31
 * @version v1.0.0
 * @name ConstantValue
 * @description
 */
public interface ConstantValue {

    /*  自动更新开始 */
    // 成功
    int WHAT_SUCCESS = 1;

    // 本地安装的apkMD5不正确
    int WHAT_FAIL_OLD_MD5 = -1;

    // 新生成的apkMD5不正确
    int WHAT_FAIL_GEN_MD5 = -2;

    // 合成失败
    int WHAT_FAIL_PATCH = -3;

    // 获取源文件失败
    int WHAT_FAIL_GET_SOURCE = -4;

    // 未知错误
    int WHAT_FAIL_UNKNOWN = -5;
    // 下载补丁包 出错
    int WHAT_FAIL_PATCHDOWNLOAD = -6;

    // 流输出文件失败
    int WHAT_FAIL_FILE_OUTPUT = -7;

    /*  自动更新结束 */
}
