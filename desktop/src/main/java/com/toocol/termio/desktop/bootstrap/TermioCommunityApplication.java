package com.toocol.termio.desktop.bootstrap;

import com.toocol.termio.desktop.components.panel.ui.MajorPanel;
import com.toocol.termio.platform.component.*;
import com.toocol.termio.platform.css.CssFileAnnotationParser;
import com.toocol.termio.platform.css.RegisterCssFile;
import com.toocol.termio.platform.font.FontFileAnnotationParser;
import com.toocol.termio.platform.font.RegisterFontFile;
import com.toocol.termio.platform.ui.TScene;
import com.toocol.termio.platform.window.StageHolder;
import com.toocol.termio.platform.window.WindowSizeAdjuster;
import com.toocol.termio.utilities.log.Loggable;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;

/**
 * Application entrance of Termio Community.
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/28 0:42
 * @version: 0.0.1
 */
@RegisterComponent(value = {
        @Component(clazz = MajorPanel.class, id = 1, initialVisible = true),
})
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
public class TermioCommunityApplication extends Application implements Loggable {
    private ComponentsParser componentParser = new ComponentsParser();
    private CssFileAnnotationParser cssParser = new CssFileAnnotationParser();
    private FontFileAnnotationParser fontParser = new FontFileAnnotationParser();

    public static void main(String[] args) {
        System.setProperty("javafx.preloader", "com.toocol.termio.desktop.bootstrap.TermioPreloader");
        launch();
    }

    @Override
    public void init() throws Exception {
        TermioCommunityBootstrap.runDesktop(TermioCommunityApplication.class);
    }

    @Override
    public void start(Stage stage) throws IOException {
        StageHolder.stage = stage;
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Termio Community");
        stage.setMinWidth(500);
        stage.setMinHeight(400);
        InputStream image = TermioCommunityApplication.class.getResourceAsStream("/com.toocol.termio.desktop.bootstrap/icon.png");
        if (image != null) {
            stage.getIcons().add(new Image(image));
        }

        componentParser.parse(this.getClass());
        Node root = componentParser.getAsNode(MajorPanel.class);
        assert root != null;

        TScene scene = new TScene(1, (Parent) root);
        WindowSizeAdjuster.Instance.init(stage, root);

        fontParser.parse(this.getClass());
        cssParser.parse(this.getClass(), scene);
        componentParser.initializeAll();

        stage.setScene(scene);
        stage.show();
        notifyPreloader(new TermioPreloader.ApplicationStartupNotification());

        ComponentsContainer.getComponents()
                .stream()
                .filter(component -> component instanceof IActionAfterShow)
                .map(component -> (IActionAfterShow) component)
                .forEach(IActionAfterShow::actionAfterShow);

        info("Starting termio-community success.");

        componentParser = null;
        cssParser = null;
        fontParser = null;
        System.gc();
    }

    @Override
    public void stop() throws Exception {
        info("Shutdown termio-community.");
        TermioCommunityBootstrap.stop();
    }
}