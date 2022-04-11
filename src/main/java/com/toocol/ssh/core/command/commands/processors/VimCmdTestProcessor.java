package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.cache.Cache;
import com.toocol.ssh.core.command.commands.OutsideCommandProcessor;
import com.toocol.ssh.core.configuration.SystemConfiguration;
import io.vertx.core.eventbus.EventBus;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.configuration.SystemConfiguration.BOOT_TYPE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/11 17:34
 */
public class VimCmdTestProcessor extends OutsideCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> tuple) {
        tuple.first(true);
        if (!SystemConfiguration.HAVE_INSTALLED_VIM) {
            return;
        }

        Cache.STOP_ACCEPT_OUT_COMMAND = true;
        new Thread(() -> {
            try {
                new ProcessBuilder(BOOT_TYPE, "/c", "vim vim_test.txt")
                        .inheritIO()
                        .start()
                        .waitFor();
                eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), 3);
            } catch (Exception e) {
                // do nothing
            }
        }).start();
    }
}
