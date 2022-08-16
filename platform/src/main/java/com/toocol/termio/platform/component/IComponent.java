package com.toocol.termio.platform.component;

import com.toocol.termio.utilities.utils.Asable;
import com.toocol.termio.utilities.utils.Castable;
import javafx.scene.Node;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/5 17:44
 */
public interface IComponent extends Asable, Castable {

    /**
     * Initialize the component.
     */
    void initialize();

    /**
     * Get current component's id
     */
    long id();

    /**
     * Find Javafx Node represented by Component by registered id
     *
     * @return Optional<Node>
     */
    default <T extends IComponent> T findComponent(Class<T> clazz, long id) {
        return ComponentsContainer.get(clazz, id);
    }

    /**
     * Register the component, so you can invoke {@link IComponent#findComponent(java.lang.Class, long)}
     * to get any components have registered by id.
     */
    default void registerComponent(long id) {
        ComponentsContainer.put(this.getClass(), id, this);
    }

    /**
     * If the component is subclass of Node, hide this component.
     */
    default void hide() {
        if (this instanceof Node) {
            Node node = this.as();
            node.setManaged(false);
            node.setVisible(false);
        }
    }

    /**
     * If the component is subclass of Node, hide this component.
     */
    default void show() {
        if (this instanceof Node) {
            Node node = this.as();
            node.setManaged(true);
            node.setVisible(true);
        }
    }
}
