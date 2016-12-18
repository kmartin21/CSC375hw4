package com.company;

import com.company.ServerInteraction.Client;
import com.company.ServerInteraction.JMessage;
import com.company.ServerInteraction.Server;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by keithmartin on 11/25/15.
 */
public class Cell extends ForkJoinTask<Double> {

    private boolean splitWorkToServer;
    volatile double maxDiff;
    JacobiPanel panel = new JacobiPanel();
    private Color red = new Color(221, 17, 17);
    private final double[][] regionsA;
    private final double[][] regionsB;
    private final int loRow;
    private final int hiCol;
    private final int loCol;
    private final int hiRow;
    private int steps = 0;
    private MetalAlloy metalAlloy;
    private ForkJoinPool mainPool = new ForkJoinPool();
    private double matrixA[][];
    private double matrixB[][];
    private double md;
    private double[][] resultantMatrixFromServer;
    private boolean sentMetalAlloyToServer;
    private double[] lastColumnFirstHalfOfMatrix;
    private double[] firstColumnSecondHalfOfMatrix;
    private int lastColumnFirstHalfOfMatrixIndex;
    private int firstColumnSecondHalfOfMatrixIndex;
    //private MetalAlloy resultantMetalAlloyFromServer;

    Cell(MetalAlloy metalAlloy, double[][] regionsA, double[][] regionsB,
         int loCol, int hiCol, int loRow, int hiRow) {
        this.regionsA = regionsA;
        this.regionsB = regionsB;
        this.loCol = loCol;
        this.hiCol = hiCol;//highest horizontal
        this.loRow = loRow;
        this.hiRow = hiRow;//highest vertical
        //matrix is in coordination of [col][row]

        displayEventFromClient(String.format("\ncJP (%s,%s)", hiCol, hiRow));
        this.metalAlloy = metalAlloy;
    }

    Cell (MetalAlloy metalAlloy, double[][] RegionA, double[][] RegionB,
        int loCol, int hiCol, int loRow, int hiRow, boolean splitWorkToServer) {
        this(metalAlloy, RegionA, RegionB, loCol, hiCol, loRow, hiRow);

        if(splitWorkToServer) {
            displayEventFromClient("#Creating Cell to split work between client and server.");
            this.splitWorkToServer = true;
            ServerListener serverListener = new ServerListener(getClient().getInputStream());
            //set the client's server listener
            getClient().setServerListener(serverListener);
            //start the client's server listener
            getClient().startServerListener();
        } else {
            System.out.printf("\n#Creating Cell to do all work on client.");
        }
    }

    public Double getRawResult() {
        return maxDiff;
    }

    protected void setRawResult(Double value) {
        maxDiff = value;
    }

    ArrayList<TemperatureCalculator> tasks;
    ArrayList<Callable<Double> > firstHalfTasks;
    ArrayList<Server.TemperatureCalculator> secondHalfTasks;
    CountDownLatch secondHalfLatch;

    @Override
    protected boolean exec() {
        md = 0.001;
        boolean doFlipAtoB = (steps++ % 2) == 0;
        matrixA = (doFlipAtoB) ? regionsA : regionsB;
        matrixB = (doFlipAtoB) ? regionsB : regionsA;
        matrixB[0][0] = metalAlloy.getTempS();
        matrixB[hiCol - 1][hiRow - 1] = metalAlloy.getTempT();
        metalAlloy.getRegions()[0][0].setCellTemp(metalAlloy.getTempS());
        metalAlloy.getRegions()[hiCol - 1][hiRow - 1].setCellTemp(metalAlloy.getTempT());

        //check if we have already created our tasks.
        //if we done already have our tasks, then create them
        //otherwise, skip this block and execute our preexisting tasks
        if(!splitWorkToServer) {
            createTasks();
            mainPool.invokeAll(tasks);
        } else if(splitWorkToServer) {
            createSplitTasks();
            runSplitTasks();
        }

        //System.out.println("md: " + md);
        setRawResult(md);
        return true;
    }

    private void createTasks() {
        if(tasks == null) {
            tasks = new ArrayList();
            for (int i = loCol; i <= hiCol; ++i) {
                //System.out.println("[ " + row + " " + j + " " + metalAlloy.getRegions()[row][j-1].getCellTemp());
                //Calculate cell temp for matrix[row][j] below
                TemperatureCalculator temp = new TemperatureCalculator(i);
                tasks.add(temp);
            }
        }
    }

