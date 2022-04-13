package com.toocol.ssh.core.term.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.utils.OsUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.misc.basicStructures.HWND;
import org.xvolks.jnative.util.User32;

import static com.toocol.ssh.core.term.TermAddress.CHECK_TYPEWRITING_STATUS;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/13 10:49
 */
public class CheckTypewritingStatusHandler extends AbstractMessageHandler<Void> {

    public static void main(String[] args) {
        try {
            System.setProperty("jnative.debug", "true");
            JNative GetActiveWindow = new JNative("User32.dll", "GetActiveWindow");
            GetActiveWindow.setRetVal(Type.INT);
            GetActiveWindow.invoke();
            int ret = GetActiveWindow.getRetValAsInt();
            GetActiveWindow.dispose();
            HWND hwnd = new HWND(ret);
            System.out.println(hwnd.createPointer().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CheckTypewritingStatusHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return CHECK_TYPEWRITING_STATUS;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Void> promise, Message<T> message) throws Exception {
        HWND hwnd = User32.GetActiveWindow();
        String imm32Lib = "Imm32" + OsUtil.libSuffix();
        while(true) {
            JNative immGetContext = new JNative(imm32Lib, "ImmGetContext");
            immGetContext.setParameter(0, hwnd.createPointer());
            immGetContext.setRetVal(Type.PSTRUCT);
        }
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<Void> asyncResult, Message<T> message) throws Exception {

    }
}
