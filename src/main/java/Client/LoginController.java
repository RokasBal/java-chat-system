package Client;

import com.chat.javachatsystem.ServerWindow;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    private Button loginButton;
    @FXML
    private TextField ipField;
    @FXML
    private TextField portField;
    @FXML
    private TextField usernameField;

    private static String ip;
    private static String username;
    private static int port;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loginButton.setOnAction(this::login);
    }

    private void login(ActionEvent actionEvent) {
        ip = ipField.getText();
        port = Integer.parseInt(portField.getText());
        username = usernameField.getText();
        openClient();
    }

    private void openClient() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.close();
            FXMLLoader fxmlLoader = new FXMLLoader(ServerWindow.class.getResource("client_controller.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1677, 860);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getIP() {
        return ip;
    }

    public static String getUsername() {
        return username;
    }

    public static int getPort() {
        return port;
    }
}
