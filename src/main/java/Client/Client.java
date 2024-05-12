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

public class Client extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private TableView<Message> chatTable;

    private String username;

    private List<String> usernameList = new ArrayList<>();

    private TableView<String> userTable;

    public Client(String ip, int port, String username, TableView<Message> chatTable, TableView<String> userTable) {
        try {
            this.chatTable = chatTable;
            this.username = username;
            this.userTable = userTable;
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

    public void sendMessage(String message) {
        System.out.println("Sending message to server: " + message);
        out.println(message);
        out.flush(); // Ensure that the output stream is flushed
    }

    @Override
    public void run() {
        System.out.println("testas");
        try {
            String serverResponse;
            while (!Thread.currentThread().isInterrupted() && (serverResponse = in.readLine()) != null) {
                System.out.println("Received message from server: " + serverResponse);

                if (serverResponse.startsWith("/userlist ")) {
                    usernameList = List.of(serverResponse.substring(10).split(","));
                    updateUserTable();
                    System.out.println("Received list of usernames: " + usernameList);
                } else {
                    final Message finalMessage;
                    String[] parts = serverResponse.split(":", 2);
                    String user = parts[0];
                    String message = parts[1];
                    finalMessage = new Message(user, message);
                    Platform.runLater(() -> {
                        chatTable.getItems().add(finalMessage);
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
    public void close() {
        System.out.println("test message in Client.close().");
        try {
            if (socket != null) {
                socket.close(); // Close the socket first
                System.out.println("socket closed.");
            }
            if (in != null) {
                in.close(); // Close the BufferedReader
                System.out.println("in closed.");
            }
            if (out != null) {
                out.close(); // Close the PrintWriter
                System.out.println("out closed.");
            }
            Thread.currentThread().interrupt(); // Interrupt the thread
            System.out.println("Client closed.");
        } catch (IOException e) {
            System.out.println("Error while closing the client: " + e.getMessage());
        }
    }

    public void updateUserTable() {
        Platform.runLater(() -> {
            userTable.getItems().clear(); // Clear existing items
            userTable.getItems().addAll(usernameList); // Add new usernames
        });
    }
}