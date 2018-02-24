package com.flynnz.tetris;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by FlynnZ on 2017/7/6.
 */

public class ForeshowView extends View {

    //方块所使用的颜色，存在数组中
    private static final int colorForTetromino[]=
            {
                    Color.parseColor("#000000"),
                    Color.parseColor("#FA3939"),
                    Color.parseColor("#9230E2"),
                    Color.parseColor("#6CCE34"),
                    Color.parseColor("#3FC4FF")
            };

    //用来存储传进来的块的列表
    private ArrayList<OneGrid> foreshowTetro=new ArrayList<>();

    //预告界面画笔
    private static Paint paintGame=null;

    //方块的画笔
    private static Paint paintTetromino=null;

    //预告界面画笔宽度
    private static final int PAINT_WIDTH=2;


    public ForeshowView(Context context) {
        super(context);
    }

    public ForeshowView(Context context, AttributeSet attrs) {
        super(context,attrs);

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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF oneUnit;
        int x,y;
        if(foreshowTetro.size()==1) {
            x=foreshowTetro.get(0).x+GameParam.GRID_SIZE*(-TetrisView.getNumCol()/2);
            y=foreshowTetro.get(0).y+GameParam.GRID_SIZE;
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.bomb);
            canvas.drawBitmap(bitmap,x,y,null);
        }
        else {
            for(OneGrid each : foreshowTetro) {
                //获取当前预告块的颜色和横纵坐标
                paintTetromino.setColor(colorForTetromino[each.color]);
                x=each.x+GameParam.GRID_SIZE*(2-TetrisView.getNumCol()/2);
                y=each.y+GameParam.GRID_SIZE*3;

                //画网格
                oneUnit=new RectF(x,y,x+GameParam.GRID_SIZE,y+GameParam.GRID_SIZE);
                canvas.drawRoundRect(oneUnit,GameParam.ROUND_REC_RADIUS,GameParam.ROUND_REC_RADIUS,
                        paintGame);

                //画方块
                oneUnit=new RectF(x+PAINT_WIDTH,y+PAINT_WIDTH,x+GameParam.GRID_SIZE-PAINT_WIDTH,
                        y+GameParam.GRID_SIZE-PAINT_WIDTH);
                canvas.drawRoundRect(oneUnit,GameParam.ROUND_REC_RADIUS,GameParam.ROUND_REC_RADIUS,
                        paintTetromino);
            }
        }
    }

    //把要画的块传进来
    public void setForeshowTetro(ArrayList<OneGrid> someGrids) {
        this.foreshowTetro=someGrids;
        invalidate();
    }
}
