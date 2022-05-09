package com.toocol.ssh.core.mosh.core.network;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/9 23:22
 * @version: 0.0.1
 */
public interface ICompressorAcquirer {

    default Compressor getCompressor() {
        return Compressor.get();
    }

}
