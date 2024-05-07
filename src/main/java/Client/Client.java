package Client;

import Resources.Message;
import javafx.application.Platform;
import javafx.scene.control.TableView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private TableView<Message> chatTable;

    public Client(String ip, int port, TableView<Message> chatTable) {
        try {
            this.chatTable = chatTable;
            socket = new Socket(ip, port);
            System.out.println("Connected to server: " + ip + ":" + port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error in the client: " + e.getMessage());
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
            while ((serverResponse = in.readLine()) != null) {
                System.out.println("Received message from server: " + serverResponse);
                final Message finalMessage; // Declare final reference
                String[] parts = serverResponse.split(":", 2);
                String user = parts[0];
                String message = parts[1];
                finalMessage = new Message(user, message);
                Platform.runLater(() -> {
                    chatTable.getItems().add(finalMessage);
                });
            }
        } catch (IOException e) {
            System.out.println("Error in client thread: " + e.getMessage());
        } finally {
            close();
        }
    }
    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Client closed.");
        } catch (IOException e) {
            System.out.println("Error while closing the client: " + e.getMessage());
        }
    }
}