package com.toocol.ssh.core.mosh.core.statesnyc;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/6/30 0:14
 * @version: 0.0.1
 */
public final class RemoteState extends State<RemoteState>{

    @Override
    public void subtract(RemoteState prefix) {

    }

    @Override
    public byte[] diffFrom(RemoteState existing) {
        return new byte[0];
    }

    @Override
    public void pushBack(UserEvent event) {

    }

    @Override
    public RemoteState copy() {
        return null;
    }

    @Override
    public int actionSize() {
        return 0;
    }
}
