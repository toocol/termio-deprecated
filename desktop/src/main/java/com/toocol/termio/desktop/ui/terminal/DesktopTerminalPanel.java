package com.toocol.termio.desktop.ui.terminal;

import com.toocol.termio.desktop.ui.panel.WorkspacePanel;
import com.toocol.termio.platform.component.IActiveAble;
import com.toocol.termio.platform.console.MetadataPrinterOutputStream;
import com.toocol.termio.platform.console.MetadataReaderInputStream;
import com.toocol.termio.platform.ui.TAnchorPane;
import com.toocol.termio.platform.ui.TScene;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.log.Loggable;
import com.toocol.termio.utilities.utils.StrUtil;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/4 11:06
 */
public class DesktopTerminalPanel extends TAnchorPane implements IActiveAble, Loggable {
    public static volatile long currentActiveId;

    /**
     * DesktopTerminalPanel has only one MetadataReaderInputStream:
     * Get user's input data.
     */
    public static final MetadataReaderInputStream terminalReaderInputStream = new MetadataReaderInputStream();

    /**
     * Each DesktopTerminalPanel or CommandExecutorPanel has one onw MetadataPrinterOutputStream and PrintStream correspondent:
     * Feedback data.
     */
    private final MetadataPrinterOutputStream terminalWriterOutputStream = new MetadataPrinterOutputStream();
    private final PrintStream terminalPrintStream = new PrintStream(terminalWriterOutputStream);
    private final TerminalOutputService terminalOutputService = new TerminalOutputService();

    private final TerminalScrollPane terminalScrollPane;
    private final TerminalConsoleTextArea terminalConsoleTextArea;

    public DesktopTerminalPanel(long id) {
        super(id);
        terminalConsoleTextArea = new TerminalConsoleTextArea(id);
        terminalScrollPane = new TerminalScrollPane(id, terminalConsoleTextArea);
    }

    @Override
    public String[] styleClasses() {
        return new String[]{
                "desktop-terminal-panel"
        };
    }

    @Override
    public void initialize() {
        styled();
        TScene scene = findComponent(TScene.class, 1);
        WorkspacePanel workspacePanel = findComponent(WorkspacePanel.class, 1);
        workspacePanel.setCenter(findComponent(DesktopTerminalPanel.class, id));

        terminalScrollPane.initialize();
        terminalConsoleTextArea.initialize();
        terminalOutputService.start();

        maxHeightProperty().bind(workspacePanel.prefHeightProperty());
        maxWidthProperty().bind(workspacePanel.prefWidthProperty());
        prefHeightProperty().bind(workspacePanel.prefHeightProperty().multiply(0.8));
        prefWidthProperty().bind(workspacePanel.prefWidthProperty());

        getChildren().add(terminalScrollPane);

        terminalConsoleTextArea.setOnInputMethodTextChanged(event -> {
            if (StrUtil.isEmpty(event.getCommitted())) {
                return;
            }
            try {
                terminalReaderInputStream.write(event.getCommitted().getBytes(StandardCharsets.UTF_8));
                terminalReaderInputStream.flush();
            } catch (IOException e) {
                error("Write to reader failed, msg = {}", e.getMessage());
            }
        });

        terminalConsoleTextArea.setOnKeyTyped(event -> {
            if (event.isShortcutDown() || event.isControlDown() || event.isAltDown() || event.isMetaDown()) {
                return;
            }
            try {
                terminalReaderInputStream.write(event.getCharacter().getBytes(StandardCharsets.UTF_8));
                terminalReaderInputStream.flush();
            } catch (IOException e) {
                error("Write to reader failed, msg = {}", e.getMessage());
            }
        });

        terminalConsoleTextArea.setOnMouseClicked(event -> terminalConsoleTextArea.requestFocus());

        terminalConsoleTextArea.focusedProperty().addListener((ob, oldVal, newVal) -> {
            if (newVal) {
                activeTerminal();
                System.out.println("Terminal get focus");
            } else {
                System.out.println("Terminal lose focus");
            }
        });

        KeyCombination ctrlU = new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlU, terminalConsoleTextArea::clear);

        KeyCombination ctrlT = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlT, terminalConsoleTextArea::clear);
    }

    @Override
    public void actionAfterShow() {
    }

    @Override
    public void active() {

    }

    public void activeTerminal() {
        currentActiveId = id();
        Printer.setPrinter(terminalPrintStream);
    }

    @SuppressWarnings("all")
    private class TerminalOutputService implements Loggable {

        public void start() {
            Thread thread = new Thread(() -> {
                while (true) {
                    try {
                        if (terminalWriterOutputStream.available() > 0) {
                            String text = terminalWriterOutputStream.read();
                            Platform.runLater(() -> {
                                terminalConsoleTextArea.append(text);
                            });
                        }
                        Thread.sleep(1);
                    } catch (Exception e) {
                        warn("TerminalOutputService catch excetion, e = {}, msg = {}", e.getClass().getName(), e.getMessage());
                    }
                }
            }, "terminal-output-service");
            thread.setDaemon(true);
            thread.start();
        }

    }
}
