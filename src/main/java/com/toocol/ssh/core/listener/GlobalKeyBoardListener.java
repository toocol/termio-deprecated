package com.toocol.ssh.core.listener;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/3 0:04
 * @version: 0.0.1
 */
public class GlobalKeyBoardListener implements NativeKeyListener {

    private volatile static OutputStream outputStream;

    private static final Lock LOCK = new ReentrantLock();

    public static void setOutputStream(OutputStream outputStream) {
        LOCK.lock();
        GlobalKeyBoardListener.outputStream = outputStream;
        LOCK.unlock();
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
        System.out.println(nativeKeyEvent.getKeyLocation());
        if (nativeKeyEvent.getRawCode() == 9) {
            // handle the tab
            LOCK.lock();
            try {
                System.out.println("tabed");
                if (outputStream == null) {
                    return;
                }
                outputStream.write("\t".getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                LOCK.unlock();
            }

        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

    }
}
