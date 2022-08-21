module termio.console {
    requires termio.utilities;
    requires termio.core;

    requires jdk.unsupported;
    requires com.google.common;
    requires org.apache.commons.io;
    requires org.apache.commons.codec;
    requires org.apache.commons.lang3;
    requires io.vertx.core;
    requires jsr305;
    requires kotlin.stdlib;
    requires annotations;
    requires jsch;

    opens com.toocol.termio.console.term.module to termio.core;
    opens com.toocol.termio.console.term.handlers to termio.core;
    opens com.toocol.termio.console.ssh.module to termio.core;
    opens com.toocol.termio.console.ssh.handlers to termio.core;

    exports com.toocol.termio.console.term.module;
    exports com.toocol.termio.console.term.handlers;
    exports com.toocol.termio.console.bootstrap;
}