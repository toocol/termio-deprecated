package com.toocol.termio.platform.text;

import com.toocol.termio.platform.component.IComponent;
import com.toocol.termio.utilities.log.Loggable;
import javafx.scene.image.ImageView;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/14 18:21
 * @version: 0.0.1
 */
public class Cursor extends ImageView implements IComponent, Loggable {

    /**
     * This is the position which text should be inserted at.
     */
    private int inlinePosition;

    public final long id;

    public Cursor(long id) {
        this.id = id;
        registerComponent(id);
    }

    @Override
    public void initialize() {

    }

    @Override
    public long id() {
        return id;
    }

    public void moveLeft() {
        inlinePosition = Math.max(inlinePosition - 1, 0);
    }

    public void moveRight() {
        inlinePosition += 1;
    }

    public void update(int val) {
        inlinePosition = Math.max(inlinePosition + val, 0);
    }

    public void setTo(int val) {
        inlinePosition = val;
    }

    public int getInlinePosition() {
        return inlinePosition;
    }
}
