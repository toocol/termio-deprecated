package com.toocol.termio.utilities.config;

import com.toocol.termio.utilities.log.Loggable;
import com.toocol.termio.utilities.utils.Castable;
import com.toocol.termio.utilities.utils.ClassScanner;
import org.ini4j.Profile;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/3 15:09
 */
public abstract class Configure implements Loggable, Castable {

    private final Map<String, SubConfigure> subConfigureMap = new HashMap<>();

    /**
     * the section's names that configure class should process.
     */
    public abstract String section();

    /**
     * assemble the ini config.
     */
    public abstract void assemble(Profile.Section section);

    public void assignAssembleJob(String name, Profile.Section section) {
        if (name.contains(".")) {
            Optional.ofNullable(subConfigureMap.get(name)).ifPresent(subConfigure -> subConfigure.assemble(section));
        } else {
            assemble(section);
        }
    }

    public void initSubConfigure() {
        String packageName = this.getClass().getPackageName();
        new ClassScanner(packageName, clazz -> Optional.ofNullable(clazz.getSuperclass())
                .map(superClazz -> superClazz.equals(SubConfigure.class))
                .orElse(false)).scan().forEach(subClazz -> {
                    try {
                        Constructor<?> constructor = subClazz.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        SubConfigure subConfigure = cast(constructor.newInstance());
                        subConfigureMap.put(subConfigure.section(), subConfigure);
                        info("Initialize sub configure success, class = {}", subClazz.getName());
                    } catch (Exception e) {
                        error("Initialize sub configure failed, class = {}", subClazz.getName());
                    }
        });
    }
}
