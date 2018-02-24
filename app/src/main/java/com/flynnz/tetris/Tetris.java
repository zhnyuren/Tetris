package com.flynnz.tetris;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.SoundPool;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;

public class Tetris extends AppCompatActivity {

    private ForeshowView foreshowView;
    private TetrisView tetrisView;
    public TextView score;
    public TextView level;

    private SoundPool soundPoolButton;//声明一个SoundPool
    private int soundIDbutton;//创建某个声音对应的音频ID

    private SoundPool soundPoolGameover;
    private int soundIDgameover;

    private SoundPool soundPoolErase;
    private int soundIDErase;

    private SoundPool soundPoolPause;
    private int soundIDPause;

    private SoundPool soundPoolBottom;
    private int soundIDBottom;

    private SoundPool soundPoolBoom;
    private int soundIDBoom;

    private MyReceiver receiver;

    private boolean isPaused=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tetris);
        tetrisView=(TetrisView) findViewById(R.id.main_game_view);
        tetrisView.setMainActivity(this);
        foreshowView=(ForeshowView) findViewById(R.id.foreshowView);
        score = (TextView) findViewById(R.id.score_text);
        level=(TextView) findViewById(R.id.level_text);

        initSoundButton();
        initSoundGameover();
        initSoundErase();
        initSoundPause();
        initSoundBootom();
        initSoundBoom();

        receiver = new MyReceiver();
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        registerReceiver(receiver, homeFilter);
    }

    /**
     * 游戏结束
     */
    public void runAlertDialogGameover(int _score,int _highscore,boolean _isNewHigh) {
        final AlertDialog alertDialog = new AlertDialog.Builder(Tetris.this).create();
        alertDialog.show();
        alertDialog.setCancelable(false);
        Window window = alertDialog.getWindow();
        window.setContentView(R.layout.gameover_alert_dialog);
        TextView tv_message = (TextView) window.findViewById(R.id.pause_dialog_msg);
        tv_message.setText(_isNewHigh ?
                "Your score:\n"+_score+"\n\nHigh Score!\nCongratulations!" :
                "Your score:\n"+_score+"\n\nYour high score:\n"+_highscore);
        window.findViewById(R.id.gameover_dialog_ok)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        playSoundPause();
                        finish();
                    }
                });
    }

    /**
     * 关于
     */
    public void runAlertDialogAbout() {
        final AlertDialog dialog =new AlertDialog.Builder(Tetris.this).create();
        dialog.setCancelable(false);
        dialog.show();
        dialog.getWindow().setContentView(R.layout.about_alert_dialog);
        dialog.getWindow().findViewById(R.id.about_dialog_ok)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tetrisView.gameContinue();
                        dialog.dismiss();
                        playSoundPause();
                        isPaused=false;
                    }
                });
    }

    /**
     * 暂停
     */
    public void runAlertDialogPause() {
        final AlertDialog alertDialog = new AlertDialog.Builder(Tetris.this).create();
        alertDialog.show();
        alertDialog.setCancelable(false);
        Window window = alertDialog.getWindow();
        window.setContentView(R.layout.pause_alert_dialog);
        TextView tv_message = (TextView) window.findViewById(R.id.pause_dialog_msg);
        tv_message.setText("Current score:\n"+tetrisView.getScore()+"\nCurrent level:\n"+tetrisView.getLevel());
        window.findViewById(R.id.pause_dialog_ok)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        tetrisView.gameContinue();
                        playSoundPause();
                        isPaused=false;
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tetris_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.exit_item:
                finish();
                break;
            case R.id.pause_item:
                runAlertDialogPause();
                playSoundPause();
                tetrisView.gamePause();
                isPaused=true;
                break;
            case R.id.about_item:
                runAlertDialogAbout();
                playSoundPause();
                tetrisView.gamePause();
                isPaused=true;
                break;
            case R.id.music_on_off:
                if(MainActivity.mediaPlayer.isPlaying()) MainActivity.mediaPlayer.pause();
                else MainActivity.mediaPlayer.start();
            default:
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //要记得释放线程
        if(tetrisView!=null) tetrisView.gameEnd();
        unregisterReceiver(receiver);
    }

    public void setForeshowView(ArrayList<OneGrid> someGrids) {
        foreshowView.setForeshowTetro(someGrids);
    }


    /**
     * 声音控制
     */
    @SuppressLint("NewApi")
    private void initSoundGameover() {
        soundPoolGameover=new SoundPool.Builder().build();
        soundIDgameover=soundPoolGameover.load(this,R.raw.gameover,1);
    }

    public void playSoundgameover() {
        soundPoolGameover.play(
                soundIDgameover,
                1,         //左耳道音量【0~1】
                1,         //右耳道音量【0~1】
                0,         //播放优先级【0表示最低优先级】
                0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
                1          //播放速度【1是正常，范围从0~2】
        );
    }

    @SuppressLint("NewApi")
    private void initSoundButton() {
        soundPoolButton=new SoundPool.Builder().build();
        soundIDbutton=soundPoolButton.load(this,R.raw.button,1);
    }

    private void playSoundbutton() {
        soundPoolButton.play(
                soundIDbutton,
                0.4f,         //左耳道音量【0~1】
                0.4f,         //右耳道音量【0~1】
                0,         //播放优先级【0表示最低优先级】
                0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
                1          //播放速度【1是正常，范围从0~2】
        );
    }

    @SuppressLint("NewApi")
    private void initSoundErase() {
        soundPoolErase=new SoundPool.Builder().build();
        soundIDErase=soundPoolErase.load(this,R.raw.erase,1);
    }

    public void playSoundErase() {
        soundPoolErase.play(
                soundIDErase,
                1,         //左耳道音量【0~1】
                1,         //右耳道音量【0~1】
                1,         //播放优先级【0表示最低优先级】
                0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
                1          //播放速度【1是正常，范围从0~2】
        );
    }

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

    @SuppressLint("NewApi")
    private void initSoundBootom() {
        soundPoolBottom=new SoundPool.Builder().build();
        soundIDBottom=soundPoolBottom.load(this,R.raw.bottom,1);
    }

    public void playSoundBottom() {
        soundPoolBottom.play(
                soundIDBottom,
                1,         //左耳道音量【0~1】
                1,         //右耳道音量【0~1】
                0,         //播放优先级【0表示最低优先级】
                0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
                1          //播放速度【1是正常，范围从0~2】
        );
    }

    @SuppressLint("NewApi")
    private void initSoundBoom() {
        soundPoolBoom=new SoundPool.Builder().build();
        soundIDBoom=soundPoolBoom.load(this,R.raw.boom,1);
    }

    public void playSoundBoom() {
        soundPoolBoom.play(
                soundIDBoom,
                0.6f,         //左耳道音量【0~1】
                0.6f,         //右耳道音量【0~1】
                0,         //播放优先级【0表示最低优先级】
                0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
                1          //播放速度【1是正常，范围从0~2】
        );
    }


    /**
     * Button游戏控制
     */

    /**
     * 响应按键向左
     * 调用View的goLeft
     */
    public void goLeft(View view) {
        playSoundbutton();
        tetrisView.goLeft();
    }

    /**
     * 响应按键向右
     * 调用View的goRight
     */
    public void goRight(View view) {
        playSoundbutton();
        tetrisView.goRight();
    }

    /**
     * 响应按键旋转
     * 调用View的goRotate
     */
    public void goRotate(View view) {
        playSoundbutton();
        tetrisView.goRotate();
    }

    /**
     * 响应按键加速下滑
     * 调用View的goBottom
     */
    public void goBottom(View view) {
        playSoundbutton();
        tetrisView.goBottom();
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
                    //暂停游戏
                    if(!isPaused) {
                        runAlertDialogPause();
                        tetrisView.gamePause();
                        isPaused=true;
                    }
                    //暂停音乐
                    MainActivity.mediaPlayer.pause();
                }

                // 最近任务列表键
                if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                    //暂停游戏
                    if(!isPaused) {
                        runAlertDialogPause();
                        tetrisView.gamePause();
                        isPaused=true;
                    }
                    //暂停音乐
                    MainActivity.mediaPlayer.pause();
                }
            }
        }
    }

    /**
     * 存储和读取最高分
     */
    public void saveSP(int _score,String _timeStr) {
        SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
        editor.putInt("high_score",_score);
        editor.putString("date_time",_timeStr);
        editor.apply();
    }

    public int loadSPscore() {
        SharedPreferences sharedPreferences=getSharedPreferences("data",MODE_PRIVATE);
        return sharedPreferences.getInt("high_score",0);
    }
}
