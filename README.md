## APP自动增量更新

抽取的Android自动更新库,目的是几行代码引入更新功能,欢迎Star，欢迎Fork，谢谢～


## 目录

* [功能介绍](#功能介绍)
* [流程图](#流程图)
* [效果图与示例apk](#效果图与示例apk)
* [如何引入](#如何引入)
* [更新清单文件](#更新清单文件)
* [简单使用](#简单使用)
* [详细说明](#详细说明)
* [差分包生成(服务端)](#差分包生成(服务端))
* [依赖](#依赖)
* [License](#License)


## 功能介绍

- [x] 支持全量更新apk,直接升级到最新版本
- [x] 支持增量更新,只下载补丁包升级
- [x] 设置仅在wifi环境下更新
- [x] 支持外部注入网络框架(库默认使用okhttp)
- [x] 支持前台或后台自动更新
- [x] 支持基于版本的强制更新
- [ ] 支持对外定制提示界面
- [ ] 支持暂停、多线程断点下载
- [x] 含发布功能后台服务端[github](https://github.com/itlwy/App-Update-Server) (Node.js实现)

## 流程图
<img src="https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/flowchart.jpg" width = 80% height = 50% />

## 效果图与示例apk

![示例1](https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/update_1.png) 
![示例2](https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/update_2.png)

[点击下载 smart_update.apk](https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/app/smart_update_old.apk) 

## 如何引入
### Gradle引入
### step 1
Add the JitPack repository to your build file

```
	allprojects {
			repositories {
				...
				maven { url 'https://jitpack.io' }
			}
		}
```

### Step 2
Add the dependency

```
dependencies {
	         implementation 'com.github.itlwy:AppSmartUpdate:v1.0.+'
	}

```

## 更新清单文件
该清单放置在静态服务器以供App访问，主要用于判断最新的版本，及要更新的版本资源信息等(示例见仓库根目录下的resources目录或直接访问后台代码 [github](https://github.com/itlwy/App-Update-Server))，清单由服务端程序发布apk时生成，详见后台示例:[github](https://github.com/itlwy/App-Update-Server)

```javascript
{
  "minVersion": "100", // app最低支持的版本代码(包含),低于此数值的app将强制更新
  "minAllowPatchVersion": "100", // 最低支持的差分版本(包含),低于此数值的app将采取全量更新,否则采用差量
  "newVersion": "101", // 当前最新版本代码
  "tip": "测试更新",	// 更新提示
  "size": 2036177,	// 最新apk文件大小
  "apkURL": "https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/app/smart_update.apk", // 最新apk url地址
  "hash": "9f60c46f29299d8922a72ebfb6bab8ee", // 最新apk文件的md5值
  "patchInfo": {  // 差分包信息
    "v100": { // v100表示-版本代码100的apk需要下载的差分包
      "patchURL": "https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/app/v100/100to101.patch", //差分包地址
      "tip": "测试", // 提示
      "hash": "9f60c46f29299d8922a72ebfb6bab8ee", // 合成后apk(即版本代码101)的文件md5值
      "size": 1262068 // 差分包大小
    }
  }
}
```

## 简单使用
### 1.初始化
```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //推荐在Application中初始化
        Config config = new Config.Builder()
                .isDebug(true)
                .build(this);
        UpdateManager.getInstance().init(config);
    }
}
```

### 2.调用

```java
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
}
```

## 详细说明
### 注册通知回调
- 其他activity界面需要获知后台更新情况

```java
public void register(IUpdateCallback callback) {...}

public void unRegister(IUpdateCallback callback) {...}

public interface IUpdateCallback {

    /**
     * 通知无新版本需要更新,运行在主线程
     */
    void noNewApp();

    /**
     * 自动更新准备开始时回调,运行在主线程，可做一些提示等
     */
    void beforeUpdate();

    /**
     * 自动更新的进度回调（分增量和全量更新）,运行在主线程
     *
     * @param percent     当前总进度百分比
     * @param totalLength 更新总大小(全量为apk大小,增量为全部补丁大小和)
     * @param patchIndex  当前更新的补丁索引(从1开始)
     * @param patchCount  需要更新的总补丁数(当为0时表示是增量更新)
     */
    void onProgress(int percent, long totalLength, int patchIndex, int patchCount);

    /**
     * 下载完成，准备更新,运行在主线程
     */
    void onCompleted();

    /**
     * 异常回调,运行在主线程
     *
     * @param error 异常信息
     */
    void onError(String error);

    /**
     * 用户取消了询问更新对话框
     */
    void onCancelUpdate();

    /**
     * 取消了更新进度对话框,压入后台自动更新,此时由通知栏通知进度
     */
    void onBackgroundTrigger();
}
```

### 网络框架注入
默认使用okhttp，也可由外部注入,只需实现如下的IHttpManager接口,然后通过new Config.Builder().httpManager(new OkhttpManager())注入即可

```java
public interface IHttpManager {


    IResponse syncGet(@NonNull String url, @NonNull Map<String, String> params) throws IOException;

    /**
     * 异步get
     *
     * @param url      get请求地址
     * @param params   get参数
     * @param callBack 回调
     */
    void asyncGet(@NonNull String url, @NonNull Map<String, String> params, @NonNull Callback callBack);


    /**
     * 异步post
     *
     * @param url      post请求地址
     * @param params   post请求参数
     * @param callBack 回调
     */
    void asyncPost(@NonNull String url, @NonNull Map<String, String> params, @NonNull Callback callBack);

    /**
     * 下载
     *
     * @param url      下载地址
     * @param path     文件保存路径
     * @param fileName 文件名称
     * @param callback 回调
     */
    void download(@NonNull String url, @NonNull String path, @NonNull String fileName, @NonNull FileCallback callback);
}
```



### 差分包合成(jni)

​	此部分采用的差分工具为开源[bsdiff](http://www.daemonology.net/bsdiff/)，用于生成.patch补丁文件，采用jni方式封装一个.so库供java调用，详见"smartupdate"库里的main/cpp目录源码，过程比较简单，就是写个jni的方法来直接调用bsdiff库，目录结构如下：

main

​	-cpp

​		-bzip2

​		-CMakeLists.txt

​		-patchUtils.c

​		-patchUtils.h

​		-update-lib.cpp

​	因为bsdiff还依赖了bzip2，所以这里涉及多个源文件编译链接问题，需要在CMakeLists.txt稍作修改：

```
# 将当前 "./src/main/cpp" 目录下的所有源文件保存到 "NATIVE_SRC" 中，然后在 add_library 方法调用。
aux_source_directory( . NATIVE_SRC )
# 将 "./src/main/cpp/bzip2" 目录下的子目录bzip2保存到 "BZIP2_BASE" 中，然后在 add_library 方法调用。
aux_source_directory( ./bzip2 BZIP2_BASE )
# 将 BZIP2_BASE 增加到 NATIVE_SRC 中，这样目录的源文件也加入了编译列表中，当然也可以不加到 NATIVE_SRC，直接调用add_library。
list(APPEND NATIVE_SRC ${BZIP2_BASE})

add_library( # Sets the name of the library.
        update-lib
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        ${NATIVE_SRC})
```



## 差分包生成(服务端)

​	服务端见[github](https://github.com/itlwy/App-Update-Server) ，使用时将manifestJsonUrl改成部署的服务器地址即可，如下示例代码片段的注释处

```java
public class MainActivity extends AppCompatActivity {
    private String manifestJsonUrl = "https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/UpdateManifest.json";
//    private String manifestJsonUrl = "http://192.168.2.107:8000/app/UpdateManifest.json";
    ...
}
```



## 依赖
- okhttp : com.squareup.okhttp3:okhttp:3.11.0
- gson : com.google.code.gson:gson:2.8.0
- numberprogressbar : com.daimajia.numberprogressbar:library:1.4@aar

## License

   	Copyright 2018 lwy

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
