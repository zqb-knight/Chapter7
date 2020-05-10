# Chapter7

## 一. 完成情况

完成基础和PRO版本

实现播放，暂停，进度条，横屏全屏，关联视频文件。



## 二. 测试平台

实体机：小米6 

系统版本：Android 9



## 三. How to use

- Relesse下载app-debug.apk

- 对应目录下终端运行

  **adb install -t app-debug.apk**

  

## 四. 具体功能代码实现

### 1. 播放窗口，开始暂停键

使用MediaPlayer和sufaceView实现播放。开始，暂停键使用Button实现。

代码结构和老师给的MediaPlayer的demo代码相同，这里不写了。



### 2. 进度条

使用一个SeekBar和2个TextView配合Timer实现。

seekBar表示进度，2个TextView分别表示当前播放时间和视频总时间。

**（注意：Timer线程里不能直接更改UI，因为只有主线程可以更改UI）**

​	**(注意：这里设计了一个isChanging的锁，防止定时器与seekbar滑动冲突)**

#### 定义相关变量

```java
private SeekBar Bar;               //进度条
private TextView currentTime;     //当前播放时间
private TextView textAll;         //视频总时间
private int length;
private Timer myTimer;          //定时刷新进度条
private TimerTask myTask;           //定时器任务
private boolean isChanging = false;    //互斥变量，防止定时器与seekbar滑动冲突
```

#### 计时器进程提醒主线程更新ui

更新**进度条**和**当前播放时间**

```java
Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bar.setProgress(player.getCurrentPosition());
            SimpleDateFormat formatter2 = new SimpleDateFormat("mm:ss");
            currentTime.setText(formatter2.format(player.getCurrentPosition()));
            Log.d("shijian", "run: " + player.getCurrentPosition());
        }
    };
```

#### 设置定时器和定时器任务

```java
myTimer = new Timer();
myTask = new TimerTask() {
         @Override
          public void run() {
                if(isChanging == true){
                    return;
                }
                mHandler.sendMessage(Message.obtain(mHandler, 1));
            }
 };
myTimer.schedule(myTask,0,10);
```

#### 设置视频总时间

```java
SimpleDateFormat formatter1 = new SimpleDateFormat("mm:ss");
length = player.getDuration();
textAll.setText(formatter1.format(length));
```

#### 拖动进度条改变视频播放进度

```java
//seekBar监听事件
        Bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.d("vd", "onProgressChanged: "+i);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isChanging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seekTo(Bar.getProgress());
                isChanging = false;
            }
        });
```



### 3. 横屏播放

这里我让它隐藏Bar后自动填充屏幕。

```java
if(MainActivity.this.getResources().getConfiguration().orientation == 
                Configuration.ORIENTATION_LANDSCAPE){
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
```



###4. PRO版本要求

##### 在AndroidMainfest.xml添加注册信息

```java
<intent-filter>
      <action android:name="android.intent.action.VIEW" />
      <category android:name="android.intent.category.DEFAULT" />
      <data android:scheme="content" />
       <data android:host="*"/>
       <data android:mimeType="video/*" />
</intent-filter>
```

##### 界面中接受信息并处理

```java
Intent intent = getIntent();
String action = intent.getAction();
if (intent.ACTION_VIEW.equals(action)) {
    Uri uri = intent.getData();
    String str = Uri.decode(uri.getEncodedPath());}
```