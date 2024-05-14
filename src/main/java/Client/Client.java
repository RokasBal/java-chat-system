package Client;

import Resources.Message;
import javafx.application.Platform;
import javafx.scene.control.TableView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class, responsible for handling the client-side communication.
 */

public class Client extends Thread {
    private Socket socket;                                 /** Socket, used for communication with the server */
    private PrintWriter out;                               /** Writer, used for writing to the server */
    private BufferedReader in;                             /** Reader, used for reading from the server */
    private TableView<Message> chatTable;                  /** Table, used for displaying the chat messages */
    private String username;                               /** Username of the client */

    private List<String> usernameList = new ArrayList<>(); /** List of all users' usernames */
    private List<String> roomList = new ArrayList<>();     /** List of all created rooms */

    private TableView<String> userTable;                   /** Table, used for displaying the list of online users */

    private ClientController controller;                   /** Controller, responsible for handling the client-side window */

    /**
     * Constructor, initializing the client thread.
     *
     * @param ip The IP address of the server.
     * @param port The port of the server.
     * @param username The username of the client.
     * @param chatTable The table, used for displaying the chat messages.
     * @param userTable The table, used for displaying the list of online users.
     * @param controller The controller, responsible for handling the client-side window.
     */

    public Client(String ip, int port, String username, TableView<Message> chatTable, TableView<String> userTable, ClientController controller) {
        try {
            this.chatTable = chatTable;
            this.username = username;
            this.userTable = userTable;
            this.controller = controller;
            socket = new Socket(ip, port);
            System.out.println("Connected to server: " + ip + ":" + port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(username);
        } catch (IOException e) {
            System.out.println("Error in the client: " + e.getMessage());
            close();
        }
    }

    /**
     * Method, used for sending a message to the server.
     *
     * @param message Message to be sent to the server.
     */

    public void sendMessage(String message) {
        System.out.println("Sending message to server: " + message);
        out.println(message);
        out.flush();
    }

    /**
     * Method, used for running the client thread.
     */

    @Override
    public void run() {
        try {
            String serverResponse;
            while (!Thread.currentThread().isInterrupted() && (serverResponse = in.readLine()) != null) {
                System.out.println("Received message from server: " + serverResponse);

                /** Filter out messages based on what's being sent */
                if (serverResponse.startsWith("/userlist ")) { // If the server sends a list of usernames
                    usernameList = List.of(serverResponse.substring(10).split(","));
                    updateUserTable();
                    System.out.println("Received list of usernames: " + usernameList);
                } else if (serverResponse.startsWith("/newRoom ")) { // If the server sends a new room
                    roomList = List.of(serverResponse.substring(9).split(","));
                    updateRoomTable();
                    System.out.println("Received new room: " + roomList);
                } else { // Else if the server sends a message
                    final Message finalMessage;
                    String[] parts = serverResponse.split(":", 3);
                    String room = parts[0];
                    String user = parts[1];
                    String message = parts[2];
                    finalMessage = new Message(room, user, message);
                    Platform.runLater(() -> {
                        controller.addMessage(finalMessage);
                    });
                }
            }
        } catch (IOException e) {
            System.err.println("Error in client thread: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close();
        }
    }

    /**
     * Method, used for closing the client.
     */

    public void close() {
        try {
            if (socket != null) {
                socket.close();
                System.out.println("socket closed.");
            }
            if (in != null) {
                in.close();
                System.out.println("in closed.");
            }
            if (out != null) {
                out.close();
                System.out.println("out closed.");
            }
            Thread.currentThread().interrupt();
            System.out.println("Client closed.");
        } catch (IOException e) {
            System.out.println("Error while closing the client: " + e.getMessage());
        }
    }

    /**
     * Method, used for updating the list of online users.
     */

    public void updateUserTable() {
        Platform.runLater(() -> {
            userTable.getItems().clear();
            userTable.getItems().addAll(usernameList);
        });
    }

    /**
     * Method, used for updating the list of rooms.
     */

    public void updateRoomTable() {
        Platform.runLater(() -> {
            controller.getRoomTable().getItems().clear();
            controller.getRoomTable().getItems().addAll(roomList);
        });
    }
}