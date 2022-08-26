package com.toocol.termio.core.shell.core

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.RemovalNotification
import com.jcraft.jsch.ChannelSftp
import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.utilities.ansi.Printer.printErr
import com.toocol.termio.utilities.utils.Castable
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import javax.annotation.Nonnull

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 17:26
 * @version: 0.0.1
 */
class SftpChannelProvider private constructor() {
    companion object Instance : Castable{
        private const val MAXIMUM_CACHE_SIZE = 30
        private const val EXPIRE_AFTER_ACCESS_AFTER_MINUTES = 10

        private val sshSessionCache = SshSessionCache
        private val cacheLoader: CacheLoader<Long, ChannelSftp> = object : CacheLoader<Long, ChannelSftp>() {
            @Nonnull
            @Throws(Exception::class)
            override fun load(sessionId: Long): ChannelSftp {
                val session = sshSessionCache.getSession(sessionId) ?: throw RuntimeException("Session is null, sessionId = null")
                if (!session.isConnected) {
                    throw RuntimeException("Session is not connected, sessionId = $sessionId")
                }
                val channelSftp = cast<ChannelSftp>(session.openChannel("sftp"))
                channelSftp.connect()
                return channelSftp
            }
        }
        private val channelSftpCache = CacheBuilder.newBuilder()
            .maximumSize(MAXIMUM_CACHE_SIZE.toLong())
            .expireAfterAccess(EXPIRE_AFTER_ACCESS_AFTER_MINUTES.toLong(), TimeUnit.MINUTES)
            .removalListener { (_, value): RemovalNotification<Long?, ChannelSftp?> ->
                Optional.ofNullable(
                    value).ifPresent { obj: ChannelSftp -> obj.disconnect() }
            }
            .build(cacheLoader)

        fun getChannelSftp(sessionId: Long): ChannelSftp? {
            try {
                return channelSftpCache[sessionId]
            } catch (e: ExecutionException) {
                printErr("Get channel exec failed, message = " + e.message)
            }
            return null
        }
    }
}