package com.chat.javachatsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Server server;

    public ClientHandler(Socket socket, Server server) {
        this.clientSocket = socket;
        this.server = server;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error initializing streams: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        try {
            out.println(message);
            out.flush();
            System.out.println("Sent message to client: " + message);
        } catch (Exception e) {
            System.out.println("Error sending message to client: " + e.getMessage());
        }
    }

    public void run() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);
                server.broadcastMessage(inputLine, this);
            }

            System.out.println("Client disconnected: " + clientSocket);
            server.removeClient(this);
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
            server.removeClient(this);
        }
    }
}