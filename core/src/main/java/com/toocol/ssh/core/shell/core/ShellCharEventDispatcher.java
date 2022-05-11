package com.toocol.ssh.core.shell.core;

import com.google.common.collect.ImmutableMap;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.utilities.event.CharEvent;
import com.toocol.ssh.utilities.utils.CastUtil;
import com.toocol.ssh.utilities.utils.ClassScanner;
import com.toocol.ssh.utilities.utils.ExitMessage;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 19:32
 */
public final class ShellCharEventDispatcher {

    private static final ImmutableMap<CharEvent, ShellCharAction> actionMap;

    static {
        Map<CharEvent, ShellCharAction> map = new HashMap<>();
        new ClassScanner("com.toocol.ssh.core.shell.core", clazz -> clazz.getSuperclass().equals(ShellCharAction.class))
                .scan()
                .forEach(clazz -> {
                    try {
                        Constructor<ShellCharAction> declaredConstructor = CastUtil.cast(clazz.getDeclaredConstructor());
                        declaredConstructor.setAccessible(true);
                        ShellCharAction charAction = declaredConstructor.newInstance();
                        for (CharEvent event : charAction.watch()) {
                            if (map.containsKey(event)) {
                                throw new RuntimeException("Char event conflict.");
                            }
                            map.put(event, charAction);
                        }
                    } catch (Exception e) {
                        ExitMessage.setMsg("Register char event action failed.");
                        System.exit(-1);
                    }
                });
        actionMap = ImmutableMap.copyOf(map);
    }

    public static void init() {

    }

    public boolean dispatch(Shell shell, char inChar) {
        CharEvent charEvent = CharEvent.eventOf(inChar);
        if (charEvent == null) {
            return false;
        }

        if (actionMap.containsKey(charEvent)) {
            boolean isBreak = Objects.requireNonNull(actionMap.get(charEvent)).act(shell, charEvent, inChar);
            if (isBreak) {
                ShellCharAction.reset();
            }
            return isBreak;
        }
        return false;
    }

}
