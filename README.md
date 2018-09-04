## APP自动增量更新

## 目录

* [功能介绍](#功能介绍)
* [流程图](#流程图)
* [效果图与示例](#效果图与示例)
* [如何引入](#如何引入)
* [简单使用](#简单使用)
* [详细说明](#详细说明)


## 功能介绍

- [x] 支持全量更新apk,直接升级到最新版本
- [x] 支持增量更新,只下载补丁包升级
- [x] 设置仅在wifi环境下更新
- [x] 支持外部注入网络框架(库默认使用okhttp)
- [x] 支持前台和后台自动更新
- [ ] 增加智能识别wifi环境自动下载功能
- [ ] 支持对外定制提示界面
- [ ] 支持暂停、多线程断点下载

## 流程图
<img src="https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/flowchart.jpg" width = 80% height = 50% />

## 效果图与示例

![示例1](https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/update_1.png?raw=true)![示例1](https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/update_2.png?raw=true)
![示例1](https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/update_3.png?raw=true)![示例1](https://raw.githubusercontent.com/itlwy/AppSmartUpdate/master/resources/update_4.png?raw=true)


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
	         implementation 'com.github.itlwy:AppSmartUpdate:v1.0.0'
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
