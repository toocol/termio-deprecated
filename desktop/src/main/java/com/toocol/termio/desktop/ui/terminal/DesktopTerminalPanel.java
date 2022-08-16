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
    private final TerminalConsoleEscapedTextArea terminalConsoleTextArea;

    private final String msg = """
            \u001B[0m\u001B[1;49r\u001B[49;1H
            \u001B[r\u001B[25;1H[root@vultrguest /]# ll -a
            total 80
            dr-xr-xr-x.  19 root root  4096 Jul  2 21:35 \u001B[0;1;34m.
            \u001B[0mdr-xr-xr-x.  19 root root  4096 Jul  2 21:35 \u001B[0;1;34m..
            \u001B[0mlrwxrwxrwx.   1 root root     7 Jun 22  2021 \u001B[0;1;36mbin\u001B[0m -> \u001B[0;1;34musr/bin
            \u001B[0mdr-xr-xr-x.   5 root root  4096 Jun 18 16:54 \u001B[0;1;34mboot
            \u001B[0mdrwxr-xr-x.   3 root root  4096 Jan 24  2021 \u001B[0;1;34mdata
            \u001B[0mdrwxr-xr-x.  19 root root  2920 Jun 22  2021 \u001B[0;1;34mdev
            \u001B[0mdrwxr-xr-x. 107 root root 12288 Jul 10 23:46 \u001B[0;1;34metc
            \u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34mhome
            \u001B[0mlrwxrwxrwx.   1 root root     7 Jun 22  2021 \u001B[0;1;36mlib\u001B[0m -> \u001B[0;1;34musr/lib
            \u001B[0mlrwxrwxrwx.   1 root root     9 Jun 22  2021 \u001B[0;1;36mlib64\u001B[0m -> \u001B[0;1;34musr/lib64
            \u001B[0mdrwx------.   2 root root 16384 Feb 13  2020 \u001B[0;1;34mlost+found
            \u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34mmedia
            \u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34mmnt
            \u001B[0mdrwxr-xr-x.   4 root root  4096 Jun 22  2021 \u001B[0;1;34mopt
            \u001B[0mdr-xr-xr-x. 101 root root     0 Apr  6 10:19 \u001B[0;1;34mproc
            \u001B[0mdr-xr-x---.  10 root root  4096 Jul 25 20:01 \u001B[0;1;34mroot
            \u001B[0mdrwxr-xr-x.  35 root root  1000 Jul 24 03:06 \u001B[0;1;34mrun
            \u001B[0mlrwxrwxrwx.   1 root root     8 Jun 22  2021 \u001B[0;1;36msbin\u001B[0m -> \u001B[0;1;34musr/sbin
            \u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34msrv
            \u001B[0mdr-xr-xr-x.  13 root root     0 Apr  6 10:19 \u001B[0;1;34msys
            \u001B[0mdrwxrwxrwt.   4 root root  4096 Jul 25 22:26 \u001B[0;30;42mtmp
            \u001B[0mdrwxr-xr-x.  13 root root  4096 Jun 18 16:48 \u001B[0;1;34musr
            \u001B[0mdrwxr-xr-x.  21 root root  4096 Jun 18 16:42 \u001B[0;1;34mvar\u001B[50;22H\u001B[0m""";

    public DesktopTerminalPanel(long id) {
        super(id);
        terminalConsoleTextArea = new TerminalConsoleEscapedTextArea(id);
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
        scene.getAccelerators().put(ctrlT, () -> {
            terminalConsoleTextArea.clear();
            terminalConsoleTextArea.appendText(msg);
        });
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
