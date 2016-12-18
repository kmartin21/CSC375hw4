package com.company.ServerInteraction;

import com.company.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

/*
 * The server that can be run both as a console application or a GUI
 */
public class Server extends ForkJoinTask<Double> {
    private static final int ROWS = 200;
    private static final int COLUMNS = 2 * ROWS;

    // a unique ID for each connection
    private static int uniqueId;
    public static int portNumber = 2841;

    //public  static String guiServerAddress = "pi.cs.oswego.edu";
    public  static String guiServerAddress = "localhost";
    public static int guiPortNumber = 2850;
    private static String guiServerFrame;
    private static JacobiPanel guiServerJPanel;
    private static ActionListener guiServerActionListener;

    // an ArrayList to keep the list of the Client
    private ArrayList<ClientThread> listOfClients;
    // if I am in a GUI
    private ServerGUI serverGUI;
    // to displayEvent time
    private SimpleDateFormat dateFormat;
    // the port number to listen for connection
    private int port;
    // the boolean that will be turned of to stopServerFromGUI the server
    private boolean keepGoing;
    private ForkJoinPool pool = new ForkJoinPool();

    /*
     *  To run as a console application just open a console window and:
     * > java Server
     * > java Server portNumber
     * If the port number failed not specified 1500 failed used
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the port number to connect to server.");
        try {
            portNumber = Integer.parseInt(scanner.nextLine());
        } catch (Exception e){
           //bad number
        }
        if(portNumber == 2) portNumber = 2850;
        else if (portNumber == 1) portNumber = 2844;
        // create a server object and start it
        Server server = new Server(portNumber);
        server.start();
    }

    /*
     *  server constructor that receive the port to listen to for connection as parameter
     *  in console
     */
    public Server(int port) {
        this(port, null);
    }

    public Server(int port, ServerGUI sg) {
        // the port
        this.port = port;
        if(port == guiPortNumber){
            initServerGUI();
        }
        // to displayEvent hh:mm:ss
        dateFormat = new SimpleDateFormat("HH:mm:ss");
        // ArrayList for the Client list
        listOfClients = new ArrayList<ClientThread>();
    }

