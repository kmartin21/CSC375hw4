package com.company;


import com.company.ServerInteraction.Client;
import com.company.ServerInteraction.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

public class Main {

    private static int ROWS = 200;
    private static int COLUMNS = ROWS * 2;
    private static JacobiPanel jacobiPanel;
    private static ActionListener actionListener;
    private static Timer timer;
    private static String frameName;
    private static int repaintPanelFrequency;
    static double tempS = 1000, tempT = tempS / 1.11;

    private static Client client;
    private static int portNumber = Server.portNumber;
    private static String serverAddress = "localhost";

    public static Client guiServerClient;
    private static String guiServerAddress = Server.guiServerAddress;
    private static int guiPortNumber = 2850;

    private static String userName = "Jacobi Cell Client";
    private static boolean splitWorkToServer;

    public static void main(String[] args) {
        serverAddress = "localhost";
        Scanner scanner = new Scanner(System.in);
        /*System.out.println("\n# > Enter the number of rows for Jacobi.");
        try {
            ROWS = Integer.parseInt(scanner.nextLine());
            COLUMNS = 2 * ROWS;
        } catch (Exception e){
            //bad number
        }*/

        System.out.println("# > Enter the PORT NUMBER to connect to server to split Jacobi with.");
        try { portNumber = Integer.parseInt(scanner.nextLine());}
        catch (Exception e){ portNumber = Server.portNumber;}
        if(portNumber == 1) portNumber = 2844;

        System.out.println("\n# > Enter the SERVER to split Jacobi with.");
        serverAddress = scanner.nextLine();
        if(serverAddress.equals("l")) serverAddress ="localhost";
        if(serverAddress.equals("p")) serverAddress ="pi.cs.oswego.edu";
        if(serverAddress.equals("w")) serverAddress ="wolf.cs.oswego.edu";

        if(!serverAddress.trim().isEmpty()) {
            client = new Client(serverAddress, portNumber, userName);
            // try to start the connection to the Server
            // if it failed nothing we can do
            client.displayEvent("TRYING TO CONNECT TO SERVER");
            if (!client.start()) {
                client.displayEvent(String.format("#%s FAILED TO CONNECT to server at [%s] on port [%s]\n", client.getUsername(), serverAddress, portNumber));
                System.out.println();
                try {
                    Thread.sleep(1000*1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                client.displayEvent("Running Jacobi on client only.");
                splitWorkToServer = false;
            } else {
                splitWorkToServer = true;
                //CONNECTED TO SERVER SUCCESSFULLY!!!
                client.displayEvent(String.format("#%s CONNECTED TO SERVER SUCCESSFULLY\n", Main.getClient().getUsername()));
                //connect gui client to the gui server
                guiServerAddress = serverAddress;
                guiServerClient = new Client(guiServerAddress, guiPortNumber, client.getUsername() + " GUI Runner");
                guiServerClient.displayEvent("TRY TO CONNECT TO GUI SERVER");
                if (!guiServerClient.start()) {
                    guiServerClient.displayEvent(String.format("#%s FAILED TO CONNECT to GUI server at [%s] on port [%s]\n", guiServerClient.getUsername(), guiServerAddress, guiPortNumber));
                    System.out.println();
                    try {
                        Thread.sleep(1000*1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    guiServerClient.displayEvent("Running Jacobi on client only.");
                    splitWorkToServer = false;
                }
                guiServerClient.displayEvent(String.format("#%s CONNECTED TO GUI SERVER [%s] on port[%s] SUCCESSFULLY\n", Main.getClient().getUsername(), guiServerAddress, guiPortNumber));
            }
        } else splitWorkToServer = false;

        //if we are doing all the work on the client, init our gui
        if(!splitWorkToServer)initGUI();

        tempS = 1000;
        tempT = tempS / 1.11;
        MetalAlloy region = new MetalAlloy(tempS, tempT, COLUMNS, ROWS, 1,1, 1);
        //MetalAlloy region = new MetalAlloy(tempS, tempT, COLUMNS, ROWS, 1.25,.75,1);
        double regionA [][];
        double regionB [][];
        regionA = region.createRegions();
        regionB = region.createRegions();
        int loCol = 0;
        int hiCol = COLUMNS - 1;
        int loRow = 0;
        int hiRow = ROWS - 1;

        int maxSteps = ROWS * 1000;
        Jacobi jacobi = new Jacobi(region, regionA, regionB, loCol, hiCol, loRow, hiRow, maxSteps, splitWorkToServer);
        jacobi.compute();
    }

    public static double getTempS() {
        return tempS;
    }

    private static void initGUI() {
        frameName = "Lincoln's Jacobi";
        JFrame frame = new JFrame(frameName);
        jacobiPanel = new JacobiPanel();
        jacobiPanel.setBackground(Color.black);
        frame.add(jacobiPanel);
        frame.setSize(COLUMNS, ROWS);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jacobiPanel.repaint();
            }
        };

        repaintPanelFrequency = 1000 * 1;//how often to trigger the timer
        timer = new Timer(repaintPanelFrequency, actionListener);
        //trigger the timer for the first time in 2 seconds
        timer.setInitialDelay(1000 * 1);
        timer.start();//start the timer
    }

    public static JacobiPanel getJacobiPanel() {
        return jacobiPanel;
    }

    public static Client getGuiServerClient() {
        return guiServerClient;
    }

    public static Client getClient() {
        return client;
    }

    public static double getTempT() {
        return tempT;
    }
}
