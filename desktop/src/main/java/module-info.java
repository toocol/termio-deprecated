module termio.desktop {
    requires io.vertx.core;
    requires ini4j;
    requires jsr305;
    requires jdk.unsupported;
    requires java.instrument;
    requires com.google.protobuf;
    requires com.google.common;
    requires org.apache.commons.io;
    requires org.apache.commons.codec;
    requires org.apache.commons.lang3;
    requires annotations;
    requires kotlinx.coroutines.core;
    requires kotlinx.coroutines.core.jvm;
    requires kotlinx.coroutines.javafx;
    requires kotlin.stdlib;

    requires javafx.controls;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires reactfx;
    requires org.jfxtras.styles.jmetro;

    requires termio.utilities;
    requires termio.core;
    requires termio.platform;

    opens com.toocol.termio.desktop.bootstrap to termio.platform;
    opens com.toocol.termio.desktop.components.panel.ui to termio.platform;
    opens com.toocol.termio.desktop.components.sidebar.ui to termio.platform;
    opens com.toocol.termio.desktop.components.terminal.ui to termio.platform;
    opens com.toocol.termio.desktop.components.homepage.ui to termio.platform;
    opens com.toocol.termio.desktop.components.executor.ui to termio.platform;
    opens com.toocol.termio.desktop.api.term.module to termio.core;
    opens com.toocol.termio.desktop.api.term.handlers to termio.core;
    opens com.toocol.termio.desktop.api.ssh.module to termio.core;
    opens com.toocol.termio.desktop.api.ssh.handlers to termio.core;
    opens com.toocol.termio.desktop.components.panel.listeners to termio.utilities;

    opens com.toocol.termio.desktop.components.terminal.config to termio.utilities;

    exports com.toocol.termio.desktop.api.term.handlers;
    exports com.toocol.termio.desktop.api.term.module;
    exports com.toocol.termio.desktop.api.ssh.handlers;
    exports com.toocol.termio.desktop.api.ssh.module;
    exports com.toocol.termio.desktop.bootstrap;
}