package com.toocol.ssh.core.mosh.core.network;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/9 23:48
 * @version: 0.0.1
 */
public class NetworkConstants {
    public static final int MOSH_PROTOCOL_VERSION = 2;

    public static final double SRIT = 1000;
    public static final double RTTVAR = 500;

    public static final long MIN_RTO = 50; /* ms */
    public static final long MAX_RTO = 1000; /* ms */

    public static final int SEND_INTERVAL_MIN = 20; /* ms between frames */
    public static final int SEND_INTERVAL_MAX = 250; /* ms between frames */
    public static final int ACK_INTERVAL = 3000; /* ms between empty acks */
    public static final int ACK_DELAY = 100; /* ms before delayed ack */
    public static final int SHUTDOWN_RETRIES = 16; /* number of shutdown packets to send before giving up */
    public static final int ACTIVE_RETRY_TIMEOUT = 10000; /* attempt to resend at frame rate */

    /**
     * Application datagram MTU. For constructors and fallback.
     */
    static final int DEFAULT_SEND_MTU = 500;

}
