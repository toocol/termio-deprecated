package com.toocol.termio.desktop.ui.executor;

import com.toocol.termio.desktop.ui.panel.WorkspacePanel;
import com.toocol.termio.platform.console.MetadataPrinterOutputStream;
import com.toocol.termio.platform.console.MetadataReaderInputStream;
import com.toocol.termio.platform.ui.TBorderPane;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.log.Loggable;
import com.toocol.termio.utilities.utils.StrUtil;
import javafx.application.Platform;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 0:42
 * @version: 0.0.1
 */
public class CommandExecutorPanel extends TBorderPane implements Loggable{
    /**
     * CommandExecutorPanel has only one MetadataReaderInputStream:
     * Get user's input data.
     */
    public static final MetadataReaderInputStream executorReaderInputStream = new MetadataReaderInputStream();

    /**
     * CommandExecutorPanel has only one MetadataPrinterOutputStream and PrintStream correspondent:
     * Feedback data.
     */
    public static final MetadataPrinterOutputStream commandExecutorPrinterOutputStream = new MetadataPrinterOutputStream();
    public static final PrintStream commandExecutorPrintStream = new PrintStream(commandExecutorPrinterOutputStream);

    private final ExecutorOutputService executorOutputService = new ExecutorOutputService();

    private final CommandExecutorInput commandExecutorInput;
    private final CommandExecutorResultEscapedTextArea commandExecutorResultTextArea;
    private final CommandExecutorResultScrollPane commandExecutorResultScrollPane;

    public CommandExecutorPanel(long id) {
        super(id);
        commandExecutorInput = new CommandExecutorInput(id);
        commandExecutorResultTextArea = new CommandExecutorResultEscapedTextArea(id);
        commandExecutorResultScrollPane = new CommandExecutorResultScrollPane(id, commandExecutorResultTextArea);
    }

    @Override
    public String[] styleClasses() {
        return new String[] {
               "command-executor-panel"
        };
    }

    @Override
    public void initialize() {
        styled();
        WorkspacePanel workspacePanel = findComponent(WorkspacePanel.class, id);
        workspacePanel.setBottom(this);

        prefWidthProperty().bind(workspacePanel.prefWidthProperty().multiply(1));
        prefHeightProperty().bind(workspacePanel.prefHeightProperty().multiply(0.3));

        commandExecutorInput.initialize();
        commandExecutorResultTextArea.initialize();
        commandExecutorResultScrollPane.initialize();

        executorOutputService.start();

        commandExecutorInput.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (StrUtil.isNewLine(event.getCharacter())) {
                try {
                    executorReaderInputStream.write((commandExecutorInput.getText() + "\n").getBytes(StandardCharsets.UTF_8));
                    executorReaderInputStream.flush();
                } catch (IOException e) {
                    error("Write to reader failed, msg = {}", e.getMessage());
                }
                event.consume();
            }
        });

        commandExecutorInput.focusedProperty().addListener((ob, oldVal, newVal) -> {
            if (newVal) {
                Printer.setPrinter(commandExecutorPrintStream);
                System.out.println("Executor input get focus");
            } else {
                System.out.println("Executor input lose focus");
            }
        });

        focusedProperty().addListener((ob, oldVal, newVal) -> {
            if (newVal) {
                Printer.setPrinter(commandExecutorPrintStream);
                System.out.println("Executor get focus");
            } else {
                System.out.println("Executor lose focus");
            }
        });

        commandExecutorInput.setOnMouseClicked(event -> commandExecutorInput.requestFocus());
    }

    @Override
    public void actionAfterShow() {
        commandExecutorInput.requestFocus();
    }

    @SuppressWarnings("all")
    private class ExecutorOutputService implements Loggable {

        public void start() {
            Thread thread = new Thread(() -> {
                while (true) {
                    try {
                        if (commandExecutorPrinterOutputStream.available() > 0) {
                            String text = commandExecutorPrinterOutputStream.read();
                            Platform.runLater(() -> {
                                commandExecutorResultTextArea.append(text);
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
