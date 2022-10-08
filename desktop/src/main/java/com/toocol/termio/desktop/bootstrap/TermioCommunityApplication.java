package com.toocol.termio.desktop.bootstrap;

import com.toocol.termio.desktop.components.UIHolder;
import com.toocol.termio.desktop.components.UILayout;
import com.toocol.termio.desktop.components.panel.ui.MajorPanel;
import com.toocol.termio.platform.component.IActionAfterShow;
import com.toocol.termio.platform.component.IComponent;
import com.toocol.termio.platform.css.CssFileAnnotationParser;
import com.toocol.termio.platform.css.RegisterCssFile;
import com.toocol.termio.platform.font.FontFileAnnotationParser;
import com.toocol.termio.platform.font.RegisterFontFile;
import com.toocol.termio.platform.window.StageHolder;
import com.toocol.termio.platform.window.WindowSizeAdjuster;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.log.Loggable;
import com.toocol.termio.utilities.utils.TimeRecorder;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Application entrance of Termio Community.
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/28 0:42
 * @version: 0.0.1
 */
@RegisterCssFile(name = {
        "base.css",
        "termio.css"
})
@RegisterFontFile(name = {
        "Font-Awesome-6-Brands-Regular-400.otf",
        "Font-Awesome-6-Free-Regular-400.otf",
        "Font-Awesome-6-Free-Solid-900.otf",
        "SegMDL2.ttf",
        "segoeui.ttf",
        "segoeuib.ttf",
        "segoeuil.ttf",
        "segoeuisl.ttf",
        "seguisb.ttf",
        "Segoe-Fluent-Icons.ttf"
})
public final class TermioCommunityApplication extends Application implements Loggable {

    private CssFileAnnotationParser cssParser = new CssFileAnnotationParser();
    private FontFileAnnotationParser fontParser = new FontFileAnnotationParser();

    public static void main(String[] args) {
        System.setProperty("javafx.preloader", "com.toocol.termio.desktop.bootstrap.TermioPreloader");
        launch();
    }

    @Override
    public void init() throws Exception {
        TermioCommunityListenerRegister.INSTANCE.reg();
        TermioCommunityConfigureRegister.INSTANCE.reg();
        TermioCommunityBootstrap.run();
    }

    @Override
    public void start(Stage stage) throws IOException {
        try {
            TimeRecorder recorder = new TimeRecorder();
            StageHolder.stage = stage;
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle("Termio Community");
            stage.setMinWidth(500);
            stage.setMinHeight(400);
            InputStream image = TermioCommunityApplication.class.getResourceAsStream("/com.toocol.termio.desktop.bootstrap/icon.png");
            if (image != null) {
                stage.getIcons().add(new Image(image));
            }

            MajorPanel root = UIHolder.getMajorPanel();
            WindowSizeAdjuster.init(stage, root);

            Scene scene = new Scene(root);
            StageHolder.scene = scene;

            fontParser.parse(this.getClass());
            cssParser.parse(this.getClass(), scene);

            UILayout.loadLayout();
            Arrays.stream(UIHolder.allUIComponents()).forEach(IComponent::initialize);

            stage.setScene(scene);
            stage.show();
            notifyPreloader(new TermioPreloader.ApplicationStartupNotification());

            Arrays.stream(UIHolder.allUIComponents())
                    .filter(component -> component instanceof IActionAfterShow)
                    .map(component -> (IActionAfterShow) component)
                    .forEach(IActionAfterShow::actionAfterShow);

            cssParser = null;
            fontParser = null;
            System.gc();

            info("Starting termio-community success.");
            info("Create application UI: " + recorder.end());
            Printer.printMemoryUse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        info("Shutdown termio-community.");
        TermioCommunityBootstrap.stop();
    }
}