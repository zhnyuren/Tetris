package com.flynnz.tetris;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by FlynnZ on 2017/7/5.
 * 主游戏界面
 */

public class TetrisView extends View {
    //正在下落的方块，用列表表示
    private ArrayList<OneGrid> tetrominoNow=new ArrayList<>();

    //下一个要显示的方块，用列表表示
    private ArrayList<OneGrid> tetrominoNext=new ArrayList<>();

    //用于旋转操作判断是否可以旋转的临时方块，用列表表示
    private ArrayList<OneGrid> rotateForm=new ArrayList<>();

    //当前已经在存在的静态的格子，用列表存储
    private ArrayList<OneGrid> existedGrids=new ArrayList<>();

    //主游戏界面画笔
    private static Paint paintGame=null;

    //方块的画笔
    private static Paint paintTetromino=null;

    //主游戏界面画笔宽度
    private static final int PAINT_WIDTH=2;

    //主游戏界面最大宽度和高度
    private static int MAX_X=0;
    private static int MAX_Y=0;

    //主游戏界面单元格列数和行数
    private static int NUM_COL=0;
    private static int NUM_ROW=0;

    //行数列数
    private static int NUM_X=0;
    private static int NUM_Y=0;

    //爆炸点的坐标
    private int boomX=0;
    private int boomY=0;

    //生成的方块
    private Tetromino tetromino;

    //当前方块的形状
    private int tetromino_shape=0;

    //方块(Tetromino)的中心坐标
    private int center_x;
    private int center_y;

    //记录当前每一行存在多少格子，用于判断消除
    private int[] numEveryLine=new int[70];

    //主活动对象，用于在主线程中设置改写分数、等级等信息
    private Tetris mainActivity=null;

    //游戏线程
    private Thread gameThread=null;
    
    //方块所使用的颜色，存在数组中
    private static final int colorForTetromino[]=
            {
                    Color.parseColor("#000000"),
                    Color.parseColor("#FA3939"),
                    Color.parseColor("#9230E2"),
                    Color.parseColor("#6CCE34"),
                    Color.parseColor("#3FC4FF")
            };

    //处理加速下滑的参数
    //暂时存储当前速度
    private int tmpSpeed=0;
    //是否已经处于加速状态
    private boolean alreadySpeeded=false;

    //处理游戏中的动态参数
    //当前得分
    private int score=0;
    //当前等级
    private int level=1;
    //当前等级的分数标杆
    private int scoreOfLevel=700;
    //升级所需要的分数
    private final int SCORE_LEVEL=700;
    //游戏是否结束当前线程
    private boolean threadWorking=true;
    //游戏暂停标记
    private boolean isPaused=false;
    //添加爆炸效果标记
    private boolean showTheBoom=false;
    //是否进入最终等级标记
    private boolean isFinalLevel=false;
    //判定第一次设置行列
    private boolean firstRowCol=true;


    /**
     * 构造函数
     * 必须重载这个和下一个构造函数才能通过引用的方式
     * attrs.xml需要在values文件夹下，否则会崩溃
     */
    public TetrisView(Context context) {
        super(context);
    }

    public TetrisView(Context context,AttributeSet attrs) {
        super(context,attrs);
        //初始化方块、下一个方块、每行的数组要清空
        tetromino=new Tetromino();
        rotateForm=tetromino.getTetromino(GameParam.BEGIN_POS,GameParam.BEGIN_POS);
        for(int i=0;i<numEveryLine.length;i++) numEveryLine[i]=0;

        //初始化画方块的笔
        if(paintTetromino==null) paintTetromino=new Paint();

        //初始化画墙的笔
        if(paintGame==null) {
            paintGame=new Paint();
            paintGame.setStyle(Paint.Style.STROKE);
            paintGame.setStrokeWidth(PAINT_WIDTH+1);
            paintGame.setColor(Color.parseColor("#FFFFFF"));
        }
    }

    public TetrisView(Context context,AttributeSet attrs,int defStyle) {
        super(context,attrs,defStyle);
    }

