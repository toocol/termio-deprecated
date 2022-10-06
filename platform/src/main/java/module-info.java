module termio.platform {
    requires jsr305;
    requires termio.utilities;
    requires termio.core;
    requires javafx.base;
    requires javafx.controls;
    requires io.vertx.core;
    requires org.fxmisc.richtext;
    requires kotlin.stdlib;
    requires kotlinx.coroutines.core.jvm;
    requires com.google.common;
    requires reactfx;
    requires annotations;
    requires org.jfxtras.styles.jmetro;
    requires org.apache.commons.io;

    opens com.toocol.termio.platform.nativefx.listeners to termio.utilities;

    exports com.toocol.termio.platform.console;
    exports com.toocol.termio.platform.component;
    exports com.toocol.termio.platform.ui;
    exports com.toocol.termio.platform.text;
    exports com.toocol.termio.platform.font;
    exports com.toocol.termio.platform.css;
    exports com.toocol.termio.platform.nativefx;
    exports com.toocol.termio.platform.nativefx.listeners;
    exports com.toocol.termio.platform.window;
}