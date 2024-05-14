package Client;

import Resources.Message;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

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
    @FXML
    private Button makeRoomButton;

    private Client client;

    private String selectedRoom = null;

    private Map<String, ObservableList<Message>> roomMessages = new HashMap<>();

    AtomicReference<String> previousRoomSelection = new AtomicReference<>(null);
    AtomicReference<String> previousUserSelection = new AtomicReference<>(null);

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

//        userTable.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                // Update the selectedUser when the selection changes
//                roomTable.getSelectionModel().clearSelection();
//                selectedRoom = userTable.getSelectionModel().getSelectedItem();
//                updateChatTable();
//                switchRoom(selectedRoom);
//                initializeRoom(selectedRoom);
//                System.out.println("Selected user: " + selectedRoom);
//            }
//        });

        TableColumn<String, String> roomNameColumn = new TableColumn<>("Room name");
        roomNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        roomTable.getColumns().add(roomNameColumn);

//        roomTable.getSelectionModel().selectedIndexProperty().addListener(selectionListener);
//        userTable.getSelectionModel().selectedIndexProperty().addListener(selectionListener);


//        roomTable.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                // Update the selectedUser when the selection changes
//                userTable.getSelectionModel().clearSelection();
//                selectedRoom = roomTable.getSelectionModel().getSelectedItem();
//                updateChatTable();
//                switchRoom(selectedRoom);
//                initializeRoom(selectedRoom);
//                System.out.println("Selected room: " + selectedRoom);
//            }
//        });

        roomTable.getItems().add("Room 1");

        roomTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.equals(previousRoomSelection.get())) {
                    // If the newly selected item is the same as the previously selected one, return
                    return;
                }
                previousRoomSelection.set(newValue);
                userTable.getSelectionModel().clearSelection();
                selectedRoom = newValue;
                updateChatTable();
                switchRoom(newValue);
                initializeRoom(newValue);
                System.out.println("Selected room: " + newValue);
            }
        });

        userTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.equals(previousUserSelection.get())) {
                    // If the newly selected item is the same as the previously selected one, return
                    return;
                }
                previousUserSelection.set(newValue);
                roomTable.getSelectionModel().clearSelection();
                selectedRoom = newValue; // Assuming selectedUser represents the room name
                updateChatTable();
                switchRoom(newValue);
                initializeRoom(newValue);
                System.out.println("Selected user: " + newValue);
            }
        });

        sendButton.setOnAction(this::sendMessage);

        messageBox.setWrapText(true);

        selectedRoom = "Room 1"; // Set initial room name
        roomMessages.put(selectedRoom, FXCollections.observableArrayList()); // Create an empty list for messages
        switchRoom(selectedRoom); // Display messages for the initial room

        makeRoomButton.setOnAction(this::makeNewRoom);

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

        selectedRoom = roomName;

        if (roomName == null) return;

        // Get messages for the selected room
        ObservableList<Message> messages = roomMessages.get(roomName);

        // If messages for the selected room exist, set them as the items for chatTable
        if (messages != null) {
            // Set the items for chatTable on the JavaFX application thread
            Platform.runLater(() -> {
                chatTable.getItems().addAll(messages);
            });
        }
    }

    public String getSelectedRoom() {
        return selectedRoom;
    }

    public void addMessage(Message message) {
        String roomName;
        ObservableList<Message> messages;

        if (!message.getRoom().startsWith("Room ")) {
            if (Objects.equals(message.getRoom(), username)) {
                roomName = message.getUser();
            } else return;
        } else {
            roomName = message.getRoom();
        }


//        roomName = message.getRoom();
        messages = roomMessages.get(roomName);
        if (messages == null) {
            messages = FXCollections.observableArrayList();
            roomMessages.put(roomName, messages);
        }
        messages.add(message);

        // If the room is the current room, update UI
        if (roomName.equals(selectedRoom)) {
            ObservableList<Message> finalMessages = messages;
            Platform.runLater(() -> {
                // Clear chatTable only when adding the first message to the current room
                if (chatTable.getItems().isEmpty()) {
                    chatTable.getItems().addAll(finalMessages);
                } else {
                    chatTable.getItems().add(message);
                }
            });
        }
    }

    public void makeNewRoom(ActionEvent actionEvent) {
        String lastRoomName = roomTable.getItems().getLast();
        String[] parts = lastRoomName.split(" ", 2);
        int lastNumber = Integer.parseInt(parts[1]);
        roomTable.getItems().add("Room " + (lastNumber + 1));
        client.sendMessage("/newRoom Room " + (lastNumber + 1));
    }

    public void receiveRoom() {

    }

    public TableView getRoomTable() {
        return roomTable;
    }

    private void initializeRoom(String roomName) {
        if (!roomMessages.containsKey(roomName)) {
            roomMessages.put(roomName, FXCollections.observableArrayList());
        }
    }
}
