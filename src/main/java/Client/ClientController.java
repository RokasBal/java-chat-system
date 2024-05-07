package Client;

import Resources.Message;
import javafx.application.Platform;
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
    private TableView<Message> chatTable;
    @FXML
    private Button sendButton;

    private Client client;

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

        sendButton.setOnAction(this::sendMessage);

        messageBox.setWrapText(true);

        Platform.runLater(() -> {
            ip = LoginController.getIP();
            port = LoginController.getPort();
            username = LoginController.getUsername();
            start();
        });
    }

    private void start() {
        client = new Client(ip, port, chatTable);
        client.start();
        stage = (Stage) messageBox.getScene().getWindow();
        stage.setOnCloseRequest(event -> {
            client.close();
        });
    }

    private void sendMessage(ActionEvent actionEvent) {
        String message = messageBox.getText();
        Message sentMessage = new Message(username, message);
        chatTable.getItems().add(sentMessage);
        client.sendMessage(sentMessage.getUser() + ":" + sentMessage.getMessage());
        messageBox.clear();
    }
}
