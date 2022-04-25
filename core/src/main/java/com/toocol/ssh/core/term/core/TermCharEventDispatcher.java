package com.toocol.ssh.core.term.core;

import com.google.common.collect.ImmutableMap;
import com.toocol.ssh.utilities.event.CharEvent;
import com.toocol.ssh.utilities.utils.CastUtil;
import com.toocol.ssh.utilities.utils.ClassScanner;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 19:32
 */
public final class TermCharEventDispatcher {

    private static final ImmutableMap<CharEvent, TermCharAction> actionMap;

    static {
        Map<CharEvent, TermCharAction> map = new HashMap<>();
        new ClassScanner("com.toocol.ssh.core.term.core", clazz -> clazz.getSuperclass().equals(TermCharAction.class))
                .scan()
                .forEach(clazz -> {
                    try {
                        Constructor<TermCharAction> declaredConstructor = CastUtil.cast(clazz.getDeclaredConstructor());
                        declaredConstructor.setAccessible(true);
                        TermCharAction charAction = declaredConstructor.newInstance();
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

    public boolean dispatch(Term term, char inChar) {
        CharEvent charEvent = CharEvent.eventOf(inChar);
        if (charEvent == null) {
            return false;
        }

        if (actionMap.containsKey(charEvent)) {
            return Objects.requireNonNull(actionMap.get(charEvent)).act(term, charEvent, inChar);
        }
        return false;
    }
}
