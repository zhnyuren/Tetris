package com.flynnz.tetris;

import android.util.Log;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by FlynnZ on 2017/7/6.
 * 生成7种方块 (Tetromino)
 */

public class Tetromino {

    //当前方块的形状
    private int shape;

    //坐标和颜色
    private int x,y,color;

    public Tetromino() {}

    public int getShape() {
        return shape;
    }

    /**
     * 通过列表来返回每一种方块
     */
    public ArrayList<OneGrid> getTetromino(int x,int y) {
        this.x=x;
        this.y=y;

        //存储方块的列表
        ArrayList<OneGrid> each=new ArrayList<>();
        each.clear();

        //通过随机数生成颜色
        color=(int) (Math.random()*GameParam.TETROMINO_COLOR)+1;

        //通过随机数生成形状
        shape=(int) (Math.random()*GameParam.TETROMINO_SHAPE)+1;

        //通过随机生成的数字来随机返回方块
        switch(shape) {
            case 1: //I形
                for(int i=0;i<=3;i++) {
                    each.add(new OneGrid(color,x+(i-2)*GameParam.GRID_SIZE,y));
                }
                return each;
            case 2: //J形
                each.add(new OneGrid(color,x-GameParam.GRID_SIZE,y-GameParam.GRID_SIZE));
                for(int i=0;i<=2;i++) {
                    each.add(new OneGrid(color,x+(i-1)*GameParam.GRID_SIZE,y));
                }
                return each;
            case 3: //L形
                each.add(new OneGrid(color,x+GameParam.GRID_SIZE,y-GameParam.GRID_SIZE));
                for(int i=0;i<=2;i++) {
                    each.add(new OneGrid(color,x+(i-1)*GameParam.GRID_SIZE,y));
                }
                return each;
            case 4: //O形
                for(int i=0;i<=1;i++) {
                    each.add(new OneGrid(color,x+(i-1)*GameParam.GRID_SIZE,y));
                    each.add(new OneGrid(color,x+(i-1)*GameParam.GRID_SIZE,y-GameParam.GRID_SIZE));
                }
                return each;
            case 5: //S形
                for (int i=0;i<=1;i++) {
                    each.add(new OneGrid(color,x+i*GameParam.GRID_SIZE,y));
                    each.add(new OneGrid(color,x+(i-1)*GameParam.GRID_SIZE,y-GameParam.GRID_SIZE));
                }
                return each;
            case 6: //Z形
                for (int i=0;i<=1;i++) {
                    each.add(new OneGrid(color,x+i*GameParam.GRID_SIZE,y-GameParam.GRID_SIZE));
                    each.add(new OneGrid(color,x+(i-1)*GameParam.GRID_SIZE,y));
                }
                return each;
            case 7: //T形
                each.add(new OneGrid(color,x,y-GameParam.GRID_SIZE));
                for (int i=0;i<3;i++) {
                    each.add(new OneGrid(color,x+(i-1)*GameParam.GRID_SIZE,y));
                }
                return each;
            case 8: //炸弹 TODO
                each.add(new OneGrid(0,x,y));
                return each;
            default:
                Log.w(TAG, "getTetromino: unwilling random number exception");
                return each;
        }
    }
}
