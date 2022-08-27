package com.toocol.termio.core

import com.toocol.termio.core.shell.core.ShellCharEventDispatcher
import com.toocol.termio.core.term.core.TermCharEventDispatcher
import com.toocol.termio.utilities.ansi.Printer.print
import com.toocol.termio.utilities.ansi.Printer.setPrinter
import com.toocol.termio.utilities.jni.JNILoader
import com.toocol.termio.utilities.log.LoggerFactory.getLogger
import com.toocol.termio.utilities.log.LoggerFactory.init
import com.toocol.termio.utilities.module.ModuleDeployment
import com.toocol.termio.utilities.utils.CastUtil
import com.toocol.termio.utilities.utils.ClassScanner
import com.toocol.termio.utilities.utils.MessageBox
import io.vertx.core.*
import io.vertx.core.eventbus.EventBus
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer
import kotlin.system.exitProcess

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/1 10:13
 */
abstract class Termio {
    enum class RunType {
        CONSOLE, DESKTOP
    }

    companion object {
        protected const val blockedCheckInterval = 30 * 24 * 60 * 60 * 1000L
        @JvmStatic
        val logger = getLogger(Termio::class.java)
        @JvmStatic
        var initialLatch: CountDownLatch? = null
        @JvmStatic
        var loadingLatch: CountDownLatch? = null
        @JvmStatic
        var verticleClassList: MutableList<Class<out AbstractVerticle?>>? = ArrayList()
        @JvmStatic
        var runType: RunType? = null
        @JvmStatic
        var vertx: Vertx? = null
        @JvmStatic
        var eventBus: EventBus? = null

        @JvmField
        @Volatile
        var windowWidth: Int = 0
        @JvmField
        @Volatile
        var windowHeight: Int = 0

        @JvmStatic
        fun runType(): RunType {
            return runType!!
        }
        @JvmStatic
        fun vertx(): Vertx {
            return vertx!!
        }
        @JvmStatic
        fun eventBus(): EventBus {
            return eventBus!!
        }

        init {
            /* Get the verticle which need to deploy in main class by annotation */
            val annotatedClassList = ClassScanner("com.toocol.termio") { clazz: Class<*> ->
                clazz.isAnnotationPresent(ModuleDeployment::class.java) }.scan()

            annotatedClassList.forEach(Consumer { annotatedClass: Class<*> ->
                val superclass: Class<*> = annotatedClass.superclass
                if (superclass.superclass == AbstractVerticle::class.java) {
                    verticleClassList!!.add(CastUtil.cast(annotatedClass))
                } else {
                    logger.error("Skip deploy verticle ${annotatedClass.name} please extends AbstractVerticle")
                }
            })
            loadingLatch = CountDownLatch(1)
        }

        @JvmStatic
        protected fun componentInitialise() {
            if (runType == RunType.CONSOLE) {
                JNILoader.load()
            }
            TermCharEventDispatcher.init()
            ShellCharEventDispatcher.init()
            setPrinter(System.out)
        }

        @JvmStatic
        protected fun prepareVertxEnvironment(ignore: Set<Class<out AbstractVerticle>>?): Vertx {
            val initialLatchSize = if (ignore == null) verticleClassList!!.size else verticleClassList!!.size - ignore.size
            initialLatch = CountDownLatch(initialLatchSize)

            /* Because this program involves a large number of IO operations, increasing the blocking check time, we don't need it */
            val options = VertxOptions()
                .setBlockedThreadCheckInterval(blockedCheckInterval)
            val vertx = Vertx.vertx(options)
            init(vertx)

            /* Deploy the verticle */
            if (ignore != null && ignore.isNotEmpty()) {
                verticleClassList = ArrayList(verticleClassList!!
                    .stream()
                    .filter { clazz: Class<out AbstractVerticle?>? -> !ignore.contains(clazz) }
                    .toList())
            }
            verticleClassList!!.sortWith(Comparator.comparingInt { clazz: Class<out AbstractVerticle?> ->
                -1 * clazz.getAnnotation(ModuleDeployment::class.java).weight
            })
            verticleClassList!!.forEach(Consumer { verticleClass: Class<out AbstractVerticle?> ->
                val deploy = verticleClass.getAnnotation(ModuleDeployment::class.java)
                val deploymentOptions = DeploymentOptions()
                if (deploy.worker) {
                    deploymentOptions.setWorker(true).setWorkerPoolSize(deploy.workerPoolSize).workerPoolName =
                        deploy.workerPoolName
                }
                vertx.deployVerticle(verticleClass.name, deploymentOptions) { result: AsyncResult<String?> ->
                    if (result.succeeded()) {
                        initialLatch!!.countDown()
                    } else {
                        MessageBox.setExitMessage("Termio start up failed, verticle = " + verticleClass.simpleName)
                        print("Termio start up failed, verticle = " + verticleClass.simpleName)
                        vertx.close()
                        exitProcess(-1)
                    }
                }
            }
            )
            return vertx
        }
    }
}