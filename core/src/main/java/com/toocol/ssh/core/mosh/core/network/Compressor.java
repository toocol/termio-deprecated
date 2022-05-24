package com.toocol.ssh.core.mosh.core.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/9 23:15
 * @version: 0.0.1
 */
public final class Compressor {

    private static Compressor compressor;

    /**
     * There are some problems:
     * 1. When we send packet to mosh-server, we have to set the nowrap to true;
     * 2. But we should set the nowrap to false when we receive packet from mosh-server.
     */
    private static boolean testMode = false;

    static synchronized Compressor get() {
        if (compressor == null) {
            compressor = new Compressor();
        }
        return compressor;
    }

    private Compressor() {

    }

    public byte[] compress(byte[] bytes, boolean nowrap) {
        if (bytes == null || bytes.length == 0) {
            return new byte[0];
        }
        byte[] output;

        Deflater compressor = new Deflater(Deflater.DEFAULT_COMPRESSION, testMode || nowrap);
        compressor.reset();
        compressor.setInput(bytes);
        compressor.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);

        try {
            byte[] buf = new byte[1024];
            while (!compressor.finished()) {
                int i = compressor.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            output = bytes;
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        compressor.end();
        return output;
    }

    public byte[] decompress(byte[] data, boolean nowrap) {
        byte[] output;

        Inflater decompressor = new Inflater(testMode || nowrap);
        decompressor.reset();
        decompressor.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompressor.finished()) {
                int i = decompressor.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompressor.end();
        return output;
    }

    public static void testMode() {
        testMode = true;
    }

}
