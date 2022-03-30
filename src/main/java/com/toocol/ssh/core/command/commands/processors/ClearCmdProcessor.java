package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.command.commands.AbstractCommandProcessor;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 19:24
 */
public class ClearCmdProcessor extends AbstractCommandProcessor {
    @Override
    public <T> void process(T param) throws Exception {
        PrintUtil.clear();
        PrintUtil.printCursorLine();
    }
}
