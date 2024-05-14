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

/**
 * Class, responsible for handling the login window.
 */

public class LoginController implements Initializable {
    @FXML
    private Button loginButton;       /** Button, used for logging in */
    @FXML
    private TextField ipField;        /** Field, used for entering the IP address */
    @FXML
    private TextField portField;      /** Field, used for entering the port */
    @FXML
    private TextField usernameField;  /** Field, used for entering the username */

    private static String ip;         /** IP address of the server */
    private static int port;          /** Port of the server */
    private static String username;   /** Username of the client */

    /**
     * Method, initializing the login window.
     *
     * @param url The URL of the login window.
     * @param resourceBundle The resource bundle of the login window.
     */

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loginButton.setOnAction(this::login);
    }

    /**
     * Method, used for getting the user data, needed to open a client thread.
     *
     * @param actionEvent The event of clicking the login button.
     */

    private void login(ActionEvent actionEvent) {
        ip = ipField.getText();
        port = Integer.parseInt(portField.getText());
        username = usernameField.getText();
        openClient();
    }

    /**
     * Method, used for opening the client window.
     */

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
