package com.toocol.termio.core.mosh.core

import com.toocol.termio.core.auth.core.SshCredential
import com.toocol.termio.core.cache.MoshSessionCache
import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.core.ssh.core.SshSessionFactory
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.Promise
import io.vertx.core.Vertx
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 19:47
 */
class MoshSessionFactory private constructor(private val vertx: Vertx) : Loggable {
    private val moshSessionCache = MoshSessionCache
    private val sshSessionCache = SshSessionCache
    private val sshSessionFactory = SshSessionFactory

    /**
     * creating udp connection to mosh-server and starting data transport;
     */
    fun getSession(credential: SshCredential): MoshSession? {
        return try {
            var sessionId = sshSessionCache.containSession(credential.host)
            sessionId = if (sessionId != 0L) {
                sshSessionFactory.invokeSession(sessionId, credential)
            } else {
                sshSessionFactory.createSession(credential)
            }
            val portKey = sshTouch(sessionId) ?: return null
            val moshSession =
                MoshSession(vertx, sessionId, credential.host, credential.user, portKey._1(), portKey._2())
            moshSessionCache.put(moshSession)
            info("Create mosh session, key = {}, sessionId = {}, host = {}, user = {}",
                portKey._2(), sessionId, credential.host, credential.user)
            moshSession
        } catch (e: Exception) {
            null
        }
    }

    /**
     * touch mosh-server by ssh;
     *
     * @return mosh server port / key
     */
    private fun sshTouch(sessionId: Long): Tuple2<Int, String>? {
        val shell = sshSessionCache.getChannelShell(sessionId)
        val portKey = Tuple2<Int, String>()
        val latch = CountDownLatch(1)
        return try {
            val inputStream = shell!!.inputStream ?: return null
            val failed = AtomicBoolean(false)
            vertx.executeBlocking({ promise: Promise<Any?> ->
                val tmp = ByteArray(1024)
                while (true) {
                    try {
                        while (inputStream.available() > 0) {
                            val i = inputStream.read(tmp, 0, 1024)
                            if (i < 0) {
                                break
                            }
                            val inputStr = String(tmp, 0, i)
                            for (line in inputStr.split("\r\n").toTypedArray()) {
                                if (line.contains("MOSH CONNECT")) {
                                    val split = line.split(" ").toTypedArray()
                                    portKey.first(split[2].toInt()).second(split[3])
                                    latch.countDown()
                                    promise.tryComplete()
                                    return@executeBlocking
                                }
                            }
                        }
                    } catch (e: Exception) {
                        failed.set(true)
                        latch.countDown()
                        promise.tryComplete()
                    }
                }
            }, false)
            vertx.executeBlocking({ promise: Promise<Any?> ->
                try {
                    val outputStream = shell.outputStream
                    outputStream.write("export HISTCONTROL=ignoreboth\nmosh-server\n".toByteArray(StandardCharsets.UTF_8))
                    outputStream.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    promise.complete()
                }
            }, false)
            val suc = latch.await(20, TimeUnit.SECONDS)
            if (!suc || failed.get()) {
                null
            } else portKey
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private var FACTORY: MoshSessionFactory? = null
        @JvmStatic
        @Synchronized
        fun factory(vertx: Vertx): MoshSessionFactory? {
            if (FACTORY == null) {
                FACTORY = MoshSessionFactory(vertx)
            }
            return FACTORY
        }
    }
}