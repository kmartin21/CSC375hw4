package com.company;

import java.io.Serializable;

/**
 * Created by keithmartin on 11/26/15.
 */
public class MetalAlloy implements Serializable{

    final private double topLeftS;
    final private double bottomRightT;
    private int cols;
    private int rows;
    private MetalCell region[][];
    private double maxTemp;
    private double redTemp;

    public MetalAlloy(double topLeftS, double bottomRightT, int cols, int rows, double c1, double c2, double c3) {
        this.topLeftS = topLeftS;
        this.bottomRightT = bottomRightT;
        maxTemp = Math.max(topLeftS,bottomRightT);
        this.cols = cols;
        this.rows = rows;
        this.region = initRegion(c1, c2, c3);
        redTemp = maxTemp / (MetalCell.MAX_TEMP_DIVISOR + 3);
    }


    public MetalCell[][] initRegion(double c1, double c2, double c3) {
        MetalCell region[][] = new MetalCell[cols][rows];
        for (int col = 0; col < region.length; col++) {
            for (int row = 0; row < region[col].length; row++) {
                MetalCell cell = new MetalCell(col, row, c1, c2, c3, maxTemp,1);
                if(col == 0 && row == 0) cell.setTemp(topLeftS);
                else if(col == region.length - 1 && row == region[col].length - 1) cell.setTemp(bottomRightT);
                else {
                    double randTemp = MetalCell.random.nextInt((int) (.1 * maxTemp)) + 47;
                    cell.setTemp(randTemp);
                }
                region[col][row] = cell;
            }
        }
        return region;
    }

    public double[][] createRegions() {
        double doubleRegion[][] = new double[cols][rows];
        for (int col = 0; col < region.length; col++) {
            for (int row = 0; row < region[col].length; row++) {
                doubleRegion[col][row] = region[col][row].getCellTemp();
            }
        }
        return doubleRegion;
    }

    public MetalCell[][] getRegions() {
        return region;
    }

    public double getTempS() {
        return topLeftS;
    }

    public double getTempT() {
        return bottomRightT;
    }

    public int getSize() {
        return rows * cols;
    }
}
