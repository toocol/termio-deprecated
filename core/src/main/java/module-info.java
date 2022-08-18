module termio.core {
    requires termio.utilities;

    requires jdk.unsupported;
    requires com.google.protobuf;
    requires com.google.common;
    requires org.apache.commons.io;
    requires org.apache.commons.codec;
    requires org.apache.commons.lang3;
    requires java.desktop;
    requires io.vertx.core;
    requires jline;
    requires jsch;
    requires jsr305;
    requires ini4j;
    requires kotlin.stdlib;

    opens com.toocol.termio.core.config.core to termio.utilities;

    exports com.toocol.termio.core;
    exports com.toocol.termio.core.cache;
    exports com.toocol.termio.core.config.core;
    exports com.toocol.termio.core.config.module;
    exports com.toocol.termio.core.auth;
    exports com.toocol.termio.core.auth.core;
    exports com.toocol.termio.core.auth.handlers;
    exports com.toocol.termio.core.auth.module;
    exports com.toocol.termio.core.file;
    exports com.toocol.termio.core.file.core;
    exports com.toocol.termio.core.file.handlers;
    exports com.toocol.termio.core.file.module;
    exports com.toocol.termio.core.mosh;
    exports com.toocol.termio.core.mosh.core;
    exports com.toocol.termio.core.mosh.handlers;
    exports com.toocol.termio.core.mosh.module;
    exports com.toocol.termio.core.shell;
    exports com.toocol.termio.core.shell.core;
    exports com.toocol.termio.core.shell.handlers;
    exports com.toocol.termio.core.shell.module;
    exports com.toocol.termio.core.ssh;
    exports com.toocol.termio.core.ssh.core;
    exports com.toocol.termio.core.ssh.handlers;
    exports com.toocol.termio.core.ssh.module;
    exports com.toocol.termio.core.term;
    exports com.toocol.termio.core.term.commands;
    exports com.toocol.termio.core.term.commands.processors;
    exports com.toocol.termio.core.term.core;
}