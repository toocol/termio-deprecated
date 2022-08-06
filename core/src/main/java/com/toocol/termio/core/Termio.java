package com.toocol.termio.core;

import com.toocol.termio.core.cache.MoshSessionCache;
import com.toocol.termio.core.cache.SshSessionCache;
import com.toocol.termio.core.cache.StatusCache;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.core.shell.core.ShellCharEventDispatcher;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermCharEventDispatcher;
import com.toocol.termio.core.term.handlers.BlockingAcceptCommandHandler;
import com.toocol.termio.utilities.anis.Printer;
import com.toocol.termio.utilities.config.IniConfigLoader;
import com.toocol.termio.utilities.functional.VerticleDeployment;
import com.toocol.termio.utilities.jni.JNILoader;
import com.toocol.termio.utilities.log.FileAppender;
import com.toocol.termio.utilities.log.Logger;
import com.toocol.termio.utilities.log.LoggerFactory;
import com.toocol.termio.utilities.utils.CastUtil;
import com.toocol.termio.utilities.utils.ClassScanner;
import com.toocol.termio.utilities.utils.MessageBox;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import sun.misc.Signal;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.toocol.termio.core.term.TermAddress.ACCEPT_COMMAND;
import static com.toocol.termio.core.term.TermAddress.MONITOR_TERMINAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/1 10:13
 */
public class Termio {
    public enum RunType {
        CONSOLE,
        DESKTOP
    }

    protected static final long BLOCKED_CHECK_INTERVAL = 30 * 24 * 60 * 60 * 1000L;
    protected static final Logger logger = LoggerFactory.getLogger(Termio.class);

    protected static CountDownLatch initialLatch;
    protected static CountDownLatch loadingLatch;
    protected static List<Class<? extends AbstractVerticle>> verticleClassList = new ArrayList<>();

    protected static RunType runType;
    protected static Vertx vertx;
    protected static EventBus eventBus;

    static {
        /* Get the verticle which need to deploy in main class by annotation */
        Set<Class<?>> annotatedClassList = new ClassScanner("com.toocol.termio.core", clazz -> clazz.isAnnotationPresent(VerticleDeployment.class)).scan();
        annotatedClassList.forEach(annotatedClass -> {
            if (annotatedClass.getSuperclass().equals(AbstractVerticle.class)) {
                verticleClassList.add(CastUtil.cast(annotatedClass));
            } else {
                Printer.printErr("Skip deploy verticle " + annotatedClass.getName() + ", please extends AbstractVerticle");
            }
        });
        initialLatch = new CountDownLatch(verticleClassList.size());
        loadingLatch = new CountDownLatch(1);
    }

    public static void run() {
        runType = RunType.CONSOLE;
        /* Block the Ctrl+C */
        Signal.handle(new Signal("INT"), signal -> {
        });

        componentInitialise(System.out);
        Term.initializeReader(null);
        Shell.initializeReader(null);
        IniConfigLoader.setConfigFileRootPath("/config");
        IniConfigLoader.setConfigurePaths(new String[]{"com.toocol.termio.core.config.core"});
        Printer.printLoading(loadingLatch);

        vertx = prepareVertxEnvironment(null);
        eventBus = vertx.eventBus();

        addShutdownHook();
        waitingStart();
    }

    public static RunType runType() {
        return runType;
    }

    public static Vertx vertx() {
        return vertx;
    }

    public static EventBus eventBus() {
        return eventBus;
    }

    protected static void componentInitialise(PrintStream printStream) {
        if (runType.equals(RunType.CONSOLE)) {
            JNILoader.load();
        }
        TermCharEventDispatcher.init();
        ShellCharEventDispatcher.init();
        Printer.setPrinter(printStream);
    }

    protected static Vertx prepareVertxEnvironment(Set<Class<? extends AbstractVerticle>> ignore) {
        /* Because this program involves a large number of IO operations, increasing the blocking check time, we don't need it */
        VertxOptions options = new VertxOptions()
                .setBlockedThreadCheckInterval(BLOCKED_CHECK_INTERVAL);
        final Vertx vertx = Vertx.vertx(options);
        LoggerFactory.init(vertx);

        /* Deploy the verticle */
        if (ignore != null && !ignore.isEmpty()) {
            verticleClassList = new ArrayList<>(verticleClassList
                    .stream()
                    .filter(clazz -> !ignore.contains(clazz))
                    .toList());
        }
        verticleClassList.sort(Comparator.comparingInt(clazz -> -1 * clazz.getAnnotation(VerticleDeployment.class).weight()));
        verticleClassList.forEach(verticleClass -> {
                    VerticleDeployment deploy = verticleClass.getAnnotation(VerticleDeployment.class);
                    DeploymentOptions deploymentOptions = new DeploymentOptions();
                    if (deploy.worker()) {
                        deploymentOptions.setWorker(true).setWorkerPoolSize(deploy.workerPoolSize()).setWorkerPoolName(deploy.workerPoolName());
                    }
                    vertx.deployVerticle(verticleClass.getName(), deploymentOptions, result -> {
                        if (result.succeeded()) {
                            initialLatch.countDown();
                        } else {
                            MessageBox.setExitMessage("Termio start up failed, verticle = " + verticleClass.getSimpleName());
                            Printer.print("Termio start up failed, verticle = " + verticleClass.getSimpleName());
                            vertx.close();
                            System.exit(-1);
                        }
                    });
                }
        );
        return vertx;
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

    protected static void waitingStart() {
        try {
            boolean ret = initialLatch.await(30, TimeUnit.SECONDS);
            if (!ret) {
                throw new RuntimeException("Waiting timeout.");
            }
            while (true) {
                if (Printer.LOADING_ACCOMPLISH) {
                    loadingLatch.await();
                    vertx.eventBus().send(MONITOR_TERMINAL.address(), null);
                    vertx.eventBus().send(ACCEPT_COMMAND.address(), BlockingAcceptCommandHandler.FIRST_IN);

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
