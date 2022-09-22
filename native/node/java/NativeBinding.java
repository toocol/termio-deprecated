package com.toocol.termio.platform.nativefx;

import java.nio.ByteBuffer;

public final class NativeBinding {
    public NativeBinding(){};

    public static native int nextKey();

    public static native int connectTo(String name);

    public static native boolean terminate(int key);

    public static native boolean isConnected(int key);

    public static native String sendMsg(int key, String msg, int sharedStringType);

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
