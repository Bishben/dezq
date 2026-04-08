package com.dezq;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.concurrent.SynchronousQueue;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public class Main extends Application {
    // This queue 'holds' the request until the UI releases it
    public static final SynchronousQueue<String> interceptQueue = new SynchronousQueue<>();
    
    private TextArea requestArea = new TextArea();
    private Button forwardBtn = new Button("Forward Request ➔");

    @Override
    public void start(Stage stage) {
        requestArea.setEditable(false); // We'll make it editable in the next version!
        requestArea.setPromptText("Waiting for traffic...");
        forwardBtn.setDisable(true);

        // When clicked, we 'release' the blocked proxy thread
        forwardBtn.setOnAction(e -> {
            interceptQueue.offer("RELEASE"); 
            requestArea.clear();
            forwardBtn.setDisable(true);
        });

        VBox root = new VBox(10, new Label("dezq | Manual Interceptor"), requestArea, forwardBtn);
        Scene scene = new Scene(root, 600, 400);
        
        stage.setTitle("dezq Proxy");
        stage.setScene(scene);
        stage.show();

        // Start Proxy in its own thread
        new Thread(this::startProxy).start();
    }

    private void startProxy() {
        System.out.println("[dezq] Engine live on port 8888");
        DefaultHttpProxyServer.bootstrap()
            .withPort(8888)
            .withFiltersSource(new InterceptFilterSource(this))
            .start();
    }

    public void updateUI(String text) {
        Platform.runLater(() -> {
            requestArea.setText(text);
            forwardBtn.setDisable(false);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}