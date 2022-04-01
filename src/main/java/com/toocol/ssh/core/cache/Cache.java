package com.toocol.ssh.core.cache;

import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.credentials.vo.SshCredential;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/1 0:08
 * @version: 0.0.1
 */
public class Cache {

    public volatile static String CURRENT_COMMAND = "";

    public volatile static boolean HANGED_QUIT = false;

    public volatile static boolean HANGED_ENTER = false;

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

    public static void showCredentials() {
        Lock lock = READ_WRITE_LOCK.readLock();
        lock.lock();
        try {
            AtomicInteger idx = new AtomicInteger(1);
            Cache.CREDENTIAL_SET.forEach(credential -> {
                Printer.println("[" + idx + "]\t\t" + credential.getHost() + "@" + credential.getUser());
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
