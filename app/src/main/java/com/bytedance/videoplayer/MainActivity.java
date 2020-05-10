package com.bytedance.videoplayer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private MediaPlayer player;
    private SurfaceHolder holder;
    private SeekBar Bar;
    private TextView currentTime;
    private TextView textAll;
    private int length;
    private Timer myTimer;     //定时刷新进度条
    private TimerTask myTask;  //定时器任务
    private boolean isChanging = false;    //互斥变量，防止定时器与seekbar滑动冲突
    //计时器进程提醒主线程更新ui
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




    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        if (intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            String str = Uri.decode(uri.getEncodedPath());}
        setTitle("MediaPlayer");
        if(MainActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            getSupportActionBar().hide();

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.surfaceView);
        Bar = findViewById(R.id.seekBar);
        currentTime = findViewById(R.id.currentTime);
        textAll = findViewById(R.id.textAll);



        player = new MediaPlayer();
        try {
            player.setDataSource(getResources().openRawResourceFd(R.raw.bytedance));
            holder = surfaceView.getHolder();
            holder.addCallback(new PlayerCallBack());
            player.prepare();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // 自动播放
                  player.start();
                    player.setLooping(true);
                }
            });
            player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    changeVideoSize(mp);
                }
            });
            player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    System.out.println(percent);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        SimpleDateFormat formatter1 = new SimpleDateFormat("mm:ss");
        length = player.getDuration();
        textAll.setText(formatter1.format(length));
        Log.d("vd", "视频长度为" + length);
        Bar.setMax(length);
        //endTime.setText();
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


        findViewById(R.id.buttonPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.start();
            }
        });

        findViewById(R.id.buttonPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
            }
        });

        //Bar.setMax();
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
    }


    public void changeVideoSize(MediaPlayer mediaPlayer) {
        int surfaceWidth = surfaceView.getWidth();
        int surfaceHeight = surfaceView.getHeight();

        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();

        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        float max;
        if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            Log.d("hengping", "竖屏了");
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max((float) videoWidth / (float) surfaceWidth, (float) videoHeight / (float) surfaceHeight);
            //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
            videoWidth = (int) Math.ceil((float) videoWidth / max);
            videoHeight = (int) Math.ceil((float) videoHeight / max);

        } else {
            //横屏模式下按视频高度计算放大倍数值
            Log.d("hengping", "横屏了");
            max = Math.max(((float) videoWidth / (float) surfaceHeight), (float) videoHeight / (float) surfaceWidth);
            float screen_widthPixels = getResources().getDisplayMetrics().widthPixels;
            float screen_heightPixels = getResources().getDisplayMetrics().widthPixels * 9f / 16f;
            videoWidth = (int)screen_widthPixels;
            videoHeight = (int)screen_heightPixels;



        }



        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        surfaceView.setLayoutParams(new LinearLayout.LayoutParams(videoWidth, videoHeight));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player == null) {
            player.stop();
            player.release();
        }
    }

    private class PlayerCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            player.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }



}