    private void createSplitTasks() {
        if(firstHalfTasks == null || secondHalfTasks == null) {
            firstHalfTasks = new ArrayList();
            secondHalfTasks = new ArrayList();
            //make a task for each column and add it to either the first half or second half tasks
            for (int i = loCol; i <= hiCol; ++i) {
                //System.out.println("[ " + row + " " + j + " " + metalAlloy.getRegions()[row][j-1].getCellTemp());
                //Calculate cell temp for matrix[row][j] below
                if(i <= (hiCol / 2)){
                    TemperatureCalculator temp = new TemperatureCalculator(i);
                    firstHalfTasks.add(temp);
                    if(i == hiCol / 2){
                        //we are on the right edge of the first half. save it to a variable
                        //this is the column we want to transfer subsequently after the first step
                        System.out.println("last column of first half: " + i);
                        lastColumnFirstHalfOfMatrixIndex = i;
                        lastColumnFirstHalfOfMatrix = matrixB[lastColumnFirstHalfOfMatrixIndex];
                    }
                } else {
                    if(i == (hiCol / 2) +1 ){
                        //we are on the left edge of the second half. save it to a variable
                        //this is the column we want to transfer subsequently after the first step
                        System.out.println("first column of second half: " + i);
                        firstColumnSecondHalfOfMatrixIndex = i;
                        firstColumnSecondHalfOfMatrix = matrixB[firstColumnSecondHalfOfMatrixIndex];
                    }
                    Server.TemperatureCalculator tempCalculator = new Server.TemperatureCalculator(i);
                    secondHalfTasks.add(tempCalculator);
                }
            }

            ServerRunner serverRunner = new ServerRunner();
            firstHalfTasks.add(serverRunner);
        }
    }

