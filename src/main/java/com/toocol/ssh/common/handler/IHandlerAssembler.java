package com.toocol.ssh.common.handler;

import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:02
 */
public interface IHandlerAssembler {

    /**
     * assemble the handler to the eventBus
     *
     * @param vertx the vertx system object
     * @param executor executor
     */
    default void assemble(Vertx vertx, WorkerExecutor executor) {
        Class<? extends IHandlerAssembler> clazz = this.getClass();
        RegisterHandler registerHandler = clazz.getAnnotation(RegisterHandler.class);
        if (registerHandler == null) {
            return;
        }

        Arrays.stream(registerHandler.handlers()).forEach(handlerClass -> {
            try {

                Constructor<? extends AbstractCommandHandler> declaredConstructor = handlerClass.getDeclaredConstructor(Vertx.class, WorkerExecutor.class);
                declaredConstructor.setAccessible(true);
                AbstractCommandHandler commandHandler = declaredConstructor.newInstance(vertx, executor);
                vertx.eventBus().consumer(commandHandler.address().address(), commandHandler::handle);
                PrintUtil.println(clazz.getSimpleName() + " assemble handler " + handlerClass.getSimpleName() + " success.");

            } catch (Exception e) {

                PrintUtil.printErr("Assemble handler failed, message = " + e.getMessage());
                System.exit(-1);

            }
        });
    }
}