    public static int getNumCol() {
        return NUM_COL;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    /**
     * 游戏线程
     */
    private class GameThread implements Runnable {
        @Override
        public void run() {
            while(threadWorking) { //游戏线程
                while(!isPaused) { //处理暂停和继续
                    //可以下落
                    if(OneGrid.judgeDown(tetrominoNow,existedGrids,MAX_Y)) {
                        //调用函数使得4个格子都下落
                        OneGrid.gridDown(tetrominoNow);
                        //更新块的中心纵坐标
                        center_y+=GameParam.GRID_SIZE;
                    }
                    //不可以下落
                    else {

                        //添加触底音效
                        if(tetromino_shape==8) mainActivity.playSoundBoom();
                        else mainActivity.playSoundBottom();

                        try {
                            Thread.sleep(GameParam.DELAY); //TODO 是否有乘二
                        } catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                        //再次判断，防止误差出现
                        if(OneGrid.judgeDown(tetrominoNow,existedGrids,MAX_Y)) {
                            //调用函数使得4个格子都下落
                            OneGrid.gridDown(tetrominoNow);
                            //更新块的中心纵坐标
                            center_y+=GameParam.GRID_SIZE;
                            continue;
                        }

                        //对落下的块进行处理
                        //添加下落的格子到静态格子中
                        for (OneGrid each : tetrominoNow) {
                            //each.y+=GameParam.GRID_SIZE; TODO
                            existedGrids.add(each);
                        }
                        //更新每行格子的个数
                        for (OneGrid each : tetrominoNow) {
                            if((each.y-GameParam.BEGIN_POS)/GameParam.GRID_SIZE>=0 &&
                                    (each.y-GameParam.BEGIN_POS)/GameParam.GRID_SIZE<70) {
                                numEveryLine[(each.y-GameParam.BEGIN_POS)/GameParam.GRID_SIZE]++;
                            }
                            else Log.e("Array","Out of bound");
                        }

                        //处理加速下滑问题，还原被加速的速度参数
                        if(alreadySpeeded) {
                            alreadySpeeded=false;
                            GameParam.DELAY=tmpSpeed;
                        }

                        //处理炸弹，炸掉炸弹周围的格子
                        if(tetromino_shape==8) {
                            for(int i=existedGrids.size()-1;i>=0;i--) {
                                if((existedGrids.get(i).x==center_x && existedGrids.get(i).y==center_y+GameParam.GRID_SIZE) ||
                                        (existedGrids.get(i).x==center_x && existedGrids.get(i).y==center_y) ||
                                        (existedGrids.get(i).x==center_x+GameParam.GRID_SIZE && existedGrids.get(i).y==center_y-GameParam.GRID_SIZE) ||
                                        (existedGrids.get(i).x==center_x && existedGrids.get(i).y==center_y-GameParam.GRID_SIZE) ||
                                        (existedGrids.get(i).x==center_x-GameParam.GRID_SIZE && existedGrids.get(i).y==center_y-GameParam.GRID_SIZE) ||
                                        (existedGrids.get(i).x==center_x-GameParam.GRID_SIZE && existedGrids.get(i).y==center_y) ||
                                        (existedGrids.get(i).x==center_x+GameParam.GRID_SIZE && existedGrids.get(i).y==center_y) ||
                                        (existedGrids.get(i).x==center_x-GameParam.GRID_SIZE && existedGrids.get(i).y==center_y+GameParam.GRID_SIZE) ||
                                        (existedGrids.get(i).x==center_x+GameParam.GRID_SIZE && existedGrids.get(i).y==center_y+GameParam.GRID_SIZE)) {

                                    //炸掉这个格子以及炸弹自己
                                    if((existedGrids.get(i).y-GameParam.BEGIN_POS)/GameParam.GRID_SIZE>=0 &&
                                            (existedGrids.get(i).y-GameParam.BEGIN_POS)/GameParam.GRID_SIZE<=70) {
                                        numEveryLine[(existedGrids.get(i).y-GameParam.BEGIN_POS)/GameParam.GRID_SIZE]--;
                                    }
                                    else Log.e("Array","Out of bound again");

                                    existedGrids.remove(i);
                                    //Log.v("once","once");
                                }
                            }

                            //添加爆炸效果
                            showTheBoom=true;

                            //更新爆炸点坐标
                            boomX=(center_x==GameParam.BEGIN_POS ? center_x : center_x-GameParam.GRID_SIZE);
                            boomY=center_y-2*GameParam.GRID_SIZE;
                        }


                        //删除满行
                        removeFullLine();

                        //test
                        for(int i=0;i<22;i++) {
                            Log.v(i+" level",""+numEveryLine[i]);
                        }
                        //test

                        //更新游戏线程
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //刷新分数
                                score+=10;
                                //更新记分牌
                                mainActivity.score.setText(score+"");

                                //刷新等级
                                if (GameParam.DELAY>GameParam.MAX_SPEED && score>=scoreOfLevel) {
                                    //刷新等级
                                    level++;
                                    //刷新当前等级的分数标杆
                                    scoreOfLevel+=SCORE_LEVEL;
                                    //更新等级牌
                                    mainActivity.level.setText(level+"");
                                    //提升块的下落速度
                                    GameParam.DELAY-=100;
                                }

                                //最终等级
                                if(score>=5000 && !isFinalLevel) {
                                    isFinalLevel=false;
                                    GameParam.DELAY=100;
                                    level++;
                                    mainActivity.level.setText("Crazy");
                                }

                                if(GameParam.DELAY==100) mainActivity.level.setText("Crazy");

                                //判断游戏结束
                                if (!OneGrid.judgeContinue(existedGrids)) {

                                    //处理最高分
                                    //获取存储中的最高分
                                    int highScore=mainActivity.loadSPscore();
                                    boolean isNewHigh=false;

                                    //比较大小
                                    if(score>highScore) {
                                        //获取当前时间
                                        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss");
                                        Date curDate=new Date(System.currentTimeMillis());
                                        String timeStr=formatter.format(curDate);

                                        //获取最高分
                                        isNewHigh=true;
                                        highScore=score;
                                        mainActivity.saveSP(score,timeStr);
                                    }

                                    mainActivity.playSoundgameover();
                                    Log.v("GameOver","GameOver");
                                    isPaused=true;
                                    threadWorking=false;
                                    if(gameThread!=null) gameThread.interrupt();
                                    mainActivity.runAlertDialogGameover(score,highScore,isNewHigh);
                                }

                                //获取新的块
                                getNewTetromino();

                                //重画
                                invalidate();

                            }
                        });
                    }

                    //更新游戏线程
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //重画
                            invalidate();
                        }
                    });
                    try {
                        Thread.sleep(GameParam.DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 绘图函数
     */
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        MAX_X=getWidth();
        MAX_Y=getHeight();

        //绘制主游戏界面
        NUM_COL=0;
        NUM_ROW=0;
        for(int i=GameParam.BEGIN_POS;i<MAX_X-GameParam.GRID_SIZE;i+=GameParam.GRID_SIZE) {
            for(int j=GameParam.BEGIN_POS;j<MAX_Y-GameParam.GRID_SIZE;j+=GameParam.GRID_SIZE) {
                //画圆角半径为ROUND_REC_RADIUS的圆角矩形
                canvas.drawRoundRect(new RectF(i,j,i+GameParam.GRID_SIZE,j+GameParam.GRID_SIZE),
                        GameParam.ROUND_REC_RADIUS,GameParam.ROUND_REC_RADIUS,paintGame);

                //通过得到的坐标生成一个格子的矩形 TODO
                RectF oneUnit=new RectF(i+PAINT_WIDTH,j+PAINT_WIDTH,i+GameParam.GRID_SIZE-PAINT_WIDTH,
                        j+GameParam.GRID_SIZE-PAINT_WIDTH);

                Paint paintWall=new Paint();
                paintWall.setColor(Color.parseColor("#F5F5F5"));
                //通过得到的矩形和画笔颜色画一个圆角为ROUND_REC_RADIUS的圆角矩形
                canvas.drawRoundRect(oneUnit,
                        GameParam.ROUND_REC_RADIUS,GameParam.ROUND_REC_RADIUS,paintWall);
                //TODO

                NUM_ROW++;//累计行数
            }
            NUM_COL++;//累计列数=15
        }
        //计算行数
        if(NUM_COL==0) Log.e("NUM_COL",""+0);
        else NUM_ROW/=NUM_COL;

        if(firstRowCol) {
            firstRowCol=false;
            NUM_X=NUM_COL;
            NUM_Y=NUM_ROW;
        }

        //绘制当前正在下落的俄罗斯方块，遍历当前正在下落的方块的每一个格子
        //绘制炸弹
        if(tetromino_shape==8) {
            for(int i=0;i<tetrominoNow.size();i++) {
                //获取某一个格子的横纵坐标
                int x=tetrominoNow.get(i).x;
                int y=tetrominoNow.get(i).y;
                //通过得到的坐标生成一个格子的矩形
                RectF oneUnit=new RectF(x+PAINT_WIDTH,y+PAINT_WIDTH,x+GameParam.GRID_SIZE-PAINT_WIDTH,
                        y+GameParam.GRID_SIZE-PAINT_WIDTH);
                //通过当前方块获取当前颜色并设置为当前格子的颜色
                paintTetromino.setColor(colorForTetromino[tetrominoNow.get(i).color]);
                //通过得到的矩形和画笔颜色画一个圆角为ROUND_REC_RADIUS的圆角矩形
                canvas.drawRoundRect(oneUnit,GameParam.GRID_SIZE/2,GameParam.GRID_SIZE/2,paintTetromino);
            }
        }
        //绘制方块
        else drawGrids(tetrominoNow,canvas);

        //绘制当前已经存在的静态的格子们，遍历每一个，并绘制这些格子
        drawGrids(existedGrids,canvas);

        //绘制爆炸效果
        if(showTheBoom) {
            showTheBoom=false;
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.boom1);
            canvas.drawBitmap(bitmap,boomX,boomY,null);
        }

        //开始游戏
        if (gameThread==null || !gameThread.isAlive()) {
            getNewTetromino();
            gameThread=new Thread(new GameThread());
            gameThread.start();
        }
    }

    /**
     * 画格子函数，遍历列表中所有的格子，并画出来
     */
    public void drawGrids(ArrayList<OneGrid> someGrids,Canvas canvas) {
        for(int i=0;i<someGrids.size();i++) {
            //获取某一个格子的横纵坐标
            int x=someGrids.get(i).x;
            int y=someGrids.get(i).y;
            //通过得到的坐标生成一个格子的矩形
            RectF oneUnit=new RectF(x+PAINT_WIDTH,y+PAINT_WIDTH,x+GameParam.GRID_SIZE-PAINT_WIDTH,
                    y+GameParam.GRID_SIZE-PAINT_WIDTH);
            //通过当前方块获取当前颜色并设置为当前格子的颜色
            paintTetromino.setColor(colorForTetromino[someGrids.get(i).color]);
            //通过得到的矩形和画笔颜色画一个圆角为ROUND_REC_RADIUS的圆角矩形
            canvas.drawRoundRect(oneUnit,
                    GameParam.ROUND_REC_RADIUS,GameParam.ROUND_REC_RADIUS,paintTetromino);
        }
    }


    /**
     * 控制函数
     */

    /**
     * 向左滑动，被主活动的goLeft调用
     * 调用一个格子(OneGrid)的gridToLeft
     * 完成每个格子的向左移动
     */
    public void goLeft() {
        if(!isPaused && GameParam.DELAY!=GameParam.ACCELERATE
                && OneGrid.gridToLeft(tetrominoNow,existedGrids)
                && OneGrid.judgeDown(tetrominoNow,existedGrids,MAX_Y)) {
            center_x-=GameParam.GRID_SIZE;
            //方块的中心坐标向左移动一个格子
        }
        //重画
        invalidate();
    }

    /**
     * 向右滑动，被主活动的goRight调用
     * 调用一个格子(OneGrid)的gridToRight
     * 完成每个格子的向右移动
     */
    public void goRight() {
        if(!isPaused && GameParam.DELAY!=GameParam.ACCELERATE
                && OneGrid.gridToRight(tetrominoNow,existedGrids,MAX_X)
                && OneGrid.judgeDown(tetrominoNow,existedGrids,MAX_Y)) {
            center_x+=GameParam.GRID_SIZE;
            //方块的中心坐标向右移动一个格子
        }
        //重画
        invalidate();
    }

    /**
     * 加速下滑，被主活动的goBottom调用
     * 直接修改GameParam中的游戏参数SPEED
     */
    public void goBottom() {
        if(!isPaused) {
            //已经处于加速下滑状态就返回，不作响应
            if(GameParam.DELAY==GameParam.ACCELERATE) return;
            //把当前速度暂存在tmp变量中
            tmpSpeed=GameParam.DELAY;
            //下滑速度变更为100
            GameParam.DELAY=GameParam.ACCELERATE;
            //修改加速标记
            alreadySpeeded=true;
        }
    }

    /**
     * 旋转函数，被主活动的goRotate调用
     * 先进行预旋转，可行则旋转当前下落的块
     */
    public void goRotate() {
        if(!isPaused && GameParam.DELAY!=GameParam.ACCELERATE
                && OneGrid.judgeDown(tetrominoNow,existedGrids,MAX_Y)) {
            //临时的坐标，用于保存旋转前的坐标，以生成旋转后的坐标
            int tmpCenter_x;
            int tmpCenter_y;

            if(tetromino_shape==4 || tetromino_shape==8) return;//O形和炸弹不旋转

            //防止rotateForm为空，如果为空生成四个OneGrid元素作为接下来修改的元素
            if(rotateForm.size()!=4) {
                for(int i=0;i<4;i++) rotateForm.add(new OneGrid(0,0,0));
            }

            //获取当前下落块的四个格子的坐标，用于预旋转
            for(int i=0;i<tetrominoNow.size();i++) {
                rotateForm.get(i).x=tetrominoNow.get(i).x;//TODO 数组越界
                rotateForm.get(i).y=tetrominoNow.get(i).y;
            }

            //旋转临时生成的块，对于临时块的四个格子，更改每个格子的坐标
            //以(center_x,center_y)为轴进行旋转
            for(OneGrid each : rotateForm) {
                tmpCenter_x=each.x;
                tmpCenter_y=each.y;
                each.x=center_y+center_x-tmpCenter_y;
                each.y=center_y-center_x+tmpCenter_x;
            }
            //旋转之后有可能出界，因此出界要把块移动回来
            overLeftMargin(rotateForm);
            overRightMargin(rotateForm);
            //判断旋转后是否和重叠，重叠则直接返回
            if(!OneGrid.judgeRotate(rotateForm,existedGrids)) return;

            //到这里说明预旋转成功，则真的旋转当前正在下落的块
            //重复上面的步骤以旋转该块
            for(OneGrid each : tetrominoNow) {
                tmpCenter_x=each.x;
                tmpCenter_y=each.y;
                each.x=center_y+center_x-tmpCenter_y;
                each.y=center_y-center_x+tmpCenter_x;
            }
            overLeftMargin(tetrominoNow);
            overRightMargin(tetrominoNow);
            invalidate();//重画
        }
    }

    /**
     * 处理旋转之后出界的函数
     */

    //处理越过右边界的情况
    public void overRightMargin(ArrayList<OneGrid> someGrids) {
        boolean should=false;
        for(OneGrid each : someGrids) {
            if(each.x+GameParam.GRID_SIZE>MAX_X) should=true;
        }
        if(should) {
            for(OneGrid each : someGrids) {
                each.x-=GameParam.GRID_SIZE;
            }
            overRightMargin(someGrids);
        }
    }
    //处理越过左边界的情况
    public void overLeftMargin(ArrayList<OneGrid> someGrids) {
        boolean should=false;
        for(OneGrid each : someGrids) {
            if(each.x<GameParam.BEGIN_POS) should=true;
        }
        if(should) {
            for(OneGrid each : someGrids) {
                each.x+=GameParam.GRID_SIZE;
            }
            overLeftMargin(someGrids);
        }
    }


    /**
     * 游戏运行状态设置
      */

    /**
     * 暂停游戏
     * 响应主活动menu中的Pause
     */
    public void gamePause() {
        isPaused=true;
    }

    /**
     * 继续游戏
     * 响应主活动menu中的continue
     */
    public void gameContinue() {
        isPaused=false;
    }

    /**
     * 结束游戏
     */
    public void gameEnd() {
        //结束线程的循环
        threadWorking=false;

        //回收线程
        if(gameThread!=null) { gameThread.interrupt(); gameThread=null;}

        //清空4个表示方块的列表
        tetrominoNow.clear();
        tetrominoNext.clear();
        rotateForm.clear();
        existedGrids.clear();

        //清空每行的格子数
        for(int i=0;i<numEveryLine.length;i++) numEveryLine[i]=0;

        //还原积分等级速度系统
        tmpSpeed=0;
        alreadySpeeded=false;
        score=0;
        level=1;
        scoreOfLevel=300;
        GameParam.DELAY=GameParam.DELAY_INIT;

        //更新记分牌
        mainActivity.score.setText(""+0);
        mainActivity.level.setText(""+1);

        //重画
        invalidate();
    }


    /**
     * 游戏运算处理函数
     */

    /**
     * 设置主活动，用于在主线程中设置改写分数、等级等信息
     */
    public void setMainActivity(Tetris tetris) {
        mainActivity=tetris;
    }

    /**
     * 获取新块，处理“正在下落”和“下一个”块
     */
    public void getNewTetromino() {
        //块的初始坐标为(width/2,0)，从中间开始下落
        this.center_x=GameParam.BEGIN_POS+GameParam.GRID_SIZE*(NUM_COL/2);
        this.center_y=GameParam.BEGIN_POS;
        //不存在正在下落的块，游戏刚开始，随机获取一个块
        if(tetrominoNext.size()==0) {
            tetrominoNext=tetromino.getTetromino(center_x,center_y);
        }
        //把“下一个”块赋给当前正在下落的块
        tetrominoNow=tetrominoNext;
        //获取当前正在下落的块的形状
        tetromino_shape=tetromino.getShape();
        //更新“下一个”块
        tetrominoNext=tetromino.getTetromino(center_x,center_y);
        //去修改右上角小视图中的块
        if(mainActivity!=null) mainActivity.setForeshowView(tetrominoNext);
    }

    /**
     * 消去满行
     */
    public void removeFullLine() {
        //遍历每一行
        Log.v("COL",""+NUM_X);
        Log.v("ROW",""+NUM_Y);
        for(int i=0;i<=NUM_Y;i++) {
            //如果这一行的格子的个数达到列数个，则进行消去
            if(numEveryLine[i]>=NUM_X) {
                Log.v("erase","success");
                //调用声效
                mainActivity.playSoundErase();

                //在包含所有格子的列表中删去行数为i的格子
                OneGrid.deleteOneLine(existedGrids,i);

                //挪动，类似于顺序表的操作，numEveryLine性上到下遍历
                for(int j=i;j>0;j--) numEveryLine[j]=numEveryLine[j-1];

                //处理存放所有格子的列表的元素的纵坐标,注意前提是纵坐标小于第i行的纵坐标
                //即在被消去行之上
                for(OneGrid each : existedGrids) {
                    if(i*GameParam.GRID_SIZE>each.y-GameParam.BEGIN_POS) {
                        each.y+=GameParam.GRID_SIZE;
                    }
                }

                //不要忘记更新分数，消去满行的奖励分数
                score+=100;
            }
        }
    }
}