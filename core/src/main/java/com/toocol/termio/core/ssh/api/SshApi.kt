package com.toocol.termio.core.ssh.api

import com.jcraft.jsch.ChannelShell
import com.toocol.termio.core.Termio
import com.toocol.termio.core.cache.*
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.core.shell.core.ShellProtocol
import com.toocol.termio.core.ssh.core.SshSessionFactory
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.core.term.core.TermStatus
import com.toocol.termio.utilities.ansi.AnsiStringBuilder
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.module.SuspendApi
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/4 15:34
 * @version: 0.0.1
 */
object SshApi : SuspendApi, Loggable {
    private val credentialCache = CredentialCache.Instance
    private val sshSessionCache = SshSessionCache.Instance
    private val shellCache = ShellCache.Instance
    private val factory = SshSessionFactory.Instance

    suspend fun establishSshSession(index: Int): Long = withContext(Dispatchers.IO) {
        run {
            val credential = credentialCache.getCredential(index)
            credential ?: return@run 0L
            var sessionId = sshSessionCache.containSession(credential.host)
            try {

                if (sessionId == 0L) {
                    HANGED_ENTER = false
                    sessionId = factory.createSession(credential)
                    val shell = Shell(sessionId,
                        credential.host,
                        credential.user,
                        sshSessionCache.getChannelShell(sessionId))
                    shell.user = credential.user
                    shellCache.putShell(sessionId, shell)
                    shell.setJumpServer(credential.isJumpServer)
                    shell.initialFirstCorrespondence(ShellProtocol.SSH)
                } else {
                    HANGED_ENTER = true
                    val newSessionId = factory.invokeSession(sessionId, credential)
                    if (newSessionId != sessionId || !shellCache.contains(newSessionId)) {
                        val shell = Shell(sessionId,
                            credential.host,
                            credential.user,
                            sshSessionCache.getChannelShell(sessionId))
                        shell.user = credential.user
                        shellCache.putShell(sessionId, shell)
                        sessionId = newSessionId
                        shell.setJumpServer(credential.isJumpServer)
                        shell.initialFirstCorrespondence(ShellProtocol.SSH)
                    } else {
                        val shell = shellCache.getShell(newSessionId) ?: return@run sessionId
                        if (shell.channelShell == null) {
                            /* If the connection is established through Mosh, it needs to be set the ChannelShell*/
                            shell.channelShell = sshSessionCache.getChannelShell(sessionId)
                        }
                        shell.resetIO(ShellProtocol.SSH)
                        sessionId = newSessionId
                    }
                }

                Optional.ofNullable(sshSessionCache.getChannelShell(sessionId)).ifPresent { channelShell: ChannelShell ->
                    val width = Termio.windowWidth
                    val height = Termio.windowHeight
                    channelShell.setPtySize(width, height, width, height)

                }
                HANGED_QUIT = false
                shellCache.initializeQuickSessionSwitchHelper()
                if (sessionId > 0) {
                    val shell = shellCache.getShell(sessionId)
                    if (shell == null) {
                        warn("Get Shell is null when try to entry shell.")
                        return@run 0
                    }
                    shell.printAfterEstablish()
                    SHOW_WELCOME = true
                    MONITOR_SESSION_ID = sessionId
                    Term.status = TermStatus.SHELL
                } else {
                    warn("Establish ssh connection failed.")
                }
            } catch (e: Exception) {
                // Do nothing
            } finally {
                // invoke gc() to clean up already un-use object during initial processing. (it's very efficacious :))
                System.gc()
            }
            return@run sessionId
        }
    }

