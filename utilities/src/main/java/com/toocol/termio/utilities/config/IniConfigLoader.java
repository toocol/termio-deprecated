package com.toocol.termio.utilities.config;

import com.google.common.collect.ImmutableMap;
import com.toocol.termio.utilities.log.Logger;
import com.toocol.termio.utilities.log.LoggerFactory;
import com.toocol.termio.utilities.utils.ClassScanner;
import com.toocol.termio.utilities.utils.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/3 15:07
 */
public class IniConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(IniConfigLoader.class);

    private static Map<String, Configure> sectionConfigureMap;
    private static String configFileRootPath;
    private static String[] configurePaths;

    public static void setConfigFileRootPath(String rootPath) {
        configFileRootPath = FileUtil.relativeToFixed(rootPath);
    }

    public static void setConfigurePaths(String[] configurePaths) {
        IniConfigLoader.configurePaths = configurePaths;
    }

    public static void loadConfig() {
        if (StringUtils.isEmpty(configFileRootPath)) {
            logger.warn("Config file root path is empty, skip config loading.");
            return;
        }
        if (configurePaths == null || configurePaths.length == 0) {
            logger.warn("Configure paths is empty, skip config loading.");
            return;
        }

        Set<Class<?>> configures = new HashSet<>();
        for (String configurePath : configurePaths) {
            configures.addAll(new ClassScanner(configurePath, clazz -> Optional.of(clazz.getSuperclass())
                    .map(superClass -> superClass.equals(Configure.class))
                    .orElse(false)).scan());
        }

        sectionConfigureMap = ImmutableMap.copyOf(configures.stream()
                .map(clazz -> {
                    try {
                        Configure configure = (Configure) clazz.getDeclaredConstructor().newInstance();
                        configure.initSubConfigure();
                        return configure;
                    } catch (Exception e) {
                        logger.error("Failed create Configure object, clazz = {}", clazz.getName());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Configure::section, configure -> configure)));

        logger.info("Initialize configures, size = {}", sectionConfigureMap.size());
        logger.info("Reading ini config file, path = {}", configFileRootPath);

        read().forEach(ini -> {
            for (Profile.Section section : ini.values()) {
                String key = section.getName().contains(".") ? section.getName().split("\\.")[0] : section.getName();
                Optional.ofNullable(sectionConfigureMap.get(key))
                        .ifPresent(configure -> {
                            configure.assignAssembleJob(section.getName(), section);
                            logger.info("Assemble ini config section, section = {}", section.getName());
                        });
            }
        });
    }

    protected static Collection<Ini> read() {
        return FileUtils.listFiles(new File(configFileRootPath), new String[]{"ini"}, true)
                .stream()
                .map(file -> {
                    try {
                        return new Ini(file);
                    } catch (IOException e) {
                        logger.error("Read ini file failed, fileName = ", file.getName());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

}
