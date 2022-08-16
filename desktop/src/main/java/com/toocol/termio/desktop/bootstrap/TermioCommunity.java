package com.toocol.termio.desktop.bootstrap;

import com.toocol.termio.core.Termio;
import com.toocol.termio.core.cache.MoshSessionCache;
import com.toocol.termio.core.cache.SshSessionCache;
import com.toocol.termio.core.cache.StatusCache;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermPrinter;
import com.toocol.termio.desktop.ui.executor.CommandExecutorPanel;
import com.toocol.termio.desktop.ui.terminal.DesktopConsole;
import com.toocol.termio.desktop.ui.terminal.DesktopTerminalPanel;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.config.IniConfigLoader;
import com.toocol.termio.utilities.console.Console;
import com.toocol.termio.utilities.functional.Ignore;
import com.toocol.termio.utilities.log.FileAppender;
import com.toocol.termio.utilities.utils.MessageBox;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.toocol.termio.core.term.TermAddress.ACCEPT_COMMAND_DESKTOP;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/2 0:34
 * @version: 0.0.1
 */
public class TermioCommunity extends Termio {

    public static void runDesktop(Class<?> runClass) {
        runType = RunType.DESKTOP;
        Console.setConsole(new DesktopConsole());
        IniConfigLoader.setConfigFileRootPath("/config");
        IniConfigLoader.setConfigurePaths(new String[]{"com.toocol.termio.desktop.configure"});

        componentInitialise();
        Term.initializeReader(CommandExecutorPanel.executorReaderInputStream);
        Shell.initializeReader(DesktopTerminalPanel.terminalReaderInputStream);
        TermPrinter.registerPrintStream(CommandExecutorPanel.commandExecutorPrintStream);
        loadingLatch.countDown();

        vertx = prepareVertxEnvironment(
                Optional.ofNullable(runClass.getAnnotation(Ignore.class))
                .map(ignore -> Arrays.stream(ignore.ignore()).collect(Collectors.toSet()))
                .orElse(null)
        );
        eventBus = vertx.eventBus();

        waitingStartDesktop();
    }

    public static void stop() {
        StatusCache.STOP_PROGRAM = true;
        if (MessageBox.hasExitMessage()) {
            Printer.println(MessageBox.exitMessage());
        }
        SshSessionCache.getInstance().stopAll();
        MoshSessionCache.getInstance().stopAll();
        FileAppender.close();
        vertx.close();
    }


    public static void waitingStartDesktop() {
        try {
            boolean ret = initialLatch.await(30, TimeUnit.SECONDS);
            if (!ret) {
                throw new RuntimeException("Waiting timeout.");
            }
            while (true) {
                if (Printer.LOADING_ACCOMPLISH) {
                    loadingLatch.await();
                    vertx.eventBus().send(ACCEPT_COMMAND_DESKTOP.address(), null);

                    loadingLatch = null;
                    initialLatch = null;
                    verticleClassList = null;
                    System.gc();
                    logger.info("Start termio success.");
                    break;
                }
            }
        } catch (Exception e) {
            vertx.close();
            MessageBox.setExitMessage("Termio start up error.");
            System.exit(-1);
        }
    }

}
