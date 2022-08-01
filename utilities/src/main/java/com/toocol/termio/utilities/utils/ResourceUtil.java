package com.toocol.termio.utilities.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/7 15:48
 */
public class ResourceUtil {
    public static EnumerationIter<URL> getResourceIter(String resource) {
        final Enumeration<URL> resources;
        try {
            resources = ClassLoaderUtil.getClassLoader().getResources(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new EnumerationIter<>(resources);
    }
}
