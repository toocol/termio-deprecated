package com.toocol.termio.core.cache

import com.toocol.termio.core.Termio
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
        private val credentialSet: MutableSet<SshCredential?> = TreeSet(Comparator.comparingInt { credential: SshCredential? -> -1 * credential!!.host.hashCode() })
        private val readWriteLock: ReadWriteLock = ReentrantReadWriteLock()
        fun credentialsSize(): Int {
            val lock = readWriteLock.readLock()
            lock.lock()
            try {
                return credentialSet.size
            } catch (e: Exception) {
                MessageBox.setExitMessage("Credential operation error.")
                exitProcess(-1)
            } finally {
                lock.unlock()
            }
        }

        val credentialsJson: String
            get() {
                val lock = readWriteLock.readLock()
                lock.lock()
                try {
                    return JsonArray(ArrayList(credentialSet)).toString()
                } catch (e: Exception) {
                    MessageBox.setExitMessage("Credential operation error.")
                    exitProcess(-1)
                } finally {
                    lock.unlock()
                }
            }

        fun showCredentials() {
            val theme = Term.theme
            val lock = readWriteLock.readLock()
            lock.lock()
            try {
                val idx = AtomicInteger(1)
                val term = Term.instance
                credentialSet.forEach(Consumer { credential: SshCredential? ->
                    val index = idx.getAndIncrement()
                    Printer.print(AnsiStringBuilder()
                        .background(theme.propertiesZoneBgColor.color)
                        .append(StrUtil.SPACE.repeat(Termio.windowWidth))
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
                exitProcess(-1)
            } finally {
                lock.unlock()
            }
        }

        fun indexOf(host: String, user: String): Int {
            val lock = readWriteLock.readLock()
            lock.lock()
            try {
                var index = 1
                for (sshCredential in credentialSet) {
                    if (sshCredential!!.host == host && sshCredential.user == user) {
                        return index
                    }
                    index++
                }
            } catch (e: Exception) {
                MessageBox.setExitMessage("Credential operation error.")
                exitProcess(-1)
            } finally {
                lock.unlock()
            }
            return -1
        }

        fun getCredential(index: Int): SshCredential? {
            val lock = readWriteLock.readLock()
            lock.lock()
            try {
                var loopIdx = 1
                for (sshCredential in credentialSet) {
                    if (loopIdx++ == index) {
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

        fun getCredential(host: String): SshCredential? {
            val lock = readWriteLock.readLock()
            lock.lock()
            try {
                for (sshCredential in credentialSet) {
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
            val lock = readWriteLock.readLock()
            lock.lock()
            try {
                return credentialSet.contains(credential)
            } catch (e: Exception) {
                MessageBox.setExitMessage("Credential operation error.")
                exitProcess(-1)
            } finally {
                lock.unlock()
            }
        }

        fun addCredential(credential: SshCredential?) {
            val lock = readWriteLock.writeLock()
            lock.lock()
            try {
                credentialSet.add(credential)
            } catch (e: Exception) {
                MessageBox.setExitMessage("Credential operation error.")
                exitProcess(-1)
            } finally {
                lock.unlock()
            }
        }

        fun deleteCredential(index: Int): String? {
            val lock = readWriteLock.writeLock()
            lock.lock()
            try {
                var tag = 0
                val iterator = credentialSet.iterator()
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
                exitProcess(-1)
            } finally {
                lock.unlock()
            }
            return null
        }

        val allSwitchable: Collection<Switchable>
            get() = ArrayList<Switchable>(credentialSet)
    }
}