    private static void initServerGUI() {
        guiServerFrame = "Jacobi Server GUI";
        JFrame frame = new JFrame(guiServerFrame);
        guiServerJPanel = new JacobiPanel();
        Color baseColor = MetalCell.hex2Rgb("#bdc3c7");
        guiServerJPanel.setBackground(baseColor);
        frame.add(guiServerJPanel);
        frame.setSize(COLUMNS, ROWS);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MetalCell region[][] = new MetalCell[COLUMNS][ROWS];

        double maxTemp = Math.max(Main.getTempS(),Main.getTempT());
        for (int i = 0; i < region.length; i++) {
            for (int j = 0; j < region[i].length; j++) {
                MetalCell cell = new MetalCell(i, j, 1, 1, 1, maxTemp,2);
                if(i == 0 && j == 0) cell.setTemp(Main.getTempS());
                if(i == region.length - 1 && j == region[i].length - 1) cell.setTemp(Main.getTempT());
            }
        }
        frame.setVisible(true);

        guiServerActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                guiServerJPanel.repaint();
            }
        };

        int repaintPanelFrequency = 1000 * 1;//how often to trigger the timer
        javax.swing.Timer timer = new javax.swing.Timer(repaintPanelFrequency, guiServerActionListener);
        //trigger the timer for the first time in 2 seconds
        timer.setInitialDelay(1000 * 1);
        timer.start();//start the timer
    }

    public static JacobiPanel getJacobiPanel() {
        return guiServerJPanel;
    }

    public void start() {
        keepGoing = true;
		/* create socket server and wait for connection requests */
        try {
            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);

            // infinite loop to wait for new connections
            while (keepGoing) {
                // format message saying we are waiting
                displayEvent("Server waiting for Clients on port " + port + ".");

                // accept connection. Waits here until new connection comes in
                Socket socket = serverSocket.accept();
                // if I was asked to stopServerFromGUI
                if (!keepGoing)
                    break;
                ClientThread t = new ClientThread(socket);  // make a thread of it
                listOfClients.add(t);                                    // save it in the ArrayList
                t.start();
            }
            // I was asked to stopServerFromGUI
            try {
                displayEvent("Jacobi Server asked to stopServerFromGUI.");
                serverSocket.close();
                for (int i = 0; i < listOfClients.size(); ++i) {
                    ClientThread tc = listOfClients.get(i);
                    try {
                        tc.inputStream.close();
                        tc.outputStream.close();
                        tc.socket.close();
                    } catch (IOException ioE) {
                        // not much I can do
                    }
                }
            } catch (Exception e) {
                displayEvent("Exception closing the server and clients: " + e);
            }
        } catch (IOException e) { // something went bad
            String msg = dateFormat.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            displayEvent(msg);
        }
    }
    /*
     * For the GUI to stopServerFromGUI the server
     */
    protected void stopServerFromGUI() {
        keepGoing = false;
        // connect to myself as Client to exit statement
        // Socket socket = serverSocket.accept();
        try {new Socket("localhost", port);}
        catch(Exception e) {/*nothing I can really do*/}
    }
    /*
     * Display an event (not a message) to the console or the GUI
     */
    private void displayEvent(String msg) {
        String timeAndMessage = dateFormat.format(new Date()) + " " + msg;
        if(serverGUI == null)
            printToConsole(timeAndMessage);
        else
            serverGUI.appendEvent(timeAndMessage + "\n");
    }
    /*
     *  to broadcast a message to all Clients
     */
    private synchronized void broadcast(String message) {
        // add HH:mm:ss and \n to the message
        String time = dateFormat.format(new Date());
        String messageLf = time + " " + message + "\n";
        // displayEvent message on console or GUI
        if(serverGUI == null)
            System.out.print(messageLf);
        else
            serverGUI.appendRoom(messageLf);     // append in the room window

        // we loop in reverse order in case we would have to removeLoggedOutClient a Client
        // because it has disconnected
        for(int i = listOfClients.size(); --i >= 0;) {
            ClientThread ct = listOfClients.get(i);
            // try to write to the Client if it fails removeLoggedOutClient it from the list
            if(!ct.writeMessage(messageLf)) {
                listOfClients.remove(i);
                displayEvent("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }

    // for a client who logoff using the LOGOUT message
    synchronized void removeLoggedOutClient(int id) {
        ClientThread clientThread = getClientThreadById(id);
        if(clientThread != null) {
            listOfClients.remove(clientThread);
        }
    }

    private ClientThread getClientThreadById(int id) {
        ClientThread clientThread = null;
        // scan the array list until we found the Id
        for(int i = 0; i < listOfClients.size(); ++i) {
            ClientThread currentClientThread = listOfClients.get(i);
            // found it
            if(currentClientThread.id == id) {
                clientThread = currentClientThread;
                break;
            }
        }
        return clientThread;
    }

    @Override
    public Double getRawResult() {
        return null;
    }

    @Override
    protected void setRawResult(Double aDouble) {

    }

    @Override
    protected boolean exec() {
        return false;
    }

    /**
     * One instance of this thread will run for each client
     */
    public class ClientThread extends Thread {
        private Client guiServerClient;
        // the socket where to listen/talk
        Socket socket;
        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;
        private ForkJoinPool pool = new ForkJoinPool();
        // my unique id (easier for disconnection)
        int id;
        // the Username of the Client
        String username;
        // the only type of message a will receive
        JMessage jMessage;
        // the date I connect
        String date;
        private ArrayList<TemperatureCalculator> jacobiTempCalcTasks;
        private boolean notRunningTasks;
        private boolean clientIsAwaitingResponse;

        // Constructore
        ClientThread(Socket socket) {
            // a unique id
            id = ++uniqueId;
            this.socket = socket;
			/* Creating both Data Stream */
            printToConsole("Thread trying to create Object Input/Output Streams");
            try
            {
                // create output first
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());
                // read the username
                username = (String) inputStream.readObject();
                displayEvent(username + " just connected.");
                pool = new ForkJoinPool();
                //CONNECTED TO GUI SERVER SUCCESSFULLY!!!
                System.out.println(String.format("#%s CONNECTED TO GUI SERVER SUCCESSFULLY\n", username));

            }
            catch (IOException e) {
                displayEvent("Exception creating new Input/output Streams: " + e);
                return;
            }
            // have to catch ClassNotFoundException
            // but I read a String, I am sure it will work
            catch (ClassNotFoundException e) {
            }
            date = new Date().toString() + "\n";
        }

        /**
         * Runs the server forever
         */
        public void run() {
            Cell cell = null;
            // to loop until LOGOUT
            boolean keepGoing = true;
            while(keepGoing) {
                // read a String (which failed an object)
                try {
                    jMessage = (JMessage) inputStream.readObject();
                } catch (IOException e) {
                    displayEvent(username + " Exception reading Streams: " + e);
                    //e.printStackTrace();
                    break;
                } catch(ClassNotFoundException e2) {
                    break;
                }
                // the message part of the JMessage
                String message = jMessage.getMessage();

                // Switch on the type of message receive
                switch(jMessage.getType()) {
                    case JMessage.MESSAGE:
                        broadcast(username + ": " + message);
                        break;
                    case JMessage.JACOBI_SET_TEMPERATURE_CALCULATOR_GLOBAL_VARS:
                        List<Object> globalVals = (ArrayList<Object>) jMessage.getObject();
                        lastColumnFirstHalfOfMatrix = (double[]) globalVals.get(0);
                        clientIsAwaitingResponse = (boolean) globalVals.get(1);
                        if(globalVals.size() > 2) {
                            //we only want to receive the whole regionB & metalAlloy & hiRow & hiCol
                            // form the client on the very first step
                            // because it has way too much data to continuously transfer back and forth
                            regionA = (double[][]) globalVals.get(2);//set the matrix
                            matrixA = (double[][]) globalVals.get(2);
                            regionB = (double[][]) globalVals.get(2);//set the copy of the matrix
                            matrixB =(double[][]) globalVals.get(2);

                            hiCol = (Integer) globalVals.get(3);//set the high column
                            hiRow = (Integer) globalVals.get(4);// set the high row
                            // set the metalAlloy to use for getting percentages
                            // of different metals in each cell in the matrix
                            metalAlloy = (MetalAlloy) globalVals.get(5);
                        }
                        transferMatrixColumnFromClientToServer();
                        /*for (int i = 0; i < lastColumnFirstHalfOfMatrix.length; i++) {
                            System.out.println("LAST COLUMN: " + lastColumnFirstHalfOfMatrix[i]);
                        }*/
                        break;
                    case JMessage.JACOBI_RUN_HALF_FIRST_TIME:
                        displayEvent(username + " JRH: sent regionB to initialize computation.");
                        if (!connectToGUIServer()) return;
                        //CONNECTION TO SERVER SUCCESSFUL

                        //get the tasks from the message
                        jacobiTempCalcTasks = (ArrayList<TemperatureCalculator>) jMessage.getObject();
                        //create cell to run our tasks in
                        cell = new Cell(this, jacobiTempCalcTasks);
                        final Cell finalCell = cell;
                        //run tasks in background as to not block messaging.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runTasks(finalCell);
                            }
                        }).start();
                        break;
                    case JMessage.JACOBI_RUN_HALF_SUBSEQUENT:
                        if(!notRunningTasks) {
                            if(cell != null) pool.invoke(cell);
                        }
                        /*doFlipAtoB = (steps++ % 2) == 0;
                        matrixA = (doFlipAtoB) ? regionA : regionB;
                        matrixB = (doFlipAtoB) ? regionB : regionA;
                        runTasks();
                        //send the new values of the regionB to the gui server
                        JMessage jMessage1 = new JMessage(JMessage.JACOBI_SET_ALLOY_COLORS_FOR_GUI,
                                "Set new jPoint colors from server.", regionB);
                        jMessage1.setFirstCol((regionB.length / 2));
                        jMessage1.setFirstRow(0);
                        jMessage1.setLastCol(hiCol);
                        jMessage1.setLastRow(regionB[0].length);
                        //displayEvent("SENDING NEW JPOINTS to GuiServer from [" + guiServerClient.getUsername() + "]");
                        guiServerClient.sendMessage(jMessage1);

                        try { guiServerClient.getOutputStream().reset(); }
                        catch (IOException e) { e.printStackTrace();}
                        reinitialize();*/
                        break;
                    case JMessage.JACOBI_SET_ALLOY_COLORS_FOR_GUI:
                        ///this will be called from a client connected to the guiServer
                        //displayEvent("RECEIVED NEW JPOINTS from [" + username + "]");
                        double[][] matrixHoldingNewCellTemps = (double[][]) jMessage.getObject();

                        int firstCol = jMessage.getFirstCol();
                        int lastCol = jMessage.getLastCol();

                        int firstRow = jMessage.getFirstRow();
                        int lastRow = jMessage.getLastRow();
                        //displayEvent(String.format("%s -> fC: %s fR: %s lC: %s lR: %s", username, firstCol,firstRow,lastCol,lastRow));
                        guiServerJPanel.transferPoints(matrixHoldingNewCellTemps, firstCol, lastCol, firstRow, lastRow);
                        break;
                    case JMessage.LOGOUT:
                        displayEvent(username + " disconnected with a LOGOUT message.");
                        keepGoing = false;
                        break;
                    case JMessage.WHOISIN:
                        writeMessage("List of the users connected at " + dateFormat.format(new Date()) + "\n");
                        // scan listOfClients the users connected
                        for(int i = 0; i < listOfClients.size(); ++i) {
                            ClientThread ct = listOfClients.get(i);
                            writeMessage((i + 1) + ") " + ct.username + " since " + ct.date);
                        }
                        break;
                }
            }
            // removeLoggedOutClient myself from the arrayList containing the list of the
            // connected Clients
            removeLoggedOutClient(id);
            close();
        }

        private void runTasks(Cell cell) {
            for (int i = 0; i < 200000; ++i) {
                pool.invoke(cell);
                //displayEvent(String.format("\nStep %: Client is awaiting Server Response: %s", steps, clientIsAwaitingResponse));
                if(i % 500 == 0) sendNewMatrixToGUIServer();

                if(clientIsAwaitingResponse) {
                    //displayEvent(String.format("\nStep %s: Sending Response to Client: %s", steps, clientIsAwaitingResponse));
                    new Thread(() -> {
                        //displayEvent(username + " JRH: returning resultsObject after " + steps + " jacobiTempCalcTasks");
                        sendResultToClientInBackground();
                    }).start();
                    clientIsAwaitingResponse = false;
                }
                cell.reinitialize();
            }
        }

        private void sendNewMatrixToGUIServer() {
            //send the new values of the regionB to the gui server
            JMessage jMessageToGUIServer = new JMessage(JMessage.JACOBI_SET_ALLOY_COLORS_FOR_GUI,
                    "Set new jPoint colors from server.", regionB);
            jMessageToGUIServer.setFirstCol((regionB.length / 2));
            jMessageToGUIServer.setFirstRow(0);
            jMessageToGUIServer.setLastCol(hiCol);
            jMessageToGUIServer.setLastRow(regionB[0].length);
            //displayEvent("SENDING NEW JPOINTS to GuiServer from [" + guiServerClient.getUsername() + "]");
            guiServerClient.sendMessage(jMessageToGUIServer);
            try {
                guiServerClient.getOutputStream().reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void transferMatrixColumnFromClientToServer() {
    /*regionA[regionB.length/2] = lastColumnFirstHalfOfMatrix;
    matrixA[regionB.length/2] = lastColumnFirstHalfOfMatrix;*/
            int halfOfMatrixCols = regionA.length / 2;
            for (int row = 0; row <lastColumnFirstHalfOfMatrix.length; row++) {
                regionA[halfOfMatrixCols][row] = lastColumnFirstHalfOfMatrix[row];
                matrixA[halfOfMatrixCols][row] = lastColumnFirstHalfOfMatrix[row];
            }
        }

        private boolean connectToGUIServer() {
            guiServerClient = new Client(guiServerAddress, guiPortNumber, username + "ServerGUIRunner");
            guiServerClient.displayEvent("TRYING TO CONNECT SERVER COMPONENT TO GUI SERVER");
            if (!guiServerClient.start()) {
                guiServerClient.displayEvent(String.format("#%s FAILED TO CONNECT to GUI server at [%s] on port [%s]\n", guiServerClient.getUsername(), guiServerAddress, guiPortNumber));
                System.out.println();
                try {
                    Thread.sleep(1000*1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                guiServerClient.displayEvent("# FATAL ERROR: !!!PLEASE END PROGRAM!!!");
                return false;
            }
            guiServerClient.displayEvent(String.format("#%s CONNECTED TO GUI SERVER [%s] on port[%s] SUCCESSFULLY\n", guiServerClient.getUsername(), guiServerAddress, guiPortNumber));
            return true;
        }



        private void sendResultToClientInBackground() {
            //send back our first column of our regionB on every step
            //create the message to be sent back to the client with the result
            firstColumnSecondHalfOfMatrix = regionB[((hiCol/2))];
            JMessage resultJMessage = new JMessage(JMessage.JACOBI_SERVER_RETURN_HALF_TO_CLIENT,
                    ">> Server has completed its work. This is the return message", firstColumnSecondHalfOfMatrix);


            //send result message to client
            writeMessage(resultJMessage);
        }

        /**
         * Try to close all the connections
         */
        private void close() {
            // try to close the connection
            try {
                if(outputStream != null) outputStream.close();
            }
            catch(Exception e) {}
            try {
                if(inputStream != null) inputStream.close();
            }
            catch(Exception e) {};
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        public boolean writeMessage(JMessage object){
            // if Client failed still connected send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                //if(jacobiHeartBeat++ % 10 == 0) {
                // printToConsole(String.format("Sent JPoints list of size %s to Client", ((List) object.getObject()).size()));
                outputStream.writeObject(object);
                outputStream.reset();
                //}
            }
            // if an error occurs, do not abort just inform the user
            catch(IOException e) {
                displayEvent("Error sending message to " + username);
                displayEvent(e.toString());
            }
            return true;
        }
        /*
         * Write a String to the Client output stream
         */
        public boolean writeMessage(String msg) {
            // if Client failed still connected send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                outputStream.writeObject(">> " + msg);
            }
            // if an error occurs, do not abort just inform the user
            catch(IOException e) {
                displayEvent("Error sending message to " + username);
                displayEvent(e.toString());
            }
            return true;
        }
    }

    public static void printToConsole(String event) {
        System.out.print(">>");
        System.out.print(event);
        System.out.println("\n");
    }

    class Cell extends ForkJoinTask<Double> {

        private ArrayList<TemperatureCalculator> jacobiTempCalcTasks;
        private ForkJoinPool pool = new ForkJoinPool();
        private ClientThread parent;

        public Cell(ClientThread parent, ArrayList<TemperatureCalculator> jacobiTempCalcTasks) {
            this. parent = parent;
            this.jacobiTempCalcTasks = jacobiTempCalcTasks;
        }

        @Override
        public Double getRawResult() {
            return null;
        }

        @Override
        protected void setRawResult(Double aDouble) {

        }

        private void runTasks() {
            //if the task were sent
            if(jacobiTempCalcTasks != null && !jacobiTempCalcTasks.isEmpty()) {
                pool.invokeAll(jacobiTempCalcTasks);
                setRawResult(1.0);
            } else {
                setRawResult(1.0);
                System.out.println(parent.username + " JRH: failed due to null jacobiTempCalcTasks list");
            }
        }

        @Override
        protected boolean exec() {
            doFlipAtoB = (steps++ % 2) == 0;
            matrixA = (doFlipAtoB) ? regionA : regionB;
            matrixB = (doFlipAtoB) ? regionB : regionA;
            //invoke all the jacobiTempCalcTasks to do calculations
            //displayEvent(username + " JRH: running " + jacobiTempCalcTasks.size() + " jacobiTempCalcTasks");
            runTasks();
            setRawResult(1.0);
            return true;
        }
    }

    /**
     * Created by lwdthe1 on 12/7/2015.
     */
    private static MetalAlloy metalAlloy;
    private static int hiRow;
    private static int hiCol;
    private static double[][] regionA;//the matrix
    private static double[][] regionB;//the copy of the matrix that does all the work
    private static double[] lastColumnFirstHalfOfMatrix;
    private static double[] firstColumnSecondHalfOfMatrix;
    private static int steps = 0 ;
    private static boolean doFlipAtoB;
    private static double[][]matrixA;//hold persistent values of the matrix for use in B to calculate new values
    private static double[][]matrixB;// use contents of matrixA to calculate B
    public static class TemperatureCalculator implements Callable<Double>, Serializable {
        private int row;
        public TemperatureCalculator(int row) {
            this.row = row;
        }

        public void calculateTemperature(int col) {
            double sigmaTemperatureFinal = 0;
            double sigmaTemperature;
            //System.out.println(hiRow);

            //go through all cells in each columns across this col and calculate their temps
            for (int row = 0; row <= hiRow; row++) {
                if (!(col == 0 && row == 0) && !(col == hiCol && row == hiRow)) {
                    int currentMetal = 1;
                    double numNeighbors = 0;
                    sigmaTemperatureFinal = 0;
                    sigmaTemperature = 0;

                    while (currentMetal < 4) {
                        sigmaTemperature = 0;
                        if (!(col - 1 < 0)) {
                            sigmaTemperature += matrixA[col - 1][row] * (metalAlloy.getRegions()[col - 1][row].getMetalPercentage(currentMetal));
                            if (currentMetal == 1) numNeighbors++;
                        }
                        if (!(col + 1 > hiCol)) {
                            sigmaTemperature += matrixA[col + 1][row] * (metalAlloy.getRegions()[col + 1][row].getMetalPercentage(currentMetal));
                            if (currentMetal == 1) numNeighbors++;
                        }
                        if (!(row - 1 < 0)) {
                            //System.out.println("[" + col + " " + j + " " + metalAlloy.getRegions()[col][j - 1].getCellTemp());
                            sigmaTemperature += matrixA[col][row - 1] * (metalAlloy.getRegions()[col][row - 1].getMetalPercentage(currentMetal));
                            if (currentMetal == 1) numNeighbors++;
                        }
                        if (!(row + 1 > hiRow)) {
                            sigmaTemperature += matrixA[col][row + 1] * (metalAlloy.getRegions()[col][row + 1].getMetalPercentage(currentMetal));
                            if (currentMetal == 1) numNeighbors++;
                        }
                        sigmaTemperatureFinal += (metalAlloy.getRegions()[col][row].getThermConstant(currentMetal) * sigmaTemperature) / numNeighbors;
                        currentMetal++;
                    }

                    matrixB[col][row] = sigmaTemperatureFinal;
                    //metalAlloy.getRegions()[col][row].setCellTemp(sigmaTemperatureFinal);
                }
                //System.out.println("[" + col + " " + row + " " + sigmaTemperatureFinal);
            }
        }

        @Override
        public Double call() throws Exception {
            this.calculateTemperature(row);
            return null;
        }

        /*public double getResult() {
            return sigmaTemperatureFinal;
        }*/
    }

}

