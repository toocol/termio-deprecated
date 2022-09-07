package com.toocol.termio.core.shell.commands.processors

import com.toocol.termio.core.shell.ShellAddress
import com.toocol.termio.core.shell.commands.ShellCommandProcessor
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.core.shell.handlers.BlockingDfHandler
import com.toocol.termio.utilities.utils.FilePathUtil
import com.toocol.termio.utilities.utils.StrUtil
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import org.apache.commons.lang3.StringUtils
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:34
 * @version: 0.0.1
 */
class ShellDfCmdProcessor : ShellCommandProcessor() {
    override fun process(
        eventBus: EventBus,
        shell: Shell,
        isBreak: AtomicBoolean,
        cmd: String
    ): Tuple2<String?, Long?> {
        val split = cmd.trim { it <= ' ' }.replace(" {2,}".toRegex(), StrUtil.SPACE).split(StrUtil.SPACE).toTypedArray()
        if (split.size < 2) {
            return Tuple2(StrUtil.EMPTY, null)
        }
        val remotePath = StringBuilder()
        for (pathIndex in 1 until split.size) {
            val inputPath = split[pathIndex]
            val user = shell.user
            val currentPath = shell.fullPath.get()
            if (inputPath.startsWith(FilePathUtil.CURRENT_FOLDER_PREFIX)) {
                if (FilePathUtil.USER_FOLDER == currentPath) {
                    remotePath.append(FilePathUtil.ROOT_FOLDER_PREFIX).append(user).append(inputPath.substring(1))
                } else {
                    remotePath.append(currentPath).append(inputPath.substring(1))
                }
            } else if (inputPath.startsWith(FilePathUtil.PARENT_FOLDER_PREFIX)) {
                val singleCurrentPaths = currentPath.split("/").toTypedArray()
                if (singleCurrentPaths.size <= 1) {
                    remotePath.append(FilePathUtil.ROOT_FOLDER_PREFIX).append(inputPath.substring(1))
                } else {
                    for (idx in 0..singleCurrentPaths.size - 2) {
                        if (StringUtils.isEmpty(singleCurrentPaths[idx])) {
                            continue
                        }
                        remotePath.append("/").append(singleCurrentPaths[idx])
                    }
                    remotePath.append(inputPath.substring(2))
                }
            } else {
                remotePath.append(currentPath).append("/").append(inputPath)
            }
            remotePath.append(",")
        }
        remotePath.deleteCharAt(remotePath.length - 1)
        val request = JsonObject()
        request.put("sessionId", shell.sessionId)
        request.put("remotePath", remotePath.toString())
        request.put("type", BlockingDfHandler.DF_TYPE_FILE)
        eventBus.send(ShellAddress.START_DF_COMMAND.address(), request)
        return Tuple2(null, null)
    }
}