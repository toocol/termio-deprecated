package com.toocol.termio.core.cache

import com.toocol.termio.core.auth.core.SshCredential
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.utilities.ansi.AnsiStringBuilder
import com.toocol.termio.utilities.ansi.Printer
import com.toocol.termio.utilities.functional.Switchable
import com.toocol.termio.utilities.utils.MessageBox
import com.toocol.termio.utilities.utils.StrUtil
import io.vertx.core.json.JsonArray
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.function.Consumer
import kotlin.system.exitProcess

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/2 15:46
 */
class CredentialCache private constructor() {
    companion object Instance {
        private val CREDENTIAL_SET: MutableSet<SshCredential?> = TreeSet(Comparator.comparingInt { credential: SshCredential? -> -1 * credential!!.host.hashCode() })
        private val READ_WRITE_LOCK: ReadWriteLock = ReentrantReadWriteLock()
        fun credentialsSize(): Int {
            val lock = READ_WRITE_LOCK.readLock()
            lock.lock()
            try {
                return CREDENTIAL_SET.size
            } catch (e: Exception) {
                MessageBox.setExitMessage("Credential operation error.")
                exitProcess(-1)
            } finally {
                lock.unlock()
            }
        }

        val credentialsJson: String?
            get() {
                val lock = READ_WRITE_LOCK.readLock()
                lock.lock()
                try {
                    return JsonArray(ArrayList(CREDENTIAL_SET)).toString()
                } catch (e: Exception) {
                    MessageBox.setExitMessage("Credential operation error.")
                    System.exit(-1)
                } finally {
                    lock.unlock()
                }
                return null
            }

        fun showCredentials() {
            val theme = Term.theme
            val lock = READ_WRITE_LOCK.readLock()
            lock.lock()
            try {
                val idx = AtomicInteger(1)
                val term = Term.getInstance()
                CREDENTIAL_SET.forEach(Consumer { credential: SshCredential? ->
                    val index = idx.getAndIncrement()
                    Printer.print(AnsiStringBuilder()
                        .background(theme.propertiesZoneBgColor.color)
                        .append(StrUtil.SPACE.repeat(term.width))
                        .toString()
                    )
                    term.setCursorPosition(0, term.cursorPosition[1])
                    val builder = AnsiStringBuilder()
                        .background(theme.propertiesZoneBgColor.color)
                    if (SshSessionCache.isAlive(credential!!.host)) {
                        builder.front(theme.sessionAliveColor.color)
                    } else {
                        builder.front(theme.indexFrontColor.color)
                    }
                    builder.append("[" + (if (index < 10) "0$index" else index) + "]\t\t").deFront()
                        .front(theme.userHighlightColor.color).append(credential.user).deFront()
                        .front(theme.atHighlightColor.color).append("@").deFront()
                        .front(theme.hostHighlightColor.color).append(credential.host).deFront()
                    Printer.println(builder.toString())
                })
            } catch (e: Exception) {
                MessageBox.setExitMessage("Credential operation error.")
                System.exit(-1)
            } finally {
                lock.unlock()
            }
        }

        fun indexOf(host: String, user: String): Int {
            val lock = READ_WRITE_LOCK.readLock()
            lock.lock()
            try {
                var index = 1
                for (sshCredential in CREDENTIAL_SET) {
                    if (sshCredential!!.host == host && sshCredential.user == user) {
                        return index
                    }
                    index++
                }
            } catch (e: Exception) {
                MessageBox.setExitMessage("Credential operation error.")
                System.exit(-1)
            } finally {
                lock.unlock()
            }
            return -1
        }

        fun getCredential(index: Int): SshCredential? {
            val lock = READ_WRITE_LOCK.readLock()
            lock.lock()
            try {
                var loopIdx = 1
                for (sshCredential in CREDENTIAL_SET) {
                    if (loopIdx++ == index) {
                        return sshCredential
                    }
                }
            } catch (e: Exception) {
                MessageBox.setExitMessage("Credential operation error.")
                System.exit(-1)
            } finally {
                lock.unlock()
            }
            return null
        }

        fun getCredential(host: String): SshCredential? {
            val lock = READ_WRITE_LOCK.readLock()
            lock.lock()
            try {
                for (sshCredential in CREDENTIAL_SET) {
                    if (sshCredential!!.host == host) {
                        return sshCredential
                    }
                }
            } catch (e: Exception) {
                MessageBox.setExitMessage("Credential operation error.")
                exitProcess(-1)
            } finally {
                lock.unlock()
            }
            return null
        }

        fun containsCredential(credential: SshCredential?): Boolean {
            val lock = READ_WRITE_LOCK.readLock()
            lock.lock()
            try {
                return CREDENTIAL_SET.contains(credential)
            } catch (e: Exception) {
                MessageBox.setExitMessage("Credential operation error.")
                System.exit(-1)
            } finally {
                lock.unlock()
            }
            return false
        }

        fun addCredential(credential: SshCredential?) {
            val lock = READ_WRITE_LOCK.writeLock()
            lock.lock()
            try {
                CREDENTIAL_SET.add(credential)
            } catch (e: Exception) {
                MessageBox.setExitMessage("Credential operation error.")
                System.exit(-1)
            } finally {
                lock.unlock()
            }
        }

        fun deleteCredential(index: Int): String? {
            val lock = READ_WRITE_LOCK.writeLock()
            lock.lock()
            try {
                var tag = 0
                val iterator = CREDENTIAL_SET.iterator()
                while (iterator.hasNext()) {
                    tag++
                    val next = iterator.next()
                    if (tag == index) {
                        iterator.remove()
                        return next!!.host
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                MessageBox.setExitMessage("Credential operation error.")
                System.exit(-1)
            } finally {
                lock.unlock()
            }
            return null
        }

        val allSwitchable: Collection<Switchable>
            get() = ArrayList<Switchable>(CREDENTIAL_SET)
    }
}