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

 /**
 * Class, responsible for handling the client-side window.
 */

public class ClientController implements Initializable {
    private String ip;                      /** IP address of the server */
    private String username;                /** Username of the client */
    private int port;                       /** Port of the server */

    private Stage stage;                    /** Stage, used for the client-side window */

    @FXML
    private TextArea messageBox;            /** Text area, used for typing out messages */
    @FXML
    private TableView<String> roomTable;    /** Table, used for displaying the list of created chat rooms */
    @FXML
    private TableView<String> userTable;    /** Table, used for displaying the list of online users available for private messages */
    @FXML
    private TableView<Message> chatTable;   /** Table, used for displaying the chat messages for a given room */
    @FXML
    private Button sendButton;              /** Button, used for sending messages */
    @FXML
    private Button makeRoomButton;          /** Button, used for creating a new chat room */

    private Client client;                  /** Client thread, responsible for handling the client-side communication */

    private String selectedRoom = null;     /** Currently selected chat room */

    private Map<String, ObservableList<Message>> roomMessages = new HashMap<>();  /** Map, containing the messages for each chat room */

    // to-do: fix the selection not working for previously selected rooms inside of the opposite table.
    AtomicReference<String> previousRoomSelection = new AtomicReference<>(null);  /** Reference, used for storing the previously selected chat room */
    AtomicReference<String> previousUserSelection = new AtomicReference<>(null);  /** Reference, used for storing the previously selected user */

     /**
      * Method, used for initializing the client-side window.
      *
      * @param url The URL, used for initializing the window.
      * @param resourceBundle The resource bundle, used for initializing the window.
      */

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeChatTable();
        initializeUserTable();
        initializeRoomTable();

        // to-do: fix this
        messageBox.setWrapText(true);

        // Set initial room.
        selectedRoom = "Room 1";
        roomMessages.put(selectedRoom, FXCollections.observableArrayList());
        switchRoom(selectedRoom);

        // Set on-action events for buttons.
        sendButton.setOnAction(this::sendMessage);
        makeRoomButton.setOnAction(this::makeNewRoom);

        // Start the client thread;
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

     /**
      * Method, used for starting the client thread.
      */

    private void start() {
        client = new Client(ip, port, username, chatTable, userTable, this);
        client.start();
    }

    /**
     * Method, used for sending a message to the server.
     *
     * @param actionEvent The event, triggered by the user.
     */

    private void sendMessage(ActionEvent actionEvent) {
        String message = messageBox.getText();
        Message sentMessage = new Message(selectedRoom, username, message);

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

    /**
     * Method, used for switching to a different chat room.
     *
     * @param roomName The name of the currently selected chat room.
     */

    private void switchRoom(String roomName) {
        chatTable.getItems().clear();

        selectedRoom = roomName;

        if (roomName == null) return;

        ObservableList<Message> messages = roomMessages.get(roomName);

        if (messages != null) {
            Platform.runLater(() -> {
                chatTable.getItems().addAll(messages);
            });
        }
    }

     /**
      * Method, used for adding a received message to the chat room.
      * This includes filtering for private messages.
      *
      * @param message The message, to be added to the chat room.
      */

    public void addMessage(Message message) {
        String roomName;
        ObservableList<Message> messages;

        // If the message is a private message, check if it's for the current user
        if (!message.getRoom().startsWith("Room ")) {
            if (Objects.equals(message.getRoom(), username)) {
                roomName = message.getUser();
            } else return;
        } else {
            roomName = message.getRoom();
        }
        
        messages = roomMessages.get(roomName);
        if (messages == null) {
            messages = FXCollections.observableArrayList();
            roomMessages.put(roomName, messages);
        }
        messages.add(message);

        if (roomName.equals(selectedRoom)) {
            ObservableList<Message> finalMessages = messages;
            Platform.runLater(() -> {
                if (chatTable.getItems().isEmpty()) {
                    chatTable.getItems().addAll(finalMessages);
                } else {
                    chatTable.getItems().add(message);
                }
            });
        }
    }

     /**
      * Method, used for creating a new chat room.
      *
      * @param actionEvent The event, triggered by the user.
      */

    public void makeNewRoom(ActionEvent actionEvent) {
        String lastRoomName = roomTable.getItems().getLast();
        String[] parts = lastRoomName.split(" ", 2);
        int lastNumber = Integer.parseInt(parts[1]);
        roomTable.getItems().add("Room " + (lastNumber + 1));
        client.sendMessage("/newRoom Room " + (lastNumber + 1));
    }

    public TableView getRoomTable() {
        return roomTable;
    }

    /**
     * Method, used for initializing a chat room.
     *
     * @param roomName The name of the new chat room.
     */

    private void initializeRoom(String roomName) {
        if (!roomMessages.containsKey(roomName)) {
            roomMessages.put(roomName, FXCollections.observableArrayList());
        }
    }

    /**
     * Method, used for initializing the chat table, used for displaying messages.
     */

    private void initializeChatTable() {
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
    }

     /**
      * Method, for initializing the user table, used for displaying currently online users.
      */

    private void initializeUserTable() {
        TableColumn<String, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        userTable.getColumns().add(usernameColumn);

        userTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.equals(previousUserSelection.get())) {
                    // If the newly selected item is the same as the previously selected one, return
                    return;
                }
                previousUserSelection.set(newValue);
                roomTable.getSelectionModel().clearSelection();
                selectedRoom = newValue; // Assuming selectedUser represents the room name
                switchRoom(newValue);
                initializeRoom(newValue);
                System.out.println("Selected user: " + newValue);
            }
        });
    }

     /**
      * Method, for initializing the room table, used for displaying the list of chat rooms.
      */

    private void initializeRoomTable() {
        TableColumn<String, String> roomNameColumn = new TableColumn<>("Room name");
        roomNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        roomTable.getColumns().add(roomNameColumn);

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
                switchRoom(newValue);
                initializeRoom(newValue);
                System.out.println("Selected room: " + newValue);
            }
        });
    }
}
