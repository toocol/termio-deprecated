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

    opens com.toocol.termio.console.vert to termio.core;
    opens com.toocol.termio.console.handlers to termio.core;

    exports com.toocol.termio.console;
    exports com.toocol.termio.console.vert;
    exports com.toocol.termio.console.handlers;
}