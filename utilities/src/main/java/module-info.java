module termio.utilities {
    requires io.vertx.core;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.apache.commons.codec;
    requires com.google.common;
    requires jsr305;
    requires ini4j;
    requires jline;
    requires kotlin.stdlib;
    requires annotations;

    exports com.toocol.termio.utilities.action;
    exports com.toocol.termio.utilities.ansi;
    exports com.toocol.termio.utilities.bundle;
    exports com.toocol.termio.utilities.command;
    exports com.toocol.termio.utilities.config;
    exports com.toocol.termio.utilities.console;
    exports com.toocol.termio.utilities.event;
    exports com.toocol.termio.utilities.execeptions;
    exports com.toocol.termio.utilities.functional;
    exports com.toocol.termio.utilities.module;
    exports com.toocol.termio.utilities.io;
    exports com.toocol.termio.utilities.jni;
    exports com.toocol.termio.utilities.log;
    exports com.toocol.termio.utilities.obj;
    exports com.toocol.termio.utilities.sync;
    exports com.toocol.termio.utilities.utils;
    exports com.toocol.termio.utilities.escape;
}