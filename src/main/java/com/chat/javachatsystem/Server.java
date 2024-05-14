package com.chat.javachatsystem;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Class, responsible for initiating the server.
 */

public class Server {
    private ServerSocket serverSocket;                        /** Server socket, initialized by the server */
    private List<ClientHandler> clients = new ArrayList<>();  /** List of currently online clients' ClientHandlers */
    private List<String> usernames = new ArrayList<>();       /** List of currently online clients' usernames */
    private List<String> rooms = new ArrayList<>();           /** List of created chat rooms */
    
    private int year, month, day, hour, minute;               /** Date values, used for creating log file */
    private BufferedWriter writer;                            /** Writer, used for writing to the log file */

    /**
     * Method, which starts the server socket.
     *
     * @param ip The IP address on which the server will be started.
     * @param port The port on which the server will be started.
     */

    protected void startServer(String ip, int port) {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
            System.out.println("Server started on " + ip + ":" + port);
            rooms.add("Room 1"); // Adding the initial chat room, that gets created by default.

            createLogFile();

            // Loop, looking for new clients.
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.out.println("Error in the server: " + e.getMessage());
        }
    }

    /**
     * Method, which stops the server socket upon closing the server window.
     */

    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server stopped.");
                writer.write("Server stopped");
                writer.close();
            }
        } catch (IOException e) {
            System.out.println("Error while stopping the server: " + e.getMessage());
        }
    }

    /**
     * Method, which sends out a message to all clients.
     *
     * @param message String, which contains the message, being sent out to all clients.
     * @param sender Sender of the message.
     * @throws IOException Throws exception, if writing to the log file fails.
     */
    public synchronized void broadcastMessage(String message, ClientHandler sender) throws IOException {
        writer.write("Broadcasting message: " + message);
        writer.newLine();
        for (ClientHandler client : clients) {
            if (client != sender) {
                System.out.println("broadcasting: " + message);
                client.sendMessage(message);
            }
        }
    }

    /**
     * Removes disconnected client from the list of currently online clients.
     *
     * @param client Client, to be removed from the currently online list.
     */

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    /**
     * Method, which adds a client username to the list of currently online clients.
     *
     * @param username Username of the client, to be added to the list of currently online clients.
     * @throws IOException Throws exception, if writing to the log file fails.
     */

    public synchronized void addUsername(String username) throws IOException {
        usernames.add(username);
        writer.write("User " + username + " has joined.");
        writer.newLine();
        sendUserListToAllClients();
    }

    /**
     * Method, which removes a client username from the list of currently online clients.
     *
     * @param username Username of the client, to be removed from the list of currently online clients.
     * @throws IOException Throws exception, if writing to the log file fails.
     */

    public synchronized void removeUsername(String username) throws IOException {
        usernames.remove(username);
        writer.write("User " + username + " has disconnected.");
        writer.newLine();
        sendUserListToAllClients();
    }

    /**
     * Method, which sends the list of currently online clients to all clients.
     *
     * @throws IOException Throws exception, if writing to the log file fails.
     */

    public synchronized void sendUserListToAllClients() throws IOException {
        String userListMessage = "/userlist " + String.join(",", usernames);
        broadcastMessage(userListMessage, null);
    }

    /**
     * Method, which sends the name of a newly created chat room to all clients.
     *
     * @param newRoom The name of the newly created chat room.
     * @param clientHandler The client handler, which created the new chat room.
     * @throws IOException Throws exception, if writing to the log file fails.
     */

    public synchronized void sendNewRoomName(String newRoom, ClientHandler clientHandler) throws IOException {
        rooms.add(newRoom);
        String roomName = "/newRoom " + String.join(",", rooms);
        writer.write(newRoom + " created.");
        writer.newLine();
        broadcastMessage(roomName, clientHandler);
    }

    /**
     * Method, which sends the list of all created rooms to new client.
     *
     * @param clientHandler The new client, which needs to receive the list of all created rooms.
     */

    public synchronized void sendRooms(ClientHandler clientHandler) {
        String roomName = "/newRoom " + String.join(",", rooms);
        for (ClientHandler client : clients) {
            if (client == clientHandler) {
                System.out.println("broadcasting: " + roomName);
                client.sendMessage(roomName);
            }
        }
    }

    /**
     * Method, which gets current time values for creating log file.
     *
     * to-do:
     * Change formatting from int to String values, add leading zeroes to values less than 10
     * to make file name more readable.
     */

    private void getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        year = now.getYear();
        month = now.getMonthValue();
        day = now.getDayOfMonth();
        hour = now.getHour();
        minute = now.getMinute();
    }

    /**
     * Method, which creates a log file for the server.
     */

    private void createLogFile() {
        getCurrentTime();
        try {
            writer = new BufferedWriter(new FileWriter("log-" + year + month + day + "-" + hour + minute + ".txt"));
        } catch (IOException e) {
            System.err.println("Error opening file: " + e.getMessage());
        }
    }
}
