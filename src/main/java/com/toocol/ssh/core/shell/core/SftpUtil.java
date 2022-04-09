package com.toocol.ssh.core.shell.core;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.toocol.ssh.core.cache.SessionCache;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:46
 * @version: 0.0.1
 */
public class SftpUtil {

    public static ChannelSftp getSftp(long sessionId) throws JSchException {
        Session session = SessionCache.getInstance().getSession(sessionId);
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
        return channelSftp;
    }

}
