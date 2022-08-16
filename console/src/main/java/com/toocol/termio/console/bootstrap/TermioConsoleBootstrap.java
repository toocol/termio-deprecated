package com.toocol.termio.console.bootstrap;

import com.toocol.termio.core.Termio;
import com.toocol.termio.core.cache.MoshSessionCache;
import com.toocol.termio.core.cache.SshSessionCache;
import com.toocol.termio.core.cache.StatusCache;
import com.toocol.termio.core.cache.StatusConstants;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermPrinter;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.config.IniConfigLoader;
import com.toocol.termio.utilities.functional.Ignore;
import com.toocol.termio.utilities.log.FileAppender;
import com.toocol.termio.utilities.utils.MessageBox;
import sun.misc.Signal;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.toocol.termio.core.term.TermAddress.ACCEPT_COMMAND_CONSOLE;
import static com.toocol.termio.core.term.TermAddress.MONITOR_TERMINAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/16 14:44
 */
public class TermioConsoleBootstrap extends Termio {

    public static void runConsole(Class<?> runClass) {
        runType = RunType.CONSOLE;
        /* Block the Ctrl+C */
        Signal.handle(new Signal("INT"), signal -> {
        });

        componentInitialise();
        Term.initializeReader(System.in);
        Shell.initializeReader(System.in);
        TermPrinter.registerPrintStream(System.out);
        IniConfigLoader.setConfigFileRootPath("/config");
        IniConfigLoader.setConfigurePaths(new String[]{"com.toocol.termio.core.config.core"});
        Printer.printLoading(loadingLatch);

        vertx = prepareVertxEnvironment(
                Optional.ofNullable(runClass.getAnnotation(Ignore.class))
                        .map(ignore -> Arrays.stream(ignore.ignore()).collect(Collectors.toSet()))
                        .orElse(null)
        );
        eventBus = vertx.eventBus();
        addShutdownHook();
        waitingStartConsole();
    }


    protected static void waitingStartConsole() {
        try {
            boolean ret = initialLatch.await(30, TimeUnit.SECONDS);
            if (!ret) {
                throw new RuntimeException("Waiting timeout.");
            }
            while (true) {
                if (Printer.LOADING_ACCOMPLISH) {
                    loadingLatch.await();
                    vertx.eventBus().send(MONITOR_TERMINAL.address(), null);
                    vertx.eventBus().send(ACCEPT_COMMAND_CONSOLE.address(), StatusConstants.FIRST_IN);

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

    protected static void addShutdownHook() {
        /* Add shutdown hook */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Printer.clear();
                StatusCache.STOP_PROGRAM = true;
                if (MessageBox.hasExitMessage()) {
                    Printer.println(MessageBox.exitMessage());
                }
                Printer.println("Termio: shutdown");
                SshSessionCache.getInstance().stopAll();
                MoshSessionCache.getInstance().stopAll();
                FileAppender.close();
                vertx.close();
            } catch (Exception e) {
                Printer.printErr("Failed to execute shutdown hook.");
            }
        }));
    }

}
