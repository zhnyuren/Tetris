package com.flynnz.tetris;

import java.util.ArrayList;

/**
 * Created by FlynnZ on 2017/7/5.
 * 用来组成俄罗斯方块的一个格子
 */

public class OneGrid {
    //颜色和坐标
    public int x,y,color;

    public OneGrid(int _color,int _x,int _y) {
        this.x=_x;
        this.y=_y;
        this.color=_color;
    }

    /**
     * 是否可以向左移动
     */
    public static boolean judgeLeft(ArrayList<OneGrid> fourGrids,ArrayList<OneGrid> existedGrids) {
        //遍历当前俄罗斯方块的四个格子
        for(OneGrid each : fourGrids) {
            //判断是否会越过左边界
            if(each.x<GameParam.BEGIN_POS+GameParam.GRID_SIZE)
                return false;
            //判断是否会和已经存在的格子重合
            if(gridCoincide(each.x-GameParam.GRID_SIZE,each.y,existedGrids))
                return false;
        }
        return true;
    }

    /**
     * 是否可以向右移动
     */
    public static boolean judgeRight(ArrayList<OneGrid> fourGrids,
                                     ArrayList<OneGrid> existedGrids,
                                     int rightMargin) {
        //遍历当前俄罗斯方块的四个格子
        for(OneGrid each : fourGrids) {
            //判断是否会越过右边界
            if(each.x>rightMargin-2*GameParam.GRID_SIZE)
                return false;
            //判断是否会和已经存在的格子重合
            if(gridCoincide(each.x+GameParam.GRID_SIZE,each.y,existedGrids))
                return false;
        }
        return true;
    }

    /**
     * 是否可以向下移动
     */
    public static boolean judgeDown(ArrayList<OneGrid> fourGrids,
                                    ArrayList<OneGrid> existedGrids,
                                    int bottomMargin) {
        //遍历当前俄罗斯方块的四个格子
        for(OneGrid each : fourGrids) {
            //判断是否会越过下边界
            if(each.y>bottomMargin-2*GameParam.GRID_SIZE) //TODO 3
                return false;
            //判断是否会和已经存在的格子重合
            if(gridCoincide(each.x,each.y+1*GameParam.GRID_SIZE,existedGrids)) //TODO 2
                return false;
        }
        return true;
    }

    /**
     * 是否可以旋转，此处仅仅判断重合，逻辑是先把块强行转过来存在临时的块结构中
     * 然后调用此函数查看是否会重合，不重合就真的旋转
     */
    public static boolean judgeRotate(ArrayList<OneGrid> fourGrids,ArrayList<OneGrid> existedGrids) {
        //遍历当前俄罗斯方块的四个格子
        for(OneGrid each : fourGrids) {
            //判断是否会和已经存在的格子重合
            if(gridCoincide(each.x,each.y,existedGrids))
                return false;
        }
        return true;
    }

    /**
     * 判断游戏是否终止
     */
    public static boolean judgeContinue(ArrayList<OneGrid> existedGrids) {
        //遍历所有已存在的格子，查看y坐标是否小于等于最顶端，小于则说明无法继续，游戏结束
        for(OneGrid each : existedGrids) {
            if(each.y<=GameParam.BEGIN_POS) return false;
        }
        return true;
    }

    /**
     * 向左移动格子
     */
    public static boolean gridToLeft(ArrayList<OneGrid> fourGrids,ArrayList<OneGrid> existedGrids) {
        //如果能够向左移动
        if(judgeLeft(fourGrids,existedGrids)) {
            for(OneGrid each : fourGrids) each.x-=GameParam.GRID_SIZE;
            return true;
        }
        return false;
    }

    /**
     * 向右移动格子
     */
    public static boolean gridToRight(ArrayList<OneGrid> fourGrids,
                                      ArrayList<OneGrid> existedGrids,
                                      int rightMargin) {
        //如果能够向右移动
        if(judgeRight(fourGrids,existedGrids,rightMargin)) {
            for(OneGrid each : fourGrids) each.x+=GameParam.GRID_SIZE;
            return true;
        }
        return false;
    }

    /**
     * 向下移动格子
     */
    public static void gridDown(ArrayList<OneGrid> fourGrids) {
        //打算在外部判断是否能够向下移动
        for(OneGrid each : fourGrids) each.y+=GameParam.GRID_SIZE;
    }

    /**
     * 判断是否会和已经存在的格子重合
     */
    public static boolean gridCoincide(int x,int y,ArrayList<OneGrid> existedGrids) {
        //遍历所有已存在的格子
        for(OneGrid each : existedGrids) {
            //横纵坐标都重合才算重合
            if(Math.abs(x-each.x)<GameParam.GRID_SIZE && Math.abs(y-each.y)<GameParam.GRID_SIZE)
                return true;
        }
        return false;
    }

    /**
     * 在“已存在的格子”的列表中删除某一行的格子
     * 这里仅针对existedGrids列表进行删除，其他数据结构的删除在其他文件中
     */
    public static void deleteOneLine(ArrayList<OneGrid> existedGrids,int row) {
        //TODO 必须从下到上遍历
        for(int i=existedGrids.size()-1;i>=0;i--) {
            if(row==(existedGrids.get(i).y-GameParam.BEGIN_POS)/GameParam.GRID_SIZE)
                existedGrids.remove(i);
        }
    }
}
