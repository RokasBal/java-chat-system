package com.chat.javachatsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerWindow extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerWindow.class.getResource("server_controller.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 791, 1039);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        Server server = new Server();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            server.startServer("localhost", 8080);
        });

        stage.setOnCloseRequest(event -> {
            executorService.shutdownNow(); // Shutdown the executor service
            if (server != null) {
                server.stopServer(); // Stop the server
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}