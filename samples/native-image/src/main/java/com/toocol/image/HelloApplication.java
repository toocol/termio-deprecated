package com.toocol.image;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        AnchorPane pane = new AnchorPane();

        Canvas canvas = new Canvas();
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.setFill(Color.BLACK);
        context.setStroke(Color.BLUE);
        context.setFont(new Font("Consolas", 9));
        context.fillText("Hello World", 0, 0);
        context.strokeText("Hello World", 0, 20);

        Text[] texts = new Text[1000];
        for (int i = 0; i < 1000; i++) {
            texts[i] = new Text("H");
        }

        pane.getChildren().add(canvas);

        Scene scene = new Scene(pane, 320, 240);
        stage.setTitle("Native Image");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}