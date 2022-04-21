package com.toocol.ssh.core.shell.core;

import com.google.common.collect.ImmutableMap;
import com.toocol.ssh.common.action.AbstractCharAction;
import com.toocol.ssh.common.event.CharEvent;
import com.toocol.ssh.common.utils.ClassScanner;
import com.toocol.ssh.core.term.core.Printer;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 19:32
 */
public final class CharEventDispatcher {

    private static final ImmutableMap<CharEvent, AbstractCharAction> actionMap;

    static {
        Map<CharEvent, AbstractCharAction> map = new HashMap<>();
        new ClassScanner("com.toocol.ssh.core.shell.core", clazz -> clazz.getSuperclass().equals(AbstractCharAction.class))
                .scan()
                .forEach(clazz -> {
                    try {
                        Constructor<?> declaredConstructor = clazz.getDeclaredConstructor();
                        declaredConstructor.setAccessible(true);
                        AbstractCharAction charAction = (AbstractCharAction) declaredConstructor.newInstance();
                        for (CharEvent event : charAction.watch()) {
                            if (map.containsKey(event)) {
                                throw new RuntimeException("Char event conflict.");
                            }
                            map.put(event, charAction);
                        }
                    } catch (Exception e) {
                        Printer.printErr("Register char event action failed.");
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
                AbstractCharAction.reset();
            }
            return isBreak;
        }
        return false;
    }

}
