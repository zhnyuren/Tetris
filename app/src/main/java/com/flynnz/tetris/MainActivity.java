package com.flynnz.tetris;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {

    private SoundPool soundPoolWelcome;//声明一个SoundPool
    private int soundIDWelcome;//创建某个声音对应的音频ID

    public static MediaPlayer mediaPlayer;

    private SoundPool soundPoolPause;
    private int soundIDPause;

    private MyReceiver receiver;

    /**
     * 声音控制
     */
    @SuppressLint("NewApi")
    private void initSoundWelcome() {
        soundPoolWelcome=new SoundPool.Builder().build();
        soundIDWelcome=soundPoolWelcome.load(this,R.raw.welcome,1);
    }

    private void playSoundWelcome() {
        soundPoolWelcome.play(
                soundIDWelcome,
                1,      //左耳道音量【0~1】
                1,      //右耳道音量【0~1】
                0,         //播放优先级【0表示最低优先级】
                0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
                1          //播放速度【1是正常，范围从0~2】
        );
    }

    /**
     * 背景音乐
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initSoundWelcome();
        initSoundPause();
        mediaPlayer=MediaPlayer.create(this,R.raw.background);
        mediaPlayer.start();
        mediaPlayer.setLooping(true);

        receiver = new MyReceiver();
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        registerReceiver(receiver, homeFilter);
    }

    public void enterTheMainGame() {
        Intent intent=new Intent(MainActivity.this,Tetris.class);
        startActivity(intent);
    }

    public void enterCrazyMode(View view) {
        playSoundPause();
        GameParam.DELAY=100;
        Intent intent=new Intent(MainActivity.this,Tetris.class);
        startActivity(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                playSoundWelcome();
                enterTheMainGame();
                return true;
            default:
                return true;
        }
    }

    /**
     * 显示最高分
     */
    public void runAlertDialogHighScore(View view) {
        playSoundPause();
        SharedPreferences sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.show();
        alertDialog.setCancelable(false);
        Window window = alertDialog.getWindow();
        window.setContentView(R.layout.high_score_dialog);
        TextView tv_message = (TextView) window.findViewById(R.id.high_dialog_msg);

        if(sharedPreferences.getInt("high_score",0)==0) {
            tv_message.setText("No high score");
        }
        else {
            tv_message.setText("\nScore:\n"+sharedPreferences.getInt("high_score",0)+
                    "\n\nTime:\n"+sharedPreferences.getString("date_time",""));
        }

        window.findViewById(R.id.high_dialog_ok)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        playSoundPause();
                    }
                });
    }

    /**
     * 显示提示
     */
    public void runAlertDialogTips(View view) {
        playSoundPause();
        final AlertDialog dialog =new AlertDialog.Builder(MainActivity.this).create();
        dialog.setCancelable(false);
        dialog.show();
        dialog.getWindow().setContentView(R.layout.tips_alert_dialog);
        dialog.getWindow().findViewById(R.id.tips_dialog_ok)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        playSoundPause();
                    }
                });
    }

    /**
     * 处理按钮声音
     */
    @SuppressLint("NewApi")
    private void initSoundPause() {
        soundPoolPause=new SoundPool.Builder().build();
        soundIDPause=soundPoolPause.load(this,R.raw.pause,1);
    }

    public void playSoundPause() {
        soundPoolPause.play(
                soundIDPause,
                1,         //左耳道音量【0~1】
                1,         //右耳道音量【0~1】
                0,         //播放优先级【0表示最低优先级】
                0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
                1          //播放速度【1是正常，范围从0~2】
        );
    }

    /**
     * 暂停音乐
     */
    public void musicPauseStart(View view) {
        if(mediaPlayer.isPlaying()) mediaPlayer.pause();
        else mediaPlayer.start();
    }

    /**
     * 处理home键按下
     */
    private class MyReceiver extends BroadcastReceiver {

        private final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

                if (reason==null) return;

                // Home键
                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                    //暂停音乐
                    MainActivity.mediaPlayer.pause();
                }

                // 最近任务列表键
                if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                    //暂停音乐
                    MainActivity.mediaPlayer.pause();
                }
            }
        }
    }

    /**
     * 重写onDestroy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if(mediaPlayer!=null) {
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }
}
