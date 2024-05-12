package Client;

import Resources.Message;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private String ip;
    private String username;
    private int port;

    private Stage stage;

    @FXML
    private TextArea messageBox;
    @FXML
    private TableView<String> roomTable;
    @FXML
    private TableView<String> userTable;
    @FXML
    private TableView<Message> chatTable;
    @FXML
    private Button sendButton;

    private Client client;

    private String selectedRoom = null;

    private Map<String, ObservableList<Message>> roomMessages = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TableColumn<Message, String> userColumn = new TableColumn<>("user");
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        TableColumn<Message, String> messageColumn = new TableColumn<>("message");
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        messageColumn.setCellFactory(tc -> {
            TableCell<Message, String> cell = new TableCell<Message, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setWrapText(true); // Enable wrapping
                    }
                }
            };
            return cell;
        });
        chatTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        chatTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double userColumnWidth = 0.1 * newWidth.doubleValue();
            double messageColumnWidth = newWidth.doubleValue() - userColumnWidth;
            userColumn.setPrefWidth(userColumnWidth);
            messageColumn.setPrefWidth(messageColumnWidth);
        });

        chatTable.getColumns().addAll(userColumn, messageColumn);

        TableColumn<String, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        userTable.getColumns().add(usernameColumn);

        userTable.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                // Update the selectedUser when the selection changes
                selectedRoom = userTable.getSelectionModel().getSelectedItem();
                updateChatTable();
                switchRoom(selectedRoom);
                System.out.println("Selected user: " + selectedRoom);
            }
        });

        sendButton.setOnAction(this::sendMessage);

        messageBox.setWrapText(true);

        selectedRoom = "Room 1"; // Set initial room name
        roomMessages.put(selectedRoom, FXCollections.observableArrayList()); // Create an empty list for messages
        switchRoom(selectedRoom); // Display messages for the initial room

        Platform.runLater(() -> {
            ip = LoginController.getIP();
            port = LoginController.getPort();
            username = LoginController.getUsername();
            stage = (Stage) messageBox.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                client.close();
                Platform.exit();
            });
            start();
        });
    }

    private void start() {
        client = new Client(ip, port, username, chatTable, userTable, this);
        client.start();
    }

//    private void sendMessage(ActionEvent actionEvent) {
//        String message = messageBox.getText();
//        Message sentMessage = new Message(username, message);
//        chatTable.getItems().add(sentMessage);
//        client.sendMessage(sentMessage.getUser() + ":" + sentMessage.getMessage());
//        messageBox.clear();
//    }

    private void sendMessage(ActionEvent actionEvent) {
        String message = messageBox.getText();
        Message sentMessage = new Message(selectedRoom, username, message);

        // Add the message to the corresponding room
        ObservableList<Message> messages = roomMessages.get(selectedRoom);
        if (messages == null) {
            messages = FXCollections.observableArrayList();
            roomMessages.put(selectedRoom, messages);
        }
        messages.add(sentMessage);

        // Update UI
//        switchRoom(selectedRoom);

//        Platform.runLater(() -> {
//            chatTable.getItems().add(sentMessage);
//        });

        chatTable.getItems().add(sentMessage);

        client.sendMessage(selectedRoom + ":" + sentMessage.getUser() + ":" + sentMessage.getMessage());
        messageBox.clear();
    }

    private void updateChatTable() {
        if (selectedRoom == null) {

        }
    }

    private void switchRoom(String roomName) {
        // Clear chatTable
        chatTable.getItems().clear();

        // Get messages for the selected room
        ObservableList<Message> messages = roomMessages.get(roomName);

        // If messages for the selected room exist, set them as the items for chatTable
        if (messages != null) {
            chatTable.setItems(messages);
        } else {
            // If no messages exist for the selected room, set an empty list as the items for chatTable
            chatTable.setItems(FXCollections.observableArrayList());
        }
    }

    public String getSelectedRoom() {
        return selectedRoom;
    }

    public void addMessage(Message message) {
        String roomName = message.getRoom();
        ObservableList<Message> messages = roomMessages.get(roomName);
        if (messages == null) {
            messages = FXCollections.observableArrayList();
            roomMessages.put(roomName, messages);
        }
        messages.add(message);

        // If the room is the current room, update UI
        if (roomName.equals(selectedRoom)) {
            Platform.runLater(() -> {
                chatTable.getItems().add(message);
            });
        }
    }
}
