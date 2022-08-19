package com.toocol.termio.platform.component;

import com.toocol.termio.utilities.log.Loggable;
import javafx.scene.Node;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 1:00
 * @version: 0.0.1
 */
public class ComponentsAnnotationParser implements Loggable {
    private final List<IComponent> components = new ArrayList<>();

    public ComponentsAnnotationParser() {
    }

    public void parse(Class<?> clazz) {
        RegisterComponent register = clazz.getAnnotation(RegisterComponent.class);
        if (register == null) {
            return;
        }
        try {
            for (Component component : register.value()) {
                Constructor<? extends IComponent> constructor = component.clazz().getDeclaredConstructor(long.class);
                constructor.setAccessible(true);
                IComponent iComponent = constructor.newInstance(component.id());
                if (!component.initialVisible()) {
                    Node node = iComponent.as();
                    node.setVisible(false);
                    node.setManaged(false);
                }
                components.add(iComponent);
            }
        } catch (Exception e) {
            error("Parse register components failed.");
            System.exit(-1);
        }
    }

    public List<IComponent> getComponents() {
        return components;
    }

}
