package com.toocol.termio.core.config.vert;

import com.toocol.termio.utilities.config.IniConfigLoader;
import com.toocol.termio.utilities.functional.VerticleDeployment;
import io.vertx.core.AbstractVerticle;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/3 15:15
 */
@VerticleDeployment(weight = 100)
public class ConfigVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        IniConfigLoader.loadConfig();
        stop();
    }

}
