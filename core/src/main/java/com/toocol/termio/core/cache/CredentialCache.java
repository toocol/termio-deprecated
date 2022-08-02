package com.toocol.termio.core.cache;

import com.toocol.termio.core.auth.core.SshCredential;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.utilities.anis.AnisStringBuilder;
import com.toocol.termio.utilities.anis.Printer;
import com.toocol.termio.utilities.functional.Switchable;
import com.toocol.termio.utilities.utils.MessageBox;
import com.toocol.termio.utilities.utils.StrUtil;
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

    private static CredentialCache instance;
    private final Set<SshCredential> CREDENTIAL_SET = new TreeSet<>(Comparator.comparingInt(credential -> -1 * credential.getHost().hashCode()));
    private final ReadWriteLock READ_WRITE_LOCK = new ReentrantReadWriteLock();

    private CredentialCache() {

    }

    public synchronized static CredentialCache getInstance() {
        if (instance == null) {
            instance = new CredentialCache();
        }
        return instance;
    }

    public int credentialsSize() {
        Lock lock = READ_WRITE_LOCK.readLock();
        lock.lock();
        try {
            return CREDENTIAL_SET.size();
        } catch (Exception e) {
            MessageBox.setExitMessage("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
        return 0;
    }

    public String getCredentialsJson() {
        Lock lock = READ_WRITE_LOCK.readLock();
        lock.lock();
        try {
            return new JsonArray(new ArrayList<>(CREDENTIAL_SET)).toString();
        } catch (Exception e) {
            MessageBox.setExitMessage("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public void showCredentials() {
        Lock lock = READ_WRITE_LOCK.readLock();
        lock.lock();
        try {
            AtomicInteger idx = new AtomicInteger(1);
            Term term = Term.getInstance();
            CREDENTIAL_SET.forEach(credential -> {
                int index = idx.getAndIncrement();
                Printer.print(new AnisStringBuilder()
                        .background(Term.theme.propertiesZoneBgColor)
                        .append(StrUtil.SPACE.repeat(term.getWidth()))
                        .toString()
                );
                term.setCursorPosition(0, term.getCursorPosition()[1]);
                AnisStringBuilder builder = new AnisStringBuilder()
                        .background(Term.theme.propertiesZoneBgColor);
                if (SshSessionCache.getInstance().isAlive(credential.getHost())) {
                    builder.front(Term.theme.sessionAliveColor);
                } else {
                    builder.front(Term.theme.indexFrontColor);
                }
                builder.append("[" + (index < 10 ? "0" + index : index) + "]\t\t").deFront()
                        .front(Term.theme.userHighlightColor).append(credential.getUser()).deFront()
                        .front(Term.theme.atHighlightColor).append("@").deFront()
                        .front(Term.theme.hostHighlightColor).append(credential.getHost()).deFront();
                Printer.println(builder.toString());
            });
        } catch (Exception e) {
            MessageBox.setExitMessage("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
    }

    public int indexOf(String host, String user) {
        Lock lock = READ_WRITE_LOCK.readLock();
        lock.lock();
        try {
            int index = 1;
            for (SshCredential sshCredential : CREDENTIAL_SET) {
                if (sshCredential.getHost().equals(host) && sshCredential.getUser().equals(user)) {
                    return index;
                }
                index++;
            }
        } catch (Exception e) {
            MessageBox.setExitMessage("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
        return -1;
    }

    public SshCredential getCredential(int index) {
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
            MessageBox.setExitMessage("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public SshCredential getCredential(String host) {
        Lock lock = READ_WRITE_LOCK.readLock();
        lock.lock();
        try {
            for (SshCredential sshCredential : CREDENTIAL_SET) {
                if (sshCredential.getHost().equals(host)) {
                    return sshCredential;
                }
            }
        } catch (Exception e) {
            MessageBox.setExitMessage("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public boolean containsCredential(SshCredential credential) {
        Lock lock = READ_WRITE_LOCK.readLock();
        lock.lock();
        try {
            return CREDENTIAL_SET.contains(credential);
        } catch (Exception e) {
            MessageBox.setExitMessage("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
        return false;
    }

    public void addCredential(SshCredential credential) {
        Lock lock = READ_WRITE_LOCK.writeLock();
        lock.lock();
        try {
            CREDENTIAL_SET.add(credential);
        } catch (Exception e) {
            MessageBox.setExitMessage("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
    }

    public String deleteCredential(int index) {
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
            MessageBox.setExitMessage("Credential operation error.");
            System.exit(-1);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public Collection<Switchable> getAllSwitchable() {
        return new ArrayList<>(CREDENTIAL_SET);
    }
}
