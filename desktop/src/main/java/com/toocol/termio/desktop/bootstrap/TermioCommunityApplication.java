package com.toocol.termio.desktop.bootstrap;

import com.toocol.termio.desktop.ui.panel.CentralPanel;
import com.toocol.termio.desktop.ui.panel.LeftSidePanel;
import com.toocol.termio.desktop.ui.panel.WorkspacePanel;
import com.toocol.termio.desktop.ui.sidebar.SessionManageSidebar;
import com.toocol.termio.platform.component.*;
import com.toocol.termio.platform.css.CssFileAnnotationParser;
import com.toocol.termio.platform.css.RegisterCssFile;
import com.toocol.termio.platform.ui.TScene;
import com.toocol.termio.utilities.log.Loggable;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;

/**
 * UI Structures:<br>
 * <ul>
 *     <li>
 *         <b>Stage - Scene</b>
 *         <ul>
 *             <li>
 *                 <b>CentralPanel</b>
 *                 <ul>
 *                     <li><b>SessionManageSidebar</b></li>
 *                     <li>
 *                         <b>WorkspacePanel</b>
 *                         <ul>
 *                             <li><b>HomepagePanel</b>: Home page panel</li>
 *                             <li>
 *                                 <b>CommandExecutor</b>: Term Command Executor panel
 *                                 <ul>
 *                                     <li>CommandExecutorInput</li>
 *                                     <li>
 *                                         CommandExecutorResultScrollPane
 *                                         <ul>
 *                                             <li>CommandExecutorResultTextArea</li>
 *                                         </ul>
 *                                     </li>
 *                                 </ul>
 *                             </li>
 *                             <li>
 *                                 <b>DesktopTerminal</b>: The Terminal component
 *                                 <ul>
 *                                     <li>TerminalScrollPane</li>
 *                                     <ul>
 *                                          <li>TerminalConsoleTextArea</li>
 *                                     </ul>
 *                                 </ul>
 *                             </li>
 *                         </ul>
 *                     </li>
 *                 </ul>
 *             </li>
 *         <ul/>
 *     </li>
 * </ul>
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/28 0:42
 * @version: 0.0.1
 */
@RegisterComponent(
        value = {
                @Component(clazz = SessionManageSidebar.class, id = 1, initialVisible = true),
                @Component(clazz = LeftSidePanel.class, id = 1, initialVisible = true),
                @Component(clazz = WorkspacePanel.class, id = 1, initialVisible = true),
        }
)
@RegisterCssFile(
        name = {
                "termio.css"
        }
)
public class TermioCommunityApplication extends Application implements Loggable {
    private static final ComponentsParser componentParser = new ComponentsParser();
    private static final CssFileAnnotationParser cssParser = new CssFileAnnotationParser();

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void init() throws Exception {
        TermioCommunityBootstrap.runDesktop(TermioCommunityApplication.class);
    }

    @Override
    public void start(Stage stage) throws IOException {
        info("Starting termio-community success.");
        stage.initStyle(StageStyle.UNIFIED);
        stage.setTitle(" Termio Community ");
        stage.setMinWidth(500);
        stage.setMinHeight(400);
        InputStream image = TermioCommunityApplication.class.getResourceAsStream("/com.toocol.termio.desktop.bootstrap/icon.png");
        if (image != null) {
            stage.getIcons().add(new Image(image));
        }

        CentralPanel centralPanel = new CentralPanel(1);
        TScene scene = new TScene(1, centralPanel);
        centralPanel.initialize();

        cssParser.parse(this.getClass(), scene);
        componentParser.parse(this.getClass());
        componentParser.initializeAll();

        centralPanel.setCenter(componentParser.get(WorkspacePanel.class));
        centralPanel.setLeft(componentParser.get(LeftSidePanel.class));

        stage.setScene(scene);
        stage.show();

        ComponentsContainer.getComponents()
                .stream()
                .filter(component -> component instanceof IActionAfterShow)
                .map(component -> (IActionAfterShow) component)
                .forEach(IActionAfterShow::actionAfterShow);

        TermioCommunityBootstrap.actionOnUiInitialized();

        System.gc();
    }

    @Override
    public void stop() throws Exception {
        info("Shutdown termio-community.");
        TermioCommunityBootstrap.stop();
    }
}