    suspend fun activeSshSession(index: List<Int>) = withContext(Dispatchers.IO) {
        run {
            val ret = JsonObject()
            val success = JsonArray()
            val failed = JsonArray()
            val rec = AtomicInteger()
            for (i in index.indices) {
                val credential = credentialCache.getCredential(index[i])
                credential ?: continue
                try {
                    val sessionId = AtomicReference(sshSessionCache.containSession(
                        credential.host))
                    if (sessionId.get() == 0L) {
                        sessionId.set(factory.createSession(credential))
                        val shell = Shell(sessionId.get(),
                            credential.host,
                            credential.user,
                            sshSessionCache.getChannelShell(sessionId.get()))
                        shell.user = credential.user
                        shellCache.putShell(sessionId.get(), shell)
                        shell.initialFirstCorrespondence(ShellProtocol.SSH)
                    } else {
                        val newSessionId = factory.invokeSession(sessionId.get(), credential)
                        if (newSessionId != sessionId.get() || !shellCache.contains(newSessionId)) {
                            val shell = Shell(sessionId.get(),
                                credential.host,
                                credential.user,
                                sshSessionCache.getChannelShell(sessionId.get()))
                            shell.user = credential.user
                            shellCache.putShell(sessionId.get(), shell)
                            sessionId.set(newSessionId)
                            shell.initialFirstCorrespondence(ShellProtocol.SSH)
                        } else {
                            val shell = shellCache.getShell(sessionId.get()) ?: throw RuntimeException()
                            shell.resetIO(ShellProtocol.SSH)
                            sessionId.set(newSessionId)
                        }
                    }

                    Optional.ofNullable(sshSessionCache.getChannelShell(sessionId.get()))
                        .ifPresent { channelShell: ChannelShell ->
                            val width = Termio.windowWidth
                            val height = Termio.windowHeight
                            channelShell.setPtySize(width, height, width, height)
                        }
                    System.gc()
                    if (sessionId.get() > 0) {
                        success.add(credential.host + "@" + credential.user)
                    } else {
                        failed.add(credential.host + "@" + credential.user)
                    }
                    if (rec.incrementAndGet() == index.size) {
                        shellCache.initializeQuickSessionSwitchHelper()
                        ret.put("success", success)
                        ret.put("failed", failed)
                    }
                } catch (e: Exception) {
                    failed.add(credential.host + "@" + credential.user)
                    if (rec.incrementAndGet() == index.size) {
                        ret.put("success", success)
                        ret.put("failed", failed)
                    }
                }
            }

            val term = Term.instance
            term.printScene(false)
            val ansiStringBuilder = AnsiStringBuilder()
            val width = Termio.windowWidth
            for ((key, value1) in ret) {
                if ("success" == key) {
                    ansiStringBuilder.append("$key:")
                    val value = value1.toString()
                    val split = value.replace("[", "").replace("]", "").replace("\"", "").split(",").toTypedArray()
                    for (i in split.indices) {
                        if (width < 24 * 3) {
                            if (i != 0 && i % 2 == 0) {
                                ansiStringBuilder.append("\n")
                            }
                        } else {
                            if (i != 0 && i % 3 == 0) {
                                ansiStringBuilder.append("\n")
                            }
                        }
                        ansiStringBuilder.front(Term.theme.activeSuccessMsgColor.color)
                            .background(Term.theme.displayBackGroundColor.color)
                            .append(split[i] + StringUtils.repeat(" ", 4))
                    }
                } else {
                    ansiStringBuilder.deFront().append("$key:")
                    val value = value1.toString()
                    val split = value.replace("[", "").replace("]", "").replace("\"", "").split(",").toTypedArray()
                    for (j in split.indices) {
                        if (width < 24 * 3) {
                            if (j != 0 && j % 2 == 0) {
                                ansiStringBuilder.append("\n")
                            }
                        } else {
                            if (j != 0 && j % 3 == 0) {
                                ansiStringBuilder.append("\n")
                            }
                        }
                        ansiStringBuilder.front(Term.theme.activeFailedMsgColor.color)
                            .background(Term.theme.displayBackGroundColor.color)
                            .append(split[j] + StringUtils.repeat(" ", 4))
                    }
                }
            }
            term.printDisplay(ansiStringBuilder.toString())
        }
    }
}