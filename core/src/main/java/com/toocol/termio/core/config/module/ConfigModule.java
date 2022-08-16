package com.toocol.termio.core.config.module;

import com.toocol.termio.utilities.config.IniConfigLoader;
import com.toocol.termio.utilities.module.AbstractModule;
import com.toocol.termio.utilities.module.ModuleDeployment;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/3 15:15
 */
@ModuleDeployment(weight = 100)
public class ConfigModule extends AbstractModule {

    @Override
    public void start() throws Exception {
        IniConfigLoader.loadConfig();
        stop();
    }

}
