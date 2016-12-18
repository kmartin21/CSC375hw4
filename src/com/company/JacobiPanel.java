package com.company;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * Created by keithmartin on 12/3/15.
 */
public class JacobiPanel extends JPanel {
    private static final double MAX_TEMP_DIVISOR = 1.11;
    private double maxTemp = 1000 / MAX_TEMP_DIVISOR;

    private Color color1;
    private Color color2;
    private GradientPaint gp;
    private static Graphics2D g2d;
    private static int pixelHeight = 5;
    private static int pixelWidth = 5;
    int columnsW, rowsH;
    List<JPoint> jPoints = new ArrayList();

    public JacobiPanel() {

    }

    public JPoint addPoint(int x, int y, Color c) {
        JPoint jPoint = new JPoint(x, y, c);
        jPoints.add(jPoint);
        return jPoint;
    }

    @Override
    public void paintComponent(Graphics g) {
        //System.out.println("IN");
        super.paintComponent(g);
        g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        for (int i = 0; i < jPoints.size(); i++) {
            JPoint jPoint = jPoints.get(i);

            if(jPoint !=null) {
                gp = new GradientPaint(50, 50, jPoint.c1, 0, pixelHeight, jPoint.c2);
                g2d.setPaint(gp);
                g2d.fillRect(jPoint.col * 4, jPoint.row * 4, pixelWidth, pixelHeight);
            }
        }
    }

    public synchronized void setJPoints(List<JPoint> list) {
        jPoints = list;
    }
    public List<JPoint> getJPoints() {
        return jPoints;
    }

    public void transferPoints(double[][] matrix, int firstCol, int lastCol, int firstRow, int lastRow) {
        //System.out.println("jPoints size :" + jPoints.size());
        for (int i = 0; i < jPoints.size(); i++) {
            JPoint jPoint = jPoints.get(i);
            //make sure the jPoint is within the bounds of the metalAlloy's regions
            int currentJPointCol = jPoint.col;
            int currentJPointRow = jPoint.row;
            if((currentJPointCol >= firstCol && currentJPointRow >= firstRow) && (currentJPointCol <= lastCol && currentJPointRow <= lastRow)) {
                //System.out.printf("\ncJP(col,row) (%s,%s)", currentJPointCol, currentJPointRow);
                //get the metalCell corresponding with the coordinates of the current jPoint
                double temp = matrix[currentJPointCol][currentJPointRow];
                //make sure the cell isn't null
                if (temp > 0) {
                    jPoint.setColor(getColor(temp));
                }
            }
        }
    }

    public Color getColor(double cellTemp) {
        Color color;
        //System.out.println("(" + xPos + "," + yPos + ")temp = " + temp);
        if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 9.25)) {
            color = hex2Rgb("#910000");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 9)) {
            color = hex2Rgb("#9b0000");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 8.75)) {
            color = hex2Rgb("#a50000");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 8.5)) {
            color = hex2Rgb("#af0000");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 8.25)) {
            color = hex2Rgb("#b90000");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 8)) {
            color = hex2Rgb("#c30000");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 7.75)) {
            color = hex2Rgb("#cd0000");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 7.5)) {
            color = hex2Rgb("#d70000");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 7.25)) {
            color = hex2Rgb("#FF0000");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 7)) {
            color = hex2Rgb("#FF0a00");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 6.75)) {
            color = hex2Rgb("#FF1400");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 6.5)) {
            color = hex2Rgb("#FF1e00");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 6.25)) {
            color = hex2Rgb("#FF2800");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 6)) {
            color = hex2Rgb("#FF3200");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 5.75)) {
            color = hex2Rgb("#FF3c00");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 5.5)) {
            color = hex2Rgb("#FF4600");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 5.25)) {
            color = hex2Rgb("#FF5000");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 5)) {
            color = hex2Rgb("#FF5a00");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 4.75)) {
            color = hex2Rgb("#FF6400");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 4.5)) {
            color = hex2Rgb("#FF6e00");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 4.25)) {
            color = hex2Rgb("#FF7800");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 4)) {
            color = hex2Rgb("#FF8200");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 3.75)) {
            color = hex2Rgb("#FF8c00");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 3.5)) {
            color = hex2Rgb("#FF9600");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 3.25)) {
            color = hex2Rgb("#FFa000");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 3)) {
            color = hex2Rgb("#FFaa00");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 2.75)) {
            color = hex2Rgb("#FFb400");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 2.5)) {
            color = hex2Rgb("#FFbe00");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 2.25)) {
            color = hex2Rgb("#FFc800");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 2)) {
            color = hex2Rgb("#FFd200");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 1.75)) {
            color = hex2Rgb("#FFdc00");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 1.5)) {
            color = hex2Rgb("#FFe600");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 1.25)) {
            color = hex2Rgb("#FFf000");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + 1)) {
            color = hex2Rgb("#FFfa00");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + .75)) {
            color = hex2Rgb("#ffff99");//dark orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + .5)) {
            color = hex2Rgb("#ffffb3");//orange
        } else if(cellTemp <= maxTemp / (MAX_TEMP_DIVISOR + .25)) {
            color = hex2Rgb("#ffffcc");//lemon
        } else if(cellTemp <= maxTemp / MAX_TEMP_DIVISOR ) {
            color = hex2Rgb("#ffffe5");//light yellow
        } else color = Color.WHITE;
        return color;
    }

    public static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    }
}
