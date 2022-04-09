package com.toocol.ssh;

import javafx.application.Platform;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:55
 * @version: 1
 */
public class FileChooseTest {

    public static void main(String[] args) {
        Platform.startup(() -> {
            FileChooser fileChooser = new FileChooser();
            List<File> files = fileChooser.showOpenMultipleDialog(null);
            if (files != null && files.size() > 0) {
                for (File file : files) {
                    System.out.println(file.getName());
                }
            }
        });
    }

}
