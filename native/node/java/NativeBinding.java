package com.toocol.termio.platform.nativefx;

public final class NativeBinding {
    public NativeBinding(){};

    public static final native int nextKey();
    public static final native int connectTo(java.lang.String name);
    public static final native boolean terminate(int key);
    public static final native boolean isConnected(int key);
    public static final native java.lang.String sendMsg(int key, java.lang.String msg);
    public static final native void processNativeEvents(int key);
    public static final native void resize(int key, int w, int h);
    public static final native boolean isDirty(int key);
    public static final native void redraw(int key, int x, int y, int w, int h);
    public static final native void setDirty(int key, boolean value);
    public static final native void setBufferReady(int key, boolean value);
    public static final native boolean isBufferReady(int key);
    public static final native int getW(int key);
    public static final native int getH(int key);
    public static final native boolean fireMousePressedEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);
    public static final native boolean fireMouseReleasedEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);
    public static final native boolean fireMouseClickedEvent(int key, double x, double y, int buttons, int modifiers, int clickCount, long timestamp);
    public static final native boolean fireMouseEnteredEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);
    public static final native boolean fireMouseExitedEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);
    public static final native boolean fireMouseMoveEvent(int key, double x, double y, int buttons, int modifiers, long timestamp);
    public static final native boolean fireMouseWheelEvent(int key, double x, double y, double amount, int buttons, int modifiers, long timestamp);
    public static final native boolean fireKeyPressedEvent(int key, java.lang.String characters, int keyCode, int modifiers, long timestamp);
    public static final native boolean fireKeyReleasedEvent(int key, java.lang.String characters, int keyCode, int modifiers, long timestamp);
    public static final native boolean fireKeyTypedEvent(int key, java.lang.String characters, int keyCode, int modifiers, long timestamp);
    public static final native java.nio.ByteBuffer getBuffer(int key);
    public static final native boolean lock(int key);
    public static final native boolean lock(int key, long timeout);
    public static final native void unlock(int key);
    public static final native void waitForBufferChanges(int key);
    public static final native boolean hasBufferChanges(int key);
    public static final native void lockBuffer(int key);
    public static final native void unlockBuffer(int key);
}
