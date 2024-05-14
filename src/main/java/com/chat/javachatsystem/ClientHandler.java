package com.chat.javachatsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Class, responsible for handling an individual client server-side.
 */

public class ClientHandler extends Thread {
    private Socket clientSocket; /** Socket, used for communication with the client */
    private PrintWriter out;     /** Writer, used for writing to the client */
    private BufferedReader in;   /** Reader, used for reading from the client */
    private Server server;       /** Server, to which the client is connected */

    /**
     * Constructor, initializing the client handler.
     *
     * @param socket The socket, used for communication with the client.
     * @param server The server, to which the client is connected.
     */

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

    /**
     * Method, used for sending a message to the client.
     *
     * @param message Message, to be sent to the client.
     */

    public void sendMessage(String message) {
        try {
            out.println(message);
            out.flush();
            System.out.println("Sent message to client: " + message);
        } catch (Exception e) {
            System.out.println("Error sending message to client: " + e.getMessage());
        }
    }

    /**
     * Method, used for running the client handler thread.
     */

    public void run() {
        try {
            String inputLine;
            String username = in.readLine();
            System.out.println("User '" + username + "' connected.");

            server.addUsername(username);
            server.sendRooms(this);

            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);
                if (inputLine.startsWith("/newRoom ")) {
                    String roomName = inputLine.substring(9);
                    server.sendNewRoomName(roomName, this);
                    continue;
                }
                server.broadcastMessage(inputLine, this);
            }

            System.out.println("Client disconnected: " + clientSocket);
            server.removeClient(this);
            server.removeUsername(username);
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