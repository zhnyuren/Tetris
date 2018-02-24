package com.flynnz.tetris;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;

/**
 * Created by FlynnZ on 2017/7/8.
 */

public class TapTextView extends android.support.v7.widget.AppCompatTextView {
    //渲染器，显示颜色效果
    private LinearGradient linearGradient;
    
    //矩阵，确定渲染范围
    private Matrix gMatrix;
    
    //渲染的起始位置
    private int startPos=0;
    
    //渲染的终止距离
    private int endPos=0;
    
    //刷新周期
    private int cycle=150;
    
    //画笔
    private Paint paint=null;

    public TapTextView(Context context,AttributeSet attrs) {
        super(context, attrs);
        paint=getPaint();
        gMatrix=new Matrix();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        startPos=getMeasuredWidth();

        linearGradient=new LinearGradient(0,0,startPos,0,new int[]
                {
                        Color.parseColor("#FFFFFF"),
                        Color.parseColor("#FFA500"),
                        Color.parseColor("#FFFFFF")
                },null,Shader.TileMode.CLAMP);

        paint.setShader(linearGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(gMatrix!=null) {
            //每次移动屏幕的1/10
            //相当于移动次数
            endPos+=startPos/10;
            if(endPos>2*startPos) endPos=-startPos;
            gMatrix.setTranslate(endPos,0);
            //渲染
            linearGradient.setLocalMatrix(gMatrix);
            postInvalidateDelayed(cycle);
        }
    }

    @Override
    protected void onSizeChanged(int newW,int newH,int oldW,int oldH) {
        super.onSizeChanged(newW,newH,oldW,oldH);
    }
}
