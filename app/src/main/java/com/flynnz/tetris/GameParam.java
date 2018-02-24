package com.flynnz.tetris;

/**
 * Created by FlynnZ on 2017/7/5.
 * 游戏参数
 */

public class GameParam {
    //运行速度
    public static int DELAY=600;

    //初始运行速度
    public static final int DELAY_INIT=600;

    //每个单元格的大小
    public static final int GRID_SIZE=60;

    //主游戏界面开始坐标
    public static final int BEGIN_POS=10;

    //方块形状的种类数量
    public static final int TETROMINO_SHAPE=8; //TODO

    //方块颜色的种类数量
    public static final int TETROMINO_COLOR=4;

    //圆角矩形的半径
    public static final int ROUND_REC_RADIUS=13;

    //加速下滑的速度
    public static final int ACCELERATE=50;

    //游戏最大速度
    public static final int MAX_SPEED=200;
}
