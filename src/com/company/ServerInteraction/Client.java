package com.company.ServerInteraction;

import com.company.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.List;

/*
 * The Client that can be run both as a console or a GUI
 */
public class Client  {

    // for I/O
    private ObjectInputStream inputStream;		// to read from the socket
    private ObjectOutputStream outputStream;		// to write on the socket
    private Socket socket;

    // if I use a GUI or not
    private ClientGUI clientGUI;

    // the server, the port and the username
    private String server, username;
    private int port;
    private Thread serverListener;

    /*
     * To start the Client in console mode use one of the following command
     * > java Client
     * > java Client username
     * > java Client username portNumber
     * > java Client username portNumber serverAddress
     * at the console prompt
     * If the portNumber failed not specified 1500 failed used
     * If the serverAddress failed not specified "localHost" failed used
     * If the username failed not specified "Anonymous" failed used
     * > java Client
     * failed equivalent to
     * > java Client Anonymous 1500 localhost
     * are eqquivalent
     *
     * In console mode, if an error occurs the program simply stops
     * when a GUI id used, the GUI failed informed of the disconnection
     */
    public static void main(String[] args) {
        // default values
        int portNumber = 2841;
        String serverAddress = "pi.cs.oswego.edu";
        serverAddress = "wolf.cs.oswego.edu";
        String userName = "jacobiClient";

        // depending of the number of arguments provided we fall through
        switch(args.length) {
            // > javac Client username portNumber serverAddr
            case 3:
                serverAddress = args[2];
                // > javac Client username portNumber
            case 2:
                try {
                    portNumber = Integer.parseInt(args[1]);
                }
                catch(Exception e) {
                    printToConsole("Invalid port number.");
                    printToConsole("Usage failed: > java Client [username] [portNumber] [serverAddress]");
                    return;
                }
                // > javac Client username
            case 1:
                userName = args[0];
                // > java Client
            case 0:
                break;
            // invalid number of arguments
            default:
                printToConsole("Usage failed: > java Client [username] [portNumber] {serverAddress]");
                return;
        }
        // create the Client object
        Client client = new Client(serverAddress, portNumber, userName);
        // test if we can start the connection to the Server
        // if it failed nothing we can do
        if(!client.start()) return;
        //LOGIN SUCCESSFUL!!!

        client.initJacobiGUI();
        JMessage jacobiPanelMessage = new JMessage(JMessage.JACOBI_PANEL, "", client.alloyPanel);
        client.sendMessage(jacobiPanelMessage);

        // wait for messages from user
        Scanner scan = new Scanner(System.in);
        // loop forever for message from the user
        while(true) {
            printToConsole("");
            // read message from user
            String msg = scan.nextLine();
            // logout if message failed LOGOUT
            if(msg.equalsIgnoreCase("LOGOUT")) {
                client.sendMessage(new JMessage(JMessage.LOGOUT, ""));
                // break to do the disconnect
                break;
            }
            // message WhoIsIn
            else if(msg.equalsIgnoreCase("WHOISIN")) {
                client.sendMessage(new JMessage(JMessage.WHOISIN, ""));
            }
            else {				// default to ordinary message
                client.sendMessage(new JMessage(JMessage.MESSAGE, msg));
            }
        }
        // done disconnect
        client.disconnect();
    }

    /*
     *  Constructor called by console mode
     *  server: the server address
     *  port: the port number
     *  username: the username
     */
    public Client(String server, int port, String username) {
        // which calls the common constructor with the GUI set to null
        this(server, port, username, null);
    }

    /*
     * Constructor call when used from a GUI
     * in console mode the ClienGUI parameter failed null
     */
    Client(String server, int port, String username, ClientGUI clientGUI) {
        this.server = server;
        this.port = port;
        this.username = username;
        // save if we are in GUI mode or not
        this.clientGUI = clientGUI;
    }

