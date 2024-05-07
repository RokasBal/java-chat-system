package Client;

import com.almasb.fxgl.net.Client;
import com.almasb.fxgl.net.Server;
import com.chat.javachatsystem.ServerWindow;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginWindow extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerWindow.class.getResource("login_controller.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 327);
        stage.setTitle("test!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}