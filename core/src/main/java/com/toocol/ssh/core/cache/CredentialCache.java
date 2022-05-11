package com.toocol.ssh.core.cache;

import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.utils.ExitMessage;
import io.vertx.core.json.JsonArray;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/2 15:46
 */
public class CredentialCache {

    private static final Set<SshCredential> CREDENTIAL_SET = new TreeSet<>(Comparator.comparingInt(credential -> -1 * credential.getHost().hashCode()));
    private static final ReadWriteLock READ_WRITE_LOCK = new ReentrantReadWriteLock();

    public static int credentialsSize() {
        Lock lock = READ_WRITE_LOCK.readLock();
        lock.lock();
        try {
            return CREDENTIAL_SET.size();
        } catch (Exception e) {
            ExitMessage.setMsg("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
        return 0;
    }

    public static String getCredentialsJson() {
        Lock lock = READ_WRITE_LOCK.readLock();
        lock.lock();
        try {
            return new JsonArray(new ArrayList<>(CREDENTIAL_SET)).toString();
        } catch (Exception e) {
            ExitMessage.setMsg("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public static void showCredentials() {
        Lock lock = READ_WRITE_LOCK.readLock();
        lock.lock();
        try {
            AtomicInteger idx = new AtomicInteger(1);
            CREDENTIAL_SET.forEach(credential -> {
                int index = idx.getAndIncrement();
                AnisStringBuilder builder = new AnisStringBuilder()
                        .append("[" + (index < 10 ? "0" + index : index) + "]\t\t")
                        .append(credential.getUser())
                        .append("@")
                        .front(Term.theme.hostHighlightColor).append(credential.getHost()).deFront();
                if (SshSessionCache.getInstance().isAlive(credential.getHost())) {
                    builder.append("\t\t").front(Term.theme.sessionAliveColor).append("[alive]").deFront();
                }
                Printer.println(builder.toString());
            });
        } catch (Exception e) {
            ExitMessage.setMsg("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
    }

    public static SshCredential getCredential(int index) {
        Lock lock = READ_WRITE_LOCK.readLock();
        lock.lock();
        try {
            int loopIdx = 1;
            for (SshCredential sshCredential : CREDENTIAL_SET) {
                if (loopIdx++ == index) {
                    return sshCredential;
                }
            }
        } catch (Exception e) {
            ExitMessage.setMsg("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public static boolean containsCredential(SshCredential credential) {
        Lock lock = READ_WRITE_LOCK.readLock();
        lock.lock();
        try {
            return CREDENTIAL_SET.contains(credential);
        } catch (Exception e) {
            ExitMessage.setMsg("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
        return false;
    }

    public static void addCredential(SshCredential credential) {
        Lock lock = READ_WRITE_LOCK.writeLock();
        lock.lock();
        try {
            CREDENTIAL_SET.add(credential);
        } catch (Exception e) {
            ExitMessage.setMsg("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
    }

    public static String deleteCredential(int index) {
        Lock lock = READ_WRITE_LOCK.writeLock();
        lock.lock();
        try {
            int tag = 0;
            Iterator<SshCredential> iterator = CREDENTIAL_SET.iterator();
            while (iterator.hasNext()) {
                tag++;
                SshCredential next = iterator.next();
                if (tag == index) {
                    iterator.remove();
                    return next.getHost();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ExitMessage.setMsg("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
        return null;
    }
}
