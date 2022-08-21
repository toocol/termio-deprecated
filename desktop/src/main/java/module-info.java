module termio.desktop {
    requires io.vertx.core;
    requires ini4j;
    requires jsr305;
    requires jdk.unsupported;
    requires com.google.protobuf;
    requires com.google.common;
    requires org.apache.commons.io;
    requires org.apache.commons.codec;
    requires org.apache.commons.lang3;
    requires kotlin.stdlib;
    requires annotations;

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires org.controlsfx.controls;
    requires reactfx;

    requires termio.utilities;
    requires termio.core;
    requires termio.platform;

    opens com.toocol.termio.desktop.bootstrap to termio.platform, javafx.fxml;
    opens com.toocol.termio.desktop.ui.panel to termio.platform, javafx.fxml;
    opens com.toocol.termio.desktop.ui.sidebar to termio.platform, javafx.fxml;
    opens com.toocol.termio.desktop.ui.terminal to termio.platform, javafx.fxml;
    opens com.toocol.termio.desktop.ui.homepage to javafx.fxml, termio.platform;
    opens com.toocol.termio.desktop.ui.executor to javafx.fxml, termio.platform;
    opens com.toocol.termio.desktop.api.term.module to termio.core;
    opens com.toocol.termio.desktop.api.term.handlers to termio.core;
    opens com.toocol.termio.desktop.api.ssh.module to termio.core;
    opens com.toocol.termio.desktop.api.ssh.handlers to termio.core;

    opens com.toocol.termio.desktop.configure to termio.utilities;

    exports com.toocol.termio.desktop.configure;
    exports com.toocol.termio.desktop.api.term.handlers;
    exports com.toocol.termio.desktop.api.term.module;
    exports com.toocol.termio.desktop.api.ssh.handlers;
    exports com.toocol.termio.desktop.api.ssh.module;
    exports com.toocol.termio.desktop.bootstrap;
}