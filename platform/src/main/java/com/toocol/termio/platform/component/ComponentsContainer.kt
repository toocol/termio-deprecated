package com.toocol.termio.platform.component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 11:54
 */
public final class ComponentsContainer {
    private static final Map<String, IComponent> COMPONENTS_MAP = new ConcurrentHashMap<>();

    public static void put(Class<?> clazz, long id, IComponent component) {
        COMPONENTS_MAP.put(generateFullId(clazz, id), component);
    }

    public static <T extends IComponent> T get(Class<?> clazz, long id) {
        return COMPONENTS_MAP.get(generateFullId(clazz, id)).as();
    }

    private static String generateFullId(Class<?> clazz, long id) {
        return clazz.getName() + "." + id;
    }
}
