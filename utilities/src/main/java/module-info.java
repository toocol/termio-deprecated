module com.toocol.ssh.utilities {
    requires io.vertx.core;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires com.google.common;
    requires jsr305;

    exports com.toocol.ssh.utilities.action;
    exports com.toocol.ssh.utilities.address;
    exports com.toocol.ssh.utilities.anis;
    exports com.toocol.ssh.utilities.command;
    exports com.toocol.ssh.utilities.console;
    exports com.toocol.ssh.utilities.event;
    exports com.toocol.ssh.utilities.execeptions;
    exports com.toocol.ssh.utilities.functional;
    exports com.toocol.ssh.utilities.handler;
    exports com.toocol.ssh.utilities.jni;
    exports com.toocol.ssh.utilities.log;
    exports com.toocol.ssh.utilities.obj;
    exports com.toocol.ssh.utilities.sync;
    exports com.toocol.ssh.utilities.utils;
}