package com.company;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by lwdthe1 on 12/7/2015.
 */
public class JPoint implements Serializable {
    //GradientPaint gradientPaint;
    int col, row;
    Color c1, c2;

    public JPoint(int x, int y, Color c) {
        this.col = x;
        this.row = y;
        this.c1 = c;
        this.c2 = c;
    }

    @Override
    public String toString(){
        return String.format("(%s,%s) %s", row, col,c1.getRGB());
    }

    public void setC1(Color c1, Color c2) {
        //System.out.println("B " + this);
        this.c1 = c1;
        this.c2 = c2;
        //System.out.println("A" + this + " c: " + c1.getRGB());
    }

    public void setColor(Color color) {
        this.c1 = color;
        this.c2 = color;
    }
}