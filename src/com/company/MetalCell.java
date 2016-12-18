package com.company;

import com.company.ServerInteraction.Server;

import java.awt.*;
import java.io.Serializable;
import java.util.Random;

/**
 * Created by keithmartin on 11/26/15.
 */
public class MetalCell implements Serializable {

    public static final double MAX_TEMP_DIVISOR = 1.11;
    public static Random random = new Random();

    private double metal1Percentage;
    private double metal2Percentage;
    private double metal3Percentage;
    private double c1;
    private double c2;
    private double c3;
    private double cellTemp;
    private int col;
    private int row;
    private JPoint jPoint;
    private double maxTemp;
    private double n;

    public MetalCell(int col, int row, double c1, double c2, double c3, double maxTemp, int whichPanel) {
        initPercentagesOfMetals();
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.cellTemp = 0;
        this.col = col;
        this.row = row;
        this.maxTemp = maxTemp;
        //n = Double.MAX_VALUE;
        n=maxTemp;
        switch (whichPanel) {
            case 1:
                JacobiPanel jacobiPanel = Main.getJacobiPanel();
                if(jacobiPanel != null) this.jPoint = jacobiPanel.addPoint(col, row, hex2Rgb("#000000"));
                break;
            case 2:
                this.jPoint = Server.getJacobiPanel().addPoint(col, row, hex2Rgb("#000000"));
        }
    }

    private void initPercentagesOfMetals() {
        double temp1 = random.nextDouble();
        double temp2 = random.nextDouble();
        double temp3 = random.nextDouble();
        double sumOfTemps = temp1 + temp2 + temp3;
        this.metal1Percentage = temp1 / sumOfTemps;
        this.metal2Percentage = temp2 / sumOfTemps;
        this.metal3Percentage = temp3 / sumOfTemps;
        /*System.out.printf("\ntemp1(%s) temp2(%s) temp3(%s) sum(%s)", temp1,temp2,temp3, sumOfTemps);
        System.out.printf("\ntemp1P(%s) temp2P(%s) temp3P(%s) sumP(%s)", metal1Percentage,metal2Percentage,metal3Percentage,
                metal1Percentage+ metal2Percentage + metal3Percentage);*/
    }

    public double getMetalPercentage(int n) {
        switch (n) {
            case 1:
                return metal1Percentage;
            case 2:
                return metal2Percentage;
            case 3:
                return metal3Percentage;
            default:
                return 0;
        }
    }

    public double getCellTemp() {
        return cellTemp;
    }


    public double getThermConstant(int n) {
        switch (n) {
            case 1:
                return c1;
            case 2:
                return c2;
            case 3:
                return c3;
            default:
                return 0;
        }
    }

    /**
     * When you set the temp of this region for the first time,
     * create it's JPoint on the JacobiPanel (GUI JPanel).
     *
     * When you change the temp of this region anytime thereafter,
     * grab it's JPoint and change it's color.
     * @param temp
     */
    public void setTemp(double temp){
        //if(n < 2) n = 2;
        if(temp >= maxTemp) temp = maxTemp - (1/--n);
        this.cellTemp = temp;
        //System.out.println(cellTemp);
        Color color = getColor();
        if(this.jPoint != null) {
            jPoint.setC1(color, color);
        }
    }

    public Color getColor() {
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


    public void setCellTemp(double tempC) {
        this.cellTemp = tempC;
    }

    public JPoint getJPoint() {
        return jPoint;
    }
}
