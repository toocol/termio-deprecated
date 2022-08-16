package com.toocol.termio.core.cache;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/1 0:08
 * @version: 0.0.1
 */
public class StatusCache {

    public volatile static boolean STOP_ACCEPT_OUT_COMMAND = false;

    public volatile static boolean HANGED_QUIT = false;

    public volatile static boolean HANGED_ENTER = false;

    public volatile static boolean JUST_CLOSE_EXHIBIT_SHELL = false;

    public volatile static boolean SHOW_WELCOME = false;

    public volatile static boolean ACCEPT_SHELL_CMD_IS_RUNNING = false;

    public volatile static boolean ACCESS_EXHIBIT_SHELL_WITH_PROMPT = true;

    public volatile static boolean STOP_PROGRAM = false;

    public volatile static boolean EXECUTE_CD_CMD = false;

    public volatile static boolean EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE = false;

    public volatile static boolean SWITCH_SESSION = false;

    public volatile static boolean SWITCH_SESSION_WAIT_HANG_PREVIOUS = false;


    public volatile static long MONITOR_SESSION_ID;

}
