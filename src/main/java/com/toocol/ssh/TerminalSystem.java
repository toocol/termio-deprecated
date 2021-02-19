package com.toocol.ssh;

import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.connector.vert.SshConnectorVerticle;
import com.toocol.ssh.core.credential.vert.SshCredentialReaderVerticle;
import com.toocol.ssh.core.credential.vert.SshCredentialWriterVerticle;
import io.vertx.core.Vertx;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 15:00
 */
public class TerminalSystem {
    public static void main(String[] args) {
        System.out.println("\n" +
                " _____ _____ _____    _____ _____ _____ _____ _____ _____ _____ __         _____ _____ _____ _____ _____ __    \n" +
                "|   __|   __|  |  |  |_   _|   __| __  |     |     |   | |  _  |  |       |_   _|     |     |     |     |  |   \n" +
                "|__   |__   |     |    | | |   __|    -| | | |-   -| | | |     |  |__    _  | | |  |  |  |  |   --|  |  |  |__ \n" +
                "|_____|_____|__|__|    |_| |_____|__|__|_|_|_|_____|_|___|__|__|_____|  |_| |_| |_____|_____|_____|_____|_____|\n" +
                "\n");
        PrintUtil.println("TerminalSystem registe the vertx service.");

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(SshConnectorVerticle.class.getName());
        vertx.deployVerticle(SshCredentialReaderVerticle.class.getName());
        vertx.deployVerticle(SshCredentialWriterVerticle.class.getName());
    }
}
