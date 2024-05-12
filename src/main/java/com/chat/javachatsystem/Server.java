package com.chat.javachatsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private List<String> usernames = new ArrayList<>();

    protected void startServer(String ip, int port) {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
            System.out.println("Server started on " + ip + ":" + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                clientHandler.start();
//                sendUserListToAllClients();
            }
        } catch (IOException e) {
            System.out.println("Error in the server: " + e.getMessage());
        }
    }

    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server stopped.");
            }
        } catch (IOException e) {
            System.out.println("Error while stopping the server: " + e.getMessage());
        }
    }

    public synchronized void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                System.out.println("broadcasting: " + message);
                client.sendMessage(message);
            }
        }
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    // Method to add a new username
    public synchronized void addUsername(String username) {
        usernames.add(username);
        sendUserListToAllClients(); // Send updated user list to all clients
    }

    // Method to remove a username
    public synchronized void removeUsername(String username) {
        usernames.remove(username);
        sendUserListToAllClients(); // Send updated user list to all clients
    }

    // Method to send the list of usernames to all clients
    public synchronized void sendUserListToAllClients() {
        String userListMessage = "/userlist " + String.join(",", usernames);
        broadcastMessage(userListMessage, null);
    }
}
