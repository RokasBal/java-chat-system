package com.chat.javachatsystem;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private List<String> usernames = new ArrayList<>();
    private List<String> allUsernames = new ArrayList<>();

    private List<String> rooms = new ArrayList<>();
    
    private int year, month, day, hour, minute;
    private BufferedWriter writer;

    protected void startServer(String ip, int port) {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
            System.out.println("Server started on " + ip + ":" + port);
            rooms.add("Room 1");

            LocalDateTime now = LocalDateTime.now();
            year = now.getYear();
            month = now.getMonthValue();
            day = now.getDayOfMonth();
            hour = now.getHour();
            minute = now.getMinute();

            try {
                writer = new BufferedWriter(new FileWriter("log-" + year + month + day + "-" + hour + minute + ".txt"));
            } catch (IOException e) {
                System.err.println("Error opening file: " + e.getMessage());
            }

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
                writer.write("Server stopped");
            }
        } catch (IOException e) {
            System.out.println("Error while stopping the server: " + e.getMessage());
        }
    }

    public synchronized void broadcastMessage(String message, ClientHandler sender) throws IOException {
        for (ClientHandler client : clients) {
            if (client != sender) {
                System.out.println("broadcasting: " + message);
                writer.write(message);
                writer.newLine();
                client.sendMessage(message);
            }
        }
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    // Method to add a new username
    public synchronized void addUsername(String username) throws IOException {
        usernames.add(username);
        writer.write("User " + username + " has joined.");
        writer.newLine();
        sendUserListToAllClients(); // Send updated user list to all clients
    }

    // Method to remove a username
    public synchronized void removeUsername(String username) throws IOException {
        usernames.remove(username);
        sendUserListToAllClients(); // Send updated user list to all clients
    }

    // Method to send the list of usernames to all clients
    public synchronized void sendUserListToAllClients() throws IOException {
        String userListMessage = "/userlist " + String.join(",", usernames);
        broadcastMessage(userListMessage, null);
    }

    public synchronized void sendNewRoomName(String newRoom, ClientHandler clientHandler) throws IOException {
        rooms.add(newRoom);
        String roomName = "/newRoom " + String.join(",", rooms);
        writer.write(newRoom + " created.");
        writer.newLine();
        broadcastMessage(roomName, clientHandler);
    }

    public synchronized void sendRooms(ClientHandler clientHandler) {
        String roomName = "/newRoom " + String.join(",", rooms);
        for (ClientHandler client : clients) {
            if (client == clientHandler) {
                System.out.println("broadcasting: " + roomName);
                client.sendMessage(roomName);
            }
        }
    }

    public BufferedWriter getWriter() {
        return writer;
    }
}