    private void runSplitTasks() {
        boolean awaitServerResponse = false;
        if(steps % 8000 ==0 || steps < 2) {
            awaitServerResponse = true;
            resetServerGlobalVars(awaitServerResponse);
        }
        //make new countdownlatch to await the second half
        secondHalfLatch = new CountDownLatch(1);
        mainPool.invokeAll(firstHalfTasks);//wait for the first half to complete
        //wait for server's response if we have chosen to on this step
        if(awaitServerResponse) {
            try {
                System.out.println("AWAITINGS SERVER RESPONSE =" + steps);
                //displayEventFromClient("Awaiting CountdownLatch");
                //await second half of tasks to complete from server
                secondHalfLatch.await();
                //displayEventFromClient("Countdown latch finished");
            } catch (InterruptedException e) { e.printStackTrace(); }
            //System.out.println("RECEIVED SERVER RESPONSE =" + steps);
        }

        //send the new values of the matrix to the gui server
        JMessage jMessage1 = new JMessage(JMessage.JACOBI_SET_ALLOY_COLORS_FOR_GUI,
                "Set new jPoint colors from server.", matrixB);
        jMessage1.setFirstCol(0);
        jMessage1.setFirstRow(0);
        jMessage1.setLastCol(lastColumnFirstHalfOfMatrixIndex);
        jMessage1.setLastRow(matrixB[0].length);
        //getClient().displayEvent("SENDING NEW JPOINTS to GuiServer from [" + getClient().getUsername() + "]");
        getGuiServerClient().sendMessage(jMessage1);
        try {
            getGuiServerClient().getOutputStream().reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Both halves have completed. CONTINUE ABOUT YOUR LIFE
    }

    private void resetServerGlobalVars(boolean awaitServerResponse) {
        List<Object> globalVars = new ArrayList();
        globalVars.add(lastColumnFirstHalfOfMatrix);
        if(awaitServerResponse)globalVars.add(true);
        else globalVars.add(false);
        if(!sentMetalAlloyToServer) {
            //we only want to send the metalAlloy and hiRow & hiCol once
            // because it has way too much data to continuously transfer back and forth
            globalVars.add(matrixB);
            globalVars.add(hiCol);
            globalVars.add(hiRow);
            /*System.out.println("hiCol" + hiCol);
            System.out.println("hiRow" + hiRow);*/
            globalVars.add(metalAlloy);
            sentMetalAlloyToServer = true;
        }

        JMessage globalVarsMessage = new JMessage(JMessage.JACOBI_SET_TEMPERATURE_CALCULATOR_GLOBAL_VARS, "Set server side global vars from client.", globalVars);
        getClient().sendMessage(globalVarsMessage);
        try { getClient().getOutputStream().reset();}
        catch (IOException e) { e.printStackTrace(); }
    }

    public double[][] getRegionsB() {
        return regionsB;
    }

    public class TemperatureCalculator implements Callable<Double> {
        private int row;

        public TemperatureCalculator(int row) {
            this.row = row;
        }

        public void calculateTemperature(int col) {

            double sigmaTemperatureFinal = 0;
            double sigmaTemperature = 0;
            //System.out.println(hiRow);

            //go through all cells in each columns across this col and calculate their temps
            for (int row = 0; row <= hiRow; row++) {
                if (!(col == 0 && row == 0) && !(col == hiCol && row == hiRow)) {
                    int numN = 1;
                    double numNeighbors = 0;
                    sigmaTemperatureFinal = 0;
                    sigmaTemperature = 0;

                    while (numN < 4) {
                        sigmaTemperature = 0;
                        if (!(col - 1 < 0)) {
                            sigmaTemperature += matrixA[col - 1][row] * (metalAlloy.getRegions()[col - 1][row].getMetalPercentage(numN));
                            if (numN == 1) numNeighbors++;
                        }
                        if (!(col + 1 > hiCol)) {
                            sigmaTemperature += matrixA[col + 1][row] * (metalAlloy.getRegions()[col + 1][row].getMetalPercentage(numN));
                            if (numN == 1) numNeighbors++;
                        }
                        if (!(row - 1 < 0)) {
                            //System.out.println("[" + col + " " + j + " " + metalAlloy.getRegions()[col][j - 1].getCellTemp());
                            sigmaTemperature += matrixA[col][row - 1] * (metalAlloy.getRegions()[col][row - 1].getMetalPercentage(numN));
                            if (numN == 1) numNeighbors++;
                        }
                        if (!(row + 1 > hiRow)) {
                            sigmaTemperature += matrixA[col][row + 1] * (metalAlloy.getRegions()[col][row + 1].getMetalPercentage(numN));
                            if (numN == 1) numNeighbors++;
                        }
                        sigmaTemperatureFinal += (metalAlloy.getRegions()[col][row].getThermConstant(numN) * sigmaTemperature) / numNeighbors;
                        numN++;
                    }

                    matrixB[col][row] = sigmaTemperatureFinal;
                    metalAlloy.getRegions()[col][row].setTemp(sigmaTemperatureFinal);
                    md = Math.max(md, matrixB[col][row] - matrixA[col][row]);
                }
                //System.out.println("[" + col + " " + j + " " + sigmaTemperatureFinal);
            }
        }

        @Override
        public Double call() throws Exception {
            this.calculateTemperature(row);
            return null;
        }
    }

    /**
     * Created by lwdthe1 on 12/7/2015.
     */
    public class ServerRunner implements Callable<Double> {
        private boolean sent = false;

        private void startSecondHalf() {
            if(!sent) {
                JMessage startSecondHalfMessage = new JMessage(JMessage.JACOBI_RUN_HALF_FIRST_TIME, "Run Second Half First Time", secondHalfTasks);
                //displayEventFromClient("Sending second half of tasks to server to compute.");
                getClient().sendMessage(startSecondHalfMessage);
                try {
                    getClient().getOutputStream().reset();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sent = true;
            } else {
                JMessage startSecondHalfMessage = new JMessage(JMessage.JACOBI_RUN_HALF_SUBSEQUENT, "Run Second Half Subsequent");
                //displayEventFromClient("Sending second half of tasks to server to compute.");
                getClient().sendMessage(startSecondHalfMessage);
                try {
                    getClient().getOutputStream().reset();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public Double call() throws Exception {
            startSecondHalf();
            return null;
        }
    }


    /*
     * matrixA class that waits for the message from the server and append them to the JTextArea
     * if we have matrixA GUI or simply printToConsole() it in console mode
     */
    public class ServerListener extends Thread {
        ObjectInputStream inputStream;
        public ServerListener(ObjectInputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void run() {
            while(true) {
                String message = "";
                try {
                    //keep trying to read from the input stream connected to the server
                    Object o = inputStream.readObject();
                    //we received something from the input stream which means the server sent something: process it.
                    processInputFromServer(o);
                    continue;
                } catch(IOException e) {
                    displayEventFromClient("Server has closed the connection: " + e);
                    break;
                }
                // can't happen with matrixA String object but need the catch anyhow
                catch(ClassNotFoundException e2) {
                }
            }
        }

        private void processInputFromServer(Object o) {
            String message;
            try{
                JMessage jMessageFromServer = (JMessage) o;
                message = jMessageFromServer.getMessage();
                switch(jMessageFromServer.getType()){
                    case JMessage.JACOBI_SERVER_RETURN_HALF_TO_CLIENT:
                        //the server has returned the result of the second half
                        firstColumnSecondHalfOfMatrix = (double[])jMessageFromServer.getObject();
                       /* for (int i = 0; i < firstColumnSecondHalfOfMatrix.length; i++) {
                            System.out.println("FIRST COLUMN: " + firstColumnSecondHalfOfMatrix[i]);
                        }
                        System.out.println(">> FINISHED ITERATING");*/
                        //set new received edge values
                        matrixB[firstColumnSecondHalfOfMatrixIndex] = firstColumnSecondHalfOfMatrix;
                        //let the client know it can move forward
                        //displayEventFromClient("Done processing server calculations response");
                        secondHalfLatch.countDown();
                        break;
                    default:
                        getClient().printToConsole(message);
                }
                return;
            } catch (ClassCastException e){
                //will reach if the server's message was not matrixA JMessage instance
                //printToConsole("NOT OBJECT");
            }

            message = (String) o;
            // if console mode print the message and add back the prompt
            getClient().printToConsole(message);
        }
    }

    private void displayEventFromClient(String event) {
        if(event != null) {
            getClient().displayEvent(String.format(event));
        }
    }

    private Client getClient() {
        return Main.getClient();
    }

    private Client getGuiServerClient() {
        return Main.getGuiServerClient();
    }
}
