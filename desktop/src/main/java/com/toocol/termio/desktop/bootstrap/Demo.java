package com.toocol.termio.desktop.bootstrap;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/28 20:52
 * @version: 0.0.1
 */
public class Demo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane pane = new BorderPane();
        pane.setPrefSize(1280, 800);
        Scene scene = new Scene(pane);

        VBox vBox = new VBox();
        pane.setTop(vBox);

        Button button = new Button("Button_1");
        vBox.getChildren().add(button);

        TreeItem<String> sc = new TreeItem<>("sc");
        TreeItem<String> cd = new TreeItem<>("ChenDu");
        TreeItem<String> xc = new TreeItem<>("XiChang");
        TreeItem<String> dz = new TreeItem<>("DaZhou");
        sc.getChildren().addAll(cd, xc, dz);
        TreeView<String> tree = new TreeView<>(sc);
        tree.setPrefWidth(280);
        pane.setLeft(tree);

        JMetro jMetro = new JMetro();
        jMetro.setStyle(Style.DARK);
        jMetro.setScene(scene);
        jMetro.setParent(pane);
        pane.getStyleClass().add(JMetroStyleClass.BACKGROUND);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
