package com.toocol.ssh.core.cache;

import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.credentials.vo.SshCredential;
import io.vertx.core.json.JsonArray;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/2 15:46
 */
public class CredentialCache {

    private static final Set<SshCredential> CREDENTIAL_SET = new TreeSet<>(Comparator.comparingInt(credential -> credential.getHost().hashCode()));
    private static final ReadWriteLock READ_WRITE_LOCK = new ReentrantReadWriteLock();

    public static int credentialsSize() {
        Lock lock = READ_WRITE_LOCK.readLock();
        lock.lock();
        try {
            return CREDENTIAL_SET.size();
        } catch (Exception e) {
            Printer.println("System devastating error.");
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
            Printer.println("System devastating error.");
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
                Printer.println("[" + idx.getAndIncrement() + "]\t\t" + credential.getHost() + "@" + credential.getUser());
            });
        } catch (Exception e) {
            Printer.println("System devastating error.");
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
            Printer.println("System devastating error.");
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
            Printer.println("System devastating error.");
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
        }  catch (Exception e) {
            Printer.println("System devastating error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
    }
}
