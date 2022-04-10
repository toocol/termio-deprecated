package com.toocol.ssh.core.file.handlers;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.utils.FileUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;
import javafx.application.Platform;
import javafx.stage.FileChooser;

import java.io.File;

import static com.toocol.ssh.core.file.FileVerticleAddress.CHOOSE_FILE;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 19:34
 * @version: 0.0.1
 */
public class ChooseFileHandler extends AbstractMessageHandler<String> {

    public ChooseFileHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress consume() {
        return CHOOSE_FILE;
    }

    @Override
    protected <T> void handleWithin(Promise<String> promise, Message<T> message) throws Exception {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);
            promise.complete(file == null ? null : file.getAbsolutePath());
        });
    }

    @Override
    protected <T> void resultWithin(AsyncResult<String> asyncResult, Message<T> message) throws Exception {
        message.reply(asyncResult.result());
    }
}
