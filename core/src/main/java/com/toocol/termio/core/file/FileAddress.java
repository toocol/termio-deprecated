package com.toocol.termio.core.file;

import com.toocol.termio.utilities.address.IAddress;

/**
 * @author JoeZane (joezane.cn@gmail.com)
 * @date 2022/03/29 16:51:33
 */
public enum FileAddress implements IAddress {
    /**
     * check the file is whether exist.
     * If not, create it.
     */
    CHECK_FILE_EXIST("terminal.file.check.exist"),
    /**
     * read the credential file(.credentials)
     */
    READ_FILE("terminal.file.read.file"),
    /**
     * choose the local file.
     */
    CHOOSE_FILE("terminal.file.choose"),
    /**
     * choose the local directory path.
     */
    CHOOSE_DIRECTORY("terminal.directory.choose"),
    ;

    /**
     * the address string of message
     */
    private final String address;

    FileAddress(String address) {
        this.address = address;
    }

    @Override
    public String address() {
        return address;
    }
}