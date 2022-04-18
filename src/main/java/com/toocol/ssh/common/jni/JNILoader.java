package com.toocol.ssh.common.jni;

import com.toocol.ssh.common.utils.OsUtil;
import com.toocol.ssh.core.term.core.Printer;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/14 23:37
 * @version: 0.0.1
 */
public class JNILoader {

    public static void load() {
        loadLib("libtermio" + OsUtil.libSuffix());
    }

    private static void loadLib(String name) {
        InputStream inputStream;
        OutputStream outputStream;

        try {
            inputStream = JNILoader.class.getResourceAsStream("/" + name);
            assert inputStream != null;

            String libraryPath = System.getenv("JAVA_HOME") + OsUtil.fileSeparator() + "bin" + OsUtil.fileSeparator();
            File fileOut = new File(libraryPath + name);
            outputStream = new FileOutputStream(fileOut);
            IOUtils.copy(inputStream, outputStream);

            String extractPath = fileOut.toString();

            inputStream.close();
            outputStream.close();

            System.load(extractPath);//loading goes here
        } catch (Exception e) {
            Printer.println("Load library failed. message = " + e);
            System.exit(-1);
        }
    }
}
