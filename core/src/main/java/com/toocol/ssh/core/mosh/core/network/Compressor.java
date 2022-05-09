package com.toocol.ssh.core.mosh.core.network;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/9 23:15
 * @version: 0.0.1
 */
public class Compressor {

    private static Compressor compressor;

    public static synchronized Compressor get() {
        if (compressor == null) {
            compressor = new Compressor();
        }
        return compressor;
    }

    private Compressor() {

    }

    public String compressStr(String input) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] output;

        Deflater compressor = new Deflater();
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
        return new String(output);
    }

    public String uncompressStr(String input) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        byte[] output;
        byte[] data = input.getBytes(StandardCharsets.UTF_8);

        Inflater decompressor = new Inflater();
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
        return new String(output);
    }

}
