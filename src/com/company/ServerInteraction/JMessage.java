package com.company.ServerInteraction;

import com.company.Jacobi;

import java.io.*;
import java.util.List;

/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server.
 * When talking from a Java Client to a Java Server a lot easier to pass Java objects, no
 * need to count bytes or to wait for a line feed at the end of the frame
 */
public class JMessage implements Serializable {

    protected static final long serialVersionUID = 1112122200L;

    // The different types of message sent by the Client
    // WHOISIN to receive the list of the users connected
    // MESSAGE an ordinary message
    // LOGOUT to disconnect from the Server
    static final int WHOISIN = 0;
    static final int MESSAGE = 1;
    static final int LOGOUT = 2;
    static final int JACOBI_PANEL = 3;
    public static final int JACOBI_RUN_HALF_FIRST_TIME = 4;
    public static final int JACOBI_SERVER_RETURN_HALF_TO_CLIENT = 5;
    public static final int LIST = 6;
    public static final int JACOBI_SET_TEMPERATURE_CALCULATOR_GLOBAL_VARS = 55;
    public static final int JACOBI_RUN_HALF_SUBSEQUENT = 56;
    public static final int JACOBI_SET_ALLOY_COLORS_FOR_GUI = 60;
    private Object object;
    private int type;
    private String message;
    private double maxDiff = 0.0;
    private int lastX;
    private int lastY;
    private int hiCol;
    private int hiRow;
    private int firstCol;
    private int firstRow;

    // constructor
    public JMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public JMessage(int type, String message, Object object) {
        this.type = type;
        this.message = message;
        this.object = object;
    }

    // getters
    public int getType() {
        return type;
    }
    public String getMessage() {
        return message;
    }

    public Object getObject() {
        return object;
    }

    public void setLastCol(int lastX) {
        this.lastX = lastX;
    }

    public void setLastRow(int lastY) {
        this.lastY = lastY;
    }

    public int getLastCol() {
        return lastX;
    }

    public int getLastRow() {
        return lastY;
    }

    public void setHiCol(int hiCol) {
        this.hiCol = hiCol;
    }

    public void setHiRow(int hiRow) {
        this.hiRow = hiRow;
    }

    public int getHiCol() {
        return hiCol;
    }

    public int getHiRow() {
        return hiRow;
    }

    public int getFirstCol() {
        return firstCol;
    }

    public int getFirstRow() {
        return firstRow;
    }

    public void setFirstCol(int firstCol) {
        this.firstCol = firstCol;
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }
}
