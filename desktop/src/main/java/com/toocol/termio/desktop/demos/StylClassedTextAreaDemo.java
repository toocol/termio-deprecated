package com.toocol.termio.desktop.demos;

import com.toocol.termio.utilities.utils.CharUtil;
import com.toocol.termio.utilities.utils.StrUtil;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.InputMethodRequests;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/13 20:49
 * @version: 0.0.1
 */
public class StylClassedTextAreaDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        StyleClassedTextArea area = new StyleClassedTextArea();
        area.setUseInitialStyleForInsertion(true);
        area.setStyle("-fx-background-color: #101010;-fx-font-family: Consolas;\n" +
                "    -fx-fill: #EEEEEE; -fx-font-size:14;");
        area.setPrefHeight(800);
        area.setPrefWidth(1280);
        area.setInputMethodRequests(new InputMethodRequestsObject());

        area.setOnInputMethodTextChanged(event -> {
            if (StrUtil.isEmpty(event.getCommitted())) {
                return;
            }
            append(area, event.getCommitted());
        });

        area.setOnKeyTyped(event -> {
            if (event.isShortcutDown() || event.isControlDown() || event.isAltDown() || event.isMetaDown()) {
                return;
            }
            append(area, event.getCharacter());
        });



        Scene scene = new Scene(area);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public void append(StyleClassedTextArea area, String text) {
        List<String> classes = new ArrayList<>();
        StrUtil.splitSequenceByChinese(text).forEach(splitText -> {
            if (StrUtil.isChineseSequenceByHead(splitText)) {
                Collections.addAll(classes, "font-color-dadada", "font-family-consolas", "font-size-13");
            } else {
                Collections.addAll(classes, "font-color-dadada", "font-family-consolas", "font-size-14");
            }
            area.replace(area.getCaretPosition(), area.getCaretPosition(), StrUtil.join(splitText.toCharArray(), CharUtil.INVISIBLE_CHAR) + CharUtil.INVISIBLE_CHAR, classes);
        });
    }

    private static class InputMethodRequestsObject implements InputMethodRequests {
        @Override
        public String getSelectedText() {
            return "";
        }

        @Override
        public int getLocationOffset(int x, int y) {
            return 0;
        }

        @Override
        public void cancelLatestCommittedText() {

        }

        @Override
        public Point2D getTextLocation(int offset) {
            return new Point2D(0, 0);
        }
    }
}
