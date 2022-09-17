package com.toocol.termio.platform.nativefx;

import java.nio.ByteBuffer;

public final class NativeBinding {
    public NativeBinding(){};

    static native int nextKey();

    static native int connectTo(String name);

    static native boolean terminate(int key);

    static native boolean isConnected(int key);

    static native String sendMsg(int key, String msg);

    static native void processNativeEvents(int key);

    static native void resize(int key, int w, int h);

    static native boolean isDirty(int key);

    static native void redraw(int key, int x, int y, int w, int h);

    static native void setDirty(int key, boolean value);

    static native void setBufferReady(int key, boolean value);

    static native boolean isBufferReady(int key);

    static native int getW(int key);

    static native int getH(int key);

    static native boolean fireMousePressedEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);

    static native boolean fireMouseReleasedEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);

    static native boolean fireMouseClickedEvent(int key, double x, double y, int buttons, int modifiers, int clickCount, long timestamp);

    static native boolean fireMouseEnteredEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);

    static native boolean fireMouseExitedEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);

    static native boolean fireMouseMoveEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);

    static native boolean fireMouseWheelEvent(int key, double x, double y, double amount, int buttons, int modifiers, long timestamp);

    static native boolean fireKeyPressedEvent(int key, String characters, int keyCode, int modifiers, long timestamp);

    static native boolean fireKeyReleasedEvent(int key, String characters, int keyCode, int modifiers, long timestamp);

    static native boolean fireKeyTypedEvent(int key, String characters, int keyCode, int modifiers, long timestamp);

    static native ByteBuffer getBuffer(int key);

    static native boolean lock(int key);

    static native boolean lock(int key, long timeout);

    static native void unlock(int key);

    static native void waitForBufferChanges(int key);

    static native boolean hasBufferChanges(int key);

    static native void lockBuffer(int key);

    static native void unlockBuffer(int key);
}