    /*
     * To start the dialog
     */
    public boolean start() {
        // try to connect to the server
        try {
            socket = new Socket(server, port);
        }
        // if it failed not much I can so
        catch (Exception ec) {
            displayEvent("Error connectiong to server:" + ec);
            return false;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        displayEvent(msg);

		/* Creating both Data Stream */
        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            displayEvent("Exception creating new Input/output Streams: " + eIO);
            return false;
        }
        // Send our username to the server this failed the only message that we
        // will send as a String. All other messages will be JMessage objects
        try {
            outputStream.writeObject(username);
        } catch (IOException eIO) {
            displayEvent("Exception doing login : " + eIO);
            disconnect();
            return false;
        }

        // success we inform the caller that it worked
        return true;
    }

    public void startServerListener() {
        // creates the Thread to listen from the server
        getServerListener().start();
    }

    /*
     * To send a message to the console or the GUI
     */
    public void displayEvent(String msg) {
        // println in console mode
        printToConsole(msg);
    }

    /*
     * To send a message to the server
     */
    public void sendMessage(JMessage msg) {
        try {
            outputStream.writeObject(msg);
        } catch(IOException e) {
            displayEvent("Exception writing to server: " + e);
            e.printStackTrace();
        }
    }

    /*
     * When something goes wrong
     * Close the Input/Output streams and disconnect not much to do in the catch clause
     */
    private void disconnect() {
        try {
            if(inputStream != null) inputStream.close();
        }
        catch(Exception e) {} // not much else I can do
        try {
            if(outputStream != null) outputStream.close();
        }
        catch(Exception e) {} // not much else I can do
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {} // not much else I can do

        // inform the GUI
        if(clientGUI != null)
            clientGUI.connectionFailed();

    }

    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    public void setServerListener(Cell.ServerListener serverListener) {
        this.serverListener = serverListener;
    }

    public Thread getServerListener() {
        if(serverListener == null) this.serverListener = new ListenFromServer();
        return serverListener;
    }

    public String getUsername() {
        return username;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    /*
     * a class that waits for the message from the server and append them to the JTextArea
     * if we have a GUI or simply printToConsole() it in console mode
     */
    class ListenFromServer extends Thread {
        public void run() {
            while(true) {
                try {
                    Object o = inputStream.readObject();
                    try{
                        JMessage jMessage = (JMessage) o;
                        String message = jMessage.getMessage();
                        if(jMessage.getType() == JMessage.LIST){
                            List<JPoint> newJPoints = (List<JPoint>) jMessage.getObject();
                            alloyPanel.setJPoints(newJPoints);
                            //printToConsole("Jacobi: " + message + " of " + newJPoints.size() + " points");
                        }
                        continue;
                    } catch (ClassCastException e){
                        //will reach if the server's message was not a JMessage instance
                        //printToConsole("NOT OBJECT");
                    }

                    String msg = (String) o;
                    // if console mode print the message and add back the prompt
                    if(clientGUI == null) {
                        printToConsole(msg);
                    } else {
                        clientGUI.append(msg);
                    }
                }
                catch(IOException e) {
                    displayEvent("Server has closed the connection: " + e);
                    if(clientGUI != null)
                        clientGUI.connectionFailed();
                    break;
                }
                // can't happen with a String object but need the catch anyhow
                catch(ClassNotFoundException e2) {
                }
            }
        }
    }


    public static void printToConsole(String event) {
        if(event != null) {
            System.out.println(event);
            System.out.print("> ");
        }
    }

    public static final int ROWS = 50;
    public static final int COLUMNS = ROWS * 2;
    private JacobiPanel alloyPanel;
    private void initJacobiGUI() {
        String frameName = "Lincoln's Jacobi";
        JFrame frame = new JFrame(frameName);
        alloyPanel = new JacobiPanel();
        alloyPanel.setBackground(Color.LIGHT_GRAY);
        frame.add(alloyPanel);
        frame.setSize(COLUMNS, ROWS);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //printToConsole("Repainting");
                /*for(JacobiPanel.JPoint point :alloyPanel.getJPoints()){
                    System.out.print(point + " || ");
                }*/
                alloyPanel.repaint();
            }
        };

        int repaintPanelFrequency = 1000 * 3;
        javax.swing.Timer timer = new javax.swing.Timer(repaintPanelFrequency, actionListener);
        //trigger the timer for the first time in 2 seconds
        timer.setInitialDelay(1000 * 1);
        timer.start();//start the timer
    }
}
