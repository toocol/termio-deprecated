package com.toocol.ssh;

import com.toocol.ssh.core.cache.MoshSessionCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.utilities.status.StatusCache;
import com.toocol.ssh.core.config.SystemConfig;
import com.toocol.ssh.core.shell.core.ShellCharEventDispatcher;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.TermCharEventDispatcher;
import com.toocol.ssh.core.term.handlers.BlockingAcceptCommandHandler;
import com.toocol.ssh.utilities.annotation.VerticleDeployment;
import com.toocol.ssh.utilities.jni.JNILoader;
import com.toocol.ssh.utilities.log.Logger;
import com.toocol.ssh.utilities.log.LoggerFactory;
import com.toocol.ssh.utilities.utils.CastUtil;
import com.toocol.ssh.utilities.utils.ClassScanner;
import com.toocol.ssh.utilities.utils.ExitMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.apache.commons.lang3.StringUtils;
import sun.misc.Signal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.toocol.ssh.core.term.TermAddress.ACCEPT_COMMAND;
import static com.toocol.ssh.core.term.TermAddress.MONITOR_TERMINAL;


/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 15:00
 */
public class TermioApplication {

    private static final long BLOCKED_CHECK_INTERVAL = 30 * 24 * 60 * 60 * 1000L;
    private static final Logger logger = LoggerFactory.getLogger(TermioApplication.class);

    private static CountDownLatch initialLatch;
    private static CountDownLatch loadingLatch;
    private static List<Class<? extends AbstractVerticle>> verticleClassList = new ArrayList<>();

    public static void main(String[] args) {
        /* Block the Ctrl+C */
        Signal.handle(new Signal("INT"), signal -> {});

        checkStartParam(args);
        componentInitialise();

        Printer.printLoading(loadingLatch);

        Vertx vertx = prepareVertxEnvironment();
        LoggerFactory.init(vertx);
        addShutdownHook(vertx);
        waitingStart(vertx);
    }

    static {
        /* Get the verticle which need to deploy in main class by annotation */
        Set<Class<?>> annotatedClassList = new ClassScanner("com.toocol.ssh.core", clazz -> clazz.isAnnotationPresent(VerticleDeployment.class)).scan();
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

    private static void checkStartParam(String[] args) {
        if (args.length != 1) {
            ExitMessage.setMsg("Wrong boot type.");
            System.exit(-1);
        }
        SystemConfig.BOOT_TYPE = args[0];
    }

    private static void componentInitialise() {
        JNILoader.load();
        TermCharEventDispatcher.init();
        ShellCharEventDispatcher.init();
    }

    private static Vertx prepareVertxEnvironment() {
        /* Because this program involves a large number of IO operations, increasing the blocking check time, we don't need it */
        VertxOptions options = new VertxOptions()
                .setBlockedThreadCheckInterval(BLOCKED_CHECK_INTERVAL);
        final Vertx vertx = Vertx.vertx(options);

        /* Deploy the verticle */
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
                            ExitMessage.setMsg("Termio start up failed, verticle = " + verticleClass.getSimpleName());
                            vertx.close();
                            System.exit(-1);
                        }
                    });
                }
        );
        return vertx;
    }

    private static void addShutdownHook(Vertx vertx) {
        /* Add shutdown hook */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Printer.clear();
                StatusCache.STOP_PROGRAM = true;
                if (StringUtils.isNotEmpty(ExitMessage.getMsg())) {
                    Printer.printErr(ExitMessage.getMsg());
                }
                Printer.println("Termio: shutdown");
                SshSessionCache.getInstance().stopAll();
                MoshSessionCache.getInstance().stopAll();
                vertx.close();
            } catch (Exception e) {
                Printer.println("Failed to execute shutdown hook.");
            }
        }));
    }

    private static void waitingStart(Vertx vertx) {
        try {
            boolean ret = initialLatch.await(30, TimeUnit.SECONDS);
            if (!ret) {
                throw new RuntimeException("Waiting timeout.");
            }
            while (true) {
                if (StatusCache.LOADING_ACCOMPLISH) {
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
            ExitMessage.setMsg("Termio start up error.");
            System.exit(-1);
        }
    }
}
