package com.toocol.termio.utilities.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/7 15:34
 */
public class ClassScanner {

    private final String packageName;

    private final String packagePath;

    private final String packageDirName;

    private final String packageNameWithDot;

    private final Filter<Class<?>> classFilter;

    private final Set<Class<?>> classes = new HashSet<>();

    private final Charset charset = StandardCharsets.UTF_8;

    private ClassLoader classLoader;

    private boolean initialize;

    public ClassScanner(String packageName, Filter<Class<?>> filter) {
        this.packageName = packageName;
        this.packagePath = packageName.replace(CharUtil.DOT, CharUtil.SLASH);
        this.packageDirName = packageName.replace(CharUtil.DOT, File.separatorChar);
        this.packageNameWithDot = StrUtil.addSuffixIfNot(packageName, StrUtil.DOT);
        this.classFilter = filter;
    }

    public Set<Class<?>> scan() {
        for (URL url : ResourceUtil.getResourceIter(this.packagePath)) {
            switch (url.getProtocol()) {
                case "file" -> scanFile(new File(URLUtil.decode(url.getFile(), this.charset.name())), null);
                case "jar" -> scanJar(URLUtil.getJarFile(url));
                default -> {
                }
            }
        }

        if (this.classes.isEmpty()) {
            scanJavaClassPaths();
        }

        return Collections.unmodifiableSet(this.classes);
    }

    private void scanJavaClassPaths() {
        final String[] javaClassPaths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        for (String classPath : javaClassPaths) {
            classPath = URLUtil.decode(classPath, CharsetUtil.systemCharsetName());

            scanFile(new File(classPath), null);
        }
    }

    private void scanFile(File file, String rootDir) {
        if (file.isFile()) {
            final String fileName = file.getAbsolutePath();
            if (fileName.endsWith(FileUtil.CLASS_EXT)) {
                final String className = fileName
                        .substring(rootDir.length(), fileName.length() - 6)
                        .replace(File.separatorChar, CharUtil.DOT);
                addIfAccept(className);
            } else if (fileName.endsWith(FileUtil.JAR_FILE_EXT)) {
                try {
                    scanJar(new JarFile(file));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (null != files) {
                for (File subFile : files) {
                    scanFile(subFile, (null == rootDir) ? subPathBeforePackage(file) : rootDir);
                }
            }
        }
    }

    private void scanJar(JarFile jar) {
        String name;
        for (JarEntry entry : new EnumerationIter<>(jar.entries())) {
            name = StrUtil.removePrefix(entry.getName(), StrUtil.SLASH);
            if (name.startsWith(this.packagePath)) {
                if (name.endsWith(FileUtil.CLASS_EXT) && !entry.isDirectory()) {
                    final String className = name
                            .substring(0, name.length() - 6)
                            .replace(CharUtil.SLASH, CharUtil.DOT);
                    addIfAccept(loadClass(className));
                }
            }
        }
    }

    private void addIfAccept(String className) {
        if (StrUtil.isBlank(className)) {
            return;
        }
        int classLen = className.length();
        int packageLen = this.packageName.length();
        if (classLen == packageLen) {
            if (className.equals(this.packageName)) {
                addIfAccept(loadClass(className));
            }
        } else if (classLen > packageLen) {
            if (className.startsWith(this.packageNameWithDot)) {
                addIfAccept(loadClass(className));
            }
        }
    }

    private void addIfAccept(Class<?> clazz) {
        if (null != clazz) {
            Filter<Class<?>> classFilter = this.classFilter;
            if (classFilter == null || classFilter.accept(clazz)) {
                this.classes.add(clazz);
            }
        }
    }

    private Class<?> loadClass(String className) {
        ClassLoader loader = this.classLoader;
        if (null == loader) {
            loader = ClassLoaderUtil.getClassLoader();
            this.classLoader = loader;
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, this.initialize, loader);
        } catch (NoClassDefFoundError | UnsupportedClassVersionError e) {
            // skip
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return clazz;
    }

    private String subPathBeforePackage(File file) {
        String filePath = file.getAbsolutePath();
        if (StrUtil.isNotEmpty(this.packageDirName)) {
            filePath = StrUtil.subBefore(filePath, this.packageDirName, true);
        }
        return StrUtil.addSuffixIfNot(filePath, File.separator);
    }
}
