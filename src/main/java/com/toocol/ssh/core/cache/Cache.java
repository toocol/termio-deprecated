package com.toocol.ssh.core.cache;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/1 0:08
 * @version: 0.0.1
 */
public class Cache {

    public volatile static boolean LOADING_ACCOMPLISH = false;

    public volatile static boolean HANGED_QUIT = false;

    public volatile static boolean HANGED_ENTER = false;

    public volatile static boolean JUST_CLOSE_EXHIBIT_SHELL = false;

    public volatile static boolean SHOW_WELCOME = false;

}
