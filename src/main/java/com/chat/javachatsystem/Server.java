package com.chat.javachatsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    protected void startServer(String ip, int port) {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
            System.out.println("Server started on " + ip + ":" + port);

            while (true) {
                clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Client says: " + inputLine);
                    out.println("Server received: " + inputLine);
                }

                out.close();
                in.close();
                clientSocket.close();
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
}
