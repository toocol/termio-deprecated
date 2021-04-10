package com.toocol.ssh.core.router;

import com.toocol.ssh.core.router.annotation.Route;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/4/2 10:12
 */
public class MessageAddress {

    /**
     * this method is writing for inner enum
     *
     * @param iAddress
     * @return
     */
    private static Optional<String> nextAddress(IAddress iAddress) {
        Route route = iAddress.getClass().getAnnotation(Route.class);
        if (route == null) {
            return Optional.empty();
        }
        String nextAddress = route.nextAddress();
        if (StringUtils.isEmpty(nextAddress)) {
            return Optional.empty();
        }
        return Optional.of(nextAddress);
    }

    /**
     * this method is writing for inner enum
     *
     * @param objects
     * @param address
     * @return
     */
    private static Optional<IAddress> addressOf(IAddress[] objects, String address) {
        if (StringUtils.isEmpty(address)) {
            return Optional.empty();
        }
        return Arrays
                .stream(objects)
                .filter(obj -> address.equals(obj.address()))
                .findFirst();
    }

    public interface IAddress {
        /**
         * return the address string
         *
         * @return
         */
        String address();

        /**
         * return the next address string in all program process
         * @return optional
         */
        Optional<String> nextAddress();

        /**
         * according to the address string returns the IAddress enum object;
         *
         * @param address the given address
         * @return optional
         */
        Optional<IAddress> addressOf(String address);
    }

    @AllArgsConstructor
    public enum TerminalView implements IAddress{
        /**
         * when the screen has been cleaned.
         */
        @Route(nextAddress = "")
        ADDRESS_SCREEN_HAS_CLEARED("ssh.terminal.view"),

        /**
         * when user selected the launch mode.
         */
        ADDRESS_MODE_SELECTION_DONE("ssh.mode.selection.done"),
        ;

        /**
         * the address string of message
         */
        public final String address;

        @Override
        public String address() {
            return address;
        }

        @Override
        public Optional<String> nextAddress() {
            return MessageAddress.nextAddress(this);
        }

        @Override
        public Optional<IAddress> addressOf(String address) {
            return MessageAddress.addressOf(values(), address);
        }
    }

    @AllArgsConstructor
    public enum ClearScreen implements IAddress{
        /**
         * to clean the screen
         */
        ADDRESS_CLEAR("ssh.command.clear")
        ;

        /**
         * the address string of message
         */
        public final String address;

        @Override
        public String address() {
            return address;
        }

        @Override
        public Optional<String> nextAddress() {
            return MessageAddress.nextAddress(this);
        }

        @Override
        public Optional<IAddress> addressOf(String address) {
            return MessageAddress.addressOf(values(), address);
        }
    }

    @AllArgsConstructor
    public enum CommandAcceptor implements IAddress {
        /**
         * to accept the user command input
         */
        ADDRESS_ACCEPT_COMMAND("ssh.command.accept"),
        /**
         * accept anykey input
         */
        ADDRESS_ACCEPT_ANYKEY("ssh.accept.anykey"),
        /**
         * accept user select the launch mode
         */
        ADDRESS_ACCEPT_SELECTION("ssh.accept.selection")
        ;
        /**
         * the address string of message
         */
        public final String address;

        @Override
        public String address() {
            return address;
        }

        @Override
        public Optional<String> nextAddress() {
            return MessageAddress.nextAddress(this);
        }

        @Override
        public Optional<IAddress> addressOf(String address) {
            return MessageAddress.addressOf(values(), address);
        }
    }

    @AllArgsConstructor
    public enum CommandExecutor implements IAddress {
        /**
         * execute shell command
         */
        ADDRESS_EXECUTE_SHELL("ssh.command.execute.shell"),
        /**
         * execute outside command
         */
        ADDRESS_EXECUTE_OUTSIDE("ssh.command.execute.outside")
        ;
        /**
         * the address string of message
         */
        public final String address;

        @Override
        public String address() {
            return address;
        }

        @Override
        public Optional<String> nextAddress() {
            return MessageAddress.nextAddress(this);
        }

        @Override
        public Optional<IAddress> addressOf(String address) {
            return MessageAddress.addressOf(values(), address);
        }
    }

    @AllArgsConstructor
    public enum FileReader implements IAddress {
        /**
         * read the credential file(credentials.json)
         */
        ADDRESS_READ_CREDENTIAL("terminal.file.read.credential")
        ;
        /**
         * the address string of message
         */
        public final String address;

        @Override
        public String address() {
            return address;
        }

        @Override
        public Optional<String> nextAddress() {
            return MessageAddress.nextAddress(this);
        }

        @Override
        public Optional<IAddress> addressOf(String address) {
            return MessageAddress.addressOf(values(), address);
        }
    }

    @AllArgsConstructor
    public enum FileWriter implements IAddress {
        /**
         * write the file to disk
         */
        ADDRESS_WRITE("terminal.file.writer")
        ;
        public final String address;

        @Override
        public String address() {
            return address;
        }

        @Override
        public Optional<String> nextAddress() {
            return MessageAddress.nextAddress(this);
        }

        @Override
        public Optional<IAddress> addressOf(String address) {
            return MessageAddress.addressOf(values(), address);
        }
    }
}
