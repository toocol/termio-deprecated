package com.toocol.termio.utilities.module;

import com.toocol.termio.utilities.functional.Ordered;
import com.toocol.termio.utilities.utils.Castable;
import com.toocol.termio.utilities.utils.MessageBox;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:02
 */
public interface IHandlerMounter extends Castable {
    /**
     * assemble the handler to the eventBus
     *
     * @param vertx    the vertx system object
     * @param context  the verticle's executor
     * @param parallel whether the handlers is handle parallel
     * @param injects  objs to inject
     * @param <T>      generic type
     */
    @SuppressWarnings("all")
    default <T> void mountHandler(Vertx vertx, Context context) {
        Class<? extends IHandlerMounter> clazz = this.getClass();
        RegisterHandler registerHandler = clazz.getAnnotation(RegisterHandler.class);
        if (registerHandler == null) {
            return;
        }

        Arrays.stream(registerHandler.handlers()).forEach(handlerClass -> {
            try {

                if (handlerClass.getSuperclass().equals(NonBlockingMessageHandler.class)) {

                    Constructor<? extends NonBlockingMessageHandler> declaredConstructor = cast(handlerClass.getDeclaredConstructor(Vertx.class, Context.class));
                    declaredConstructor.setAccessible(true);
                    AbstractMessageHandler commandHandler = declaredConstructor.newInstance(vertx, context);
                    vertx.eventBus().consumer(commandHandler.consume().address(), commandHandler::handle);

                } else if (handlerClass.getSuperclass().equals(BlockingMessageHandler.class)) {

                    Constructor<? extends BlockingMessageHandler<?>> declaredConstructor = cast(handlerClass.getDeclaredConstructor(Vertx.class, Context.class, boolean.class));
                    declaredConstructor.setAccessible(true);
                    boolean parallel = handlerClass.getAnnotation(Ordered.class) == null;
                    BlockingMessageHandler<?> commandHandler = declaredConstructor.newInstance(vertx, context, parallel);
                    vertx.eventBus().consumer(commandHandler.consume().address(), commandHandler::handle);

                }

            } catch (Exception e) {
                MessageBox.setExitMessage("Assemble handler failed, message = " + e.getMessage());
                System.exit(-1);
            }
        });
    }
}
