package com.toocol.termio.platform.nativefx;

import com.toocol.termio.utilities.utils.OsUtil;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NativeBinding {

    private static boolean initialized;

    private NativeBinding() {
        throw new AssertionError("Don't instantiate me!");
    }

    private static String libEnding() {
        if (isOS("windows")) return ".dll";
        if (isOS("linux")) return ".so";
        if (isOS("macos")) return ".dylib";
        return ".so";
    }

    private static String archName() {
        String osArch = System.getProperty("os.arch");

        if (osArch.toLowerCase().contains("x64")
                || osArch.toLowerCase().contains("amd64")
                || osArch.toLowerCase().contains("x86_64")) {
            return "x64";
        } else if (osArch.toLowerCase().contains("x86")) {
            return "x86";
        } else if (osArch.toLowerCase().contains("aarch64")) {
            return "aarch64";
        }

        return osArch;
    }

    private static String osName() {
        String osName = System.getProperty("os.name");

        if (osName.toLowerCase().contains("windows")) {
            return "windows";
        } else if (osName.toLowerCase().contains("linux")) {
            return "linux";
        } else if (osName.toLowerCase().contains("mac")
                || osName.toLowerCase().contains("darwin")) {
            return "macos";
        }

        return osName;
    }

    private static boolean isOS(String os) {
        return os.equals(osName());
    }

    public static void init() {
        if (initialized) {
            return;
        }

        initialized = true;

        String name = "nativenode" + OsUtil.libSuffix();
        InputStream inputStream;
        OutputStream outputStream;

        try {
            inputStream = NativeBinding.class.getResourceAsStream("/" + name);
            assert inputStream != null;

            String libraryPath = System.getenv("JAVA_HOME") + OsUtil.fileSeparator() + "bin" + OsUtil.fileSeparator();
            File fileOut = new File(libraryPath + name);
            outputStream = new FileOutputStream(fileOut);
            IOUtils.copy(inputStream, outputStream);

            String extractPath = fileOut.toString();

            inputStream.close();
            outputStream.close();

            System.load(extractPath);
        } catch (Exception e) {
            System.out.println("Load nativenode.dll failed. message = " + e);
            System.exit(-1);
        }
    }

    interface IntEnum {
        int asInt();

        static int combine(IntEnum... enums) {
            int result = 0;

            for (IntEnum e : enums) {
                result = result | e.asInt();
            }

            return result;
        }

        static int combine(int combined, IntEnum... enums) {
            int result = combined;

            for (IntEnum e : enums) {
                result = result | e.asInt();
            }

            return result;
        }
    }

    interface MouseBtn {
        int NO_BTN = 0;
        int PRIMARY_BTN = 1;
        int SECONDARY_BTN = 2;
        int MIDDLE_BTN = 4;

        static int fromEvent(MouseEvent ev) {
            int result = MouseBtn.NO_BTN;

            if (ev.isPrimaryButtonDown()) {
                result |= MouseBtn.PRIMARY_BTN;
            }

            if (ev.isSecondaryButtonDown()) {
                result |= MouseBtn.SECONDARY_BTN;
            }

            if (ev.isMiddleButtonDown()) {
                result |= MouseBtn.MIDDLE_BTN;
            }

            return result;
        }

        static int fromEvent(ScrollEvent ev) {
            // TODO implement me 8.07.2019
            return MouseBtn.NO_BTN;
        }
    }

    interface Modifier {
        int NO_KEY = 0;
        int SHIFT_KEY = 1;
        int ALT_KEY = 2;
        int META_KEY = 4;
        int CONTROL_KEY = 8;

        static int fromEvent(MouseEvent ev) {
            int result = Modifier.NO_KEY;

            if (ev.isShiftDown()) {
                result |= Modifier.SHIFT_KEY;
            }

            if (ev.isAltDown()) {
                result |= Modifier.ALT_KEY;
            }

            if (ev.isMetaDown()) {
                result |= Modifier.META_KEY;
            }

            if (ev.isControlDown()) {
                result |= Modifier.CONTROL_KEY;
            }

            return result;
        }

        static int fromEvent(ScrollEvent ev) {
            int result = Modifier.NO_KEY;

            if (ev.isShiftDown()) {
                result |= Modifier.SHIFT_KEY;
            }

            if (ev.isAltDown()) {
                result |= Modifier.ALT_KEY;
            }

            if (ev.isMetaDown()) {
                result |= Modifier.META_KEY;
            }

            if (ev.isControlDown()) {
                result |= Modifier.CONTROL_KEY;
            }

            return result;
        }
    }

    private static final Map<Integer, List<NativeEventListener>> listeners = new HashMap<>();

    public static void addEventListener(int key, NativeEventListener l) {
        // create list if not present
        if (!listeners.containsKey(key)) {
            List<NativeEventListener> list = new ArrayList<>();
            listeners.put(key, list);
        }

        listeners.get(key).add(l);
    }

    public static void removeEventListener(int key, NativeEventListener l) {
        // create list if not present
        if (!listeners.containsKey(key)) {
            List<NativeEventListener> list = new ArrayList<>();
            listeners.put(key, list);
        }

        listeners.get(key).remove(l);
    }

    public static void removeEventListeners(int key) {
        // create list if not present
        if (!listeners.containsKey(key)) {
            List<NativeEventListener> list = new ArrayList<>();
            listeners.put(key, list);
        }

        listeners.get(key).clear();
    }

    /*CALLED FROM NATIVE*/
    public static void fireNativeEvent(int key, String type, String evt) {
        // return early if not present
        if (!listeners.containsKey(key)) {
            return;
        }

        List<NativeEventListener> list = listeners.get(key);

        for (NativeEventListener l : list) {
            l.event(key, type, evt);
        }
    }

    public static native int nextKey();

    public static native int connectTo(String name);

    public static native boolean terminate(int key);

    public static native boolean isConnected(int key);

    public static native String sendMsg(int key, String msg);

    public static native void processNativeEvents(int key);

    public static native void resize(int key, int w, int h);

    public static native boolean isDirty(int key);

    public static native void redraw(int key, int x, int y, int w, int h);

    public static native void setDirty(int key, boolean value);

    public static native void setBufferReady(int key, boolean value);

    public static native boolean isBufferReady(int key);

    public static native int getW(int key);

    public static native int getH(int key);

    public static native boolean fireMousePressedEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);

    public static native boolean fireMouseReleasedEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);

    public static native boolean fireMouseClickedEvent(int key, double x, double y, int buttons, int modifiers, int clickCount, long timestamp);

    public static native boolean fireMouseEnteredEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);

    public static native boolean fireMouseExitedEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);

    public static native boolean fireMouseMoveEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);

    public static native boolean fireMouseWheelEvent(int key, double x, double y, double amount, int buttons, int modifiers, long timestamp);

    public static native boolean fireKeyPressedEvent(int key, String characters, int keyCode, int modifiers, long timestamp);

    public static native boolean fireKeyReleasedEvent(int key, String characters, int keyCode, int modifiers, long timestamp);

    public static native boolean fireKeyTypedEvent(int key, String characters, int keyCode, int modifiers, long timestamp);

    public static native boolean requestFocus(int key, boolean focus, long timestamp);

    public static native ByteBuffer getBuffer(int key);

    public static native boolean lock(int key);

    public static native boolean lock(int key, long timeout);

    public static native void unlock(int key);

    public static native void waitForBufferChanges(int key);

    public static native boolean hasBufferChanges(int key);

    public static native void lockBuffer(int key);

    public static native void unlockBuffer(int key);
}