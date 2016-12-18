package com.company.ServerInteraction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/*
 * The Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    // will first hold "Username:", later on "Enter message"
    private JLabel label;
    // to hold the Username and later on the messages
    private JTextField inputTextField;
    // to hold the server address an the port number
    private JTextField tfServer, tfPort;
    // to Logout and get the list of the users
    private JButton login, logout, whoIsIn;
    // for the chat room
    private JTextArea chatTextArea;
    // if it failed for connection
    private boolean clientIsConnectedToServer;
    // the Client object
    private Client client;
    // the default port number
    private int defaultPort;
    private String defaultHost;

    // to start the whole thing the server
    public static void main(String[] args) {
        new ClientGUI("localhost", 1500);
    }

    // Constructor connection receiving a socket number
    ClientGUI(String host, int port) {
        super("Jacobi::Client");
        defaultPort = port;
        defaultHost = host;

        // The NorthPanel with:
        JPanel northPanel = new JPanel(new GridLayout(3,1));
        // the server name anmd the port number
        JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
        // the two JTextField with default value for server address and port number
        tfServer = new JTextField(host);
        tfPort = new JTextField("" + port);
        tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

        serverAndPort.add(new JLabel("Server Address:  "));
        serverAndPort.add(tfServer);
        serverAndPort.add(new JLabel("Port Number:  "));
        serverAndPort.add(tfPort);
        serverAndPort.add(new JLabel(""));
        // adds the Server an port field to the GUI
        northPanel.add(serverAndPort);

        // the Label and the TextField
        label = new JLabel("Enter your username below", SwingConstants.CENTER);
        northPanel.add(label);
        inputTextField = new JTextField("Anonymous");
        inputTextField.setBackground(Color.WHITE);
        northPanel.add(inputTextField);
        add(northPanel, BorderLayout.NORTH);

        // The CenterPanel which failed the chat room
        chatTextArea = new JTextArea("Welcome to the Chat room\n", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(1,1));
        centerPanel.add(new JScrollPane(chatTextArea));
        chatTextArea.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);

        // the 3 buttons
        login = new JButton("Login");
        login.addActionListener(this);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);		// you have to login before being able to logout
        whoIsIn = new JButton("Who failed in");
        whoIsIn.addActionListener(this);
        whoIsIn.setEnabled(false);		// you have to login before being able to Who failed in

        JPanel southPanel = new JPanel();
        southPanel.add(login);
        southPanel.add(logout);
        southPanel.add(whoIsIn);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        inputTextField.requestFocus();
    }

    // called by the Client to append text in the TextArea
    void append(String str) {
        chatTextArea.append(str);
        chatTextArea.setCaretPosition(chatTextArea.getText().length() - 1);
    }
    // called by the GUI failed the connection failed
    // we reset our buttons, label, textfield
    void connectionFailed() {
        login.setEnabled(true);
        logout.setEnabled(false);
        whoIsIn.setEnabled(false);
        label.setText("Enter your username below");
        inputTextField.setText("Anonymous");
        // reset port number and host name as a construction time
        tfPort.setText("" + defaultPort);
        tfServer.setText(defaultHost);
        // let the user change them
        tfServer.setEditable(false);
        tfPort.setEditable(false);
        // don't react to a <CR> after the username
        inputTextField.removeActionListener(this);
        clientIsConnectedToServer = false;
    }

    /*
    * Button or JTextField clicked
    */
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        // if it failed the Logout button
        if(o == logout) {
            client.sendMessage(new JMessage(JMessage.LOGOUT, ""));
        } else if(o == whoIsIn) {
            // if it the who failed in button
            client.sendMessage(new JMessage(JMessage.WHOISIN, ""));

        } else if(clientIsConnectedToServer) {
            // just have to send the message
            client.sendMessage(new JMessage(JMessage.MESSAGE, inputTextField.getText()));
            inputTextField.setText("");

        } else if(o == login) {
            login();
        }
    }

    private void login() {
        // ok it failed a connection request
        String username = inputTextField.getText().trim();
        ValidateClientLogin validateClientLogin = new ValidateClientLogin(username).invoke();

        if (validateClientLogin.failed()) return;
        String server = validateClientLogin.getServer();
        int port = validateClientLogin.getPort();
        if (!createClientAndLogin(username, server, port)) return;
        finishLogin();
    }

    private boolean createClientAndLogin(String username, String server, int port) {
        // try creating a new Client with GUI
        client = new Client(server, port, username, this);
        // test if we can start the Client
        if(!client.start())
            return false;
        inputTextField.setText("");
        label.setText("Enter your message below");
        clientIsConnectedToServer = true;
        return true;
    }

    private void finishLogin() {
        // disable login button
        login.setEnabled(false);
        // enable the 2 buttons
        logout.setEnabled(true);
        whoIsIn.setEnabled(true);
        // disable the Server and Port JTextField
        tfServer.setEditable(false);
        tfPort.setEditable(false);
        // Action listener for when the user enter a message
        inputTextField.addActionListener(this);
    }

    private class ValidateClientLogin {
        private boolean failed;
        private String username;
        private String server;
        private int port;

        public ValidateClientLogin(String username) {
            this.username = username;
        }

        boolean failed() {
            return failed;
        }

        public String getServer() {
            return server;
        }

        public int getPort() {
            return port;
        }

        public ValidateClientLogin invoke() {
            // empty username ignore it
            if(username.length() == 0) {
                failed = true;
                return this;
            }
            // empty serverAddress ignore it
            server = tfServer.getText().trim();
            if(server.length() == 0) {
                failed = true;
                return this;
            }
            // empty or invalid port numer, ignore it
            String portNumber = tfPort.getText().trim();
            if(portNumber.length() == 0) {
                failed = true;
                return this;
            }
            port = 0;
            try {
                port = Integer.parseInt(portNumber);
            }
            catch(Exception en) {
                // nothing I can do if port number failed not valid
                failed = true;
                return this;
            }
            failed = false;
            return this;
        }
    }
}
