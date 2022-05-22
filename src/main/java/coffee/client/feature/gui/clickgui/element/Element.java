/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.clickgui.element;

import net.minecraft.client.util.math.MatrixStack;

public abstract class Element {
    protected double x, y, width, height;

    public Element(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public boolean inBounds(double cx, double cy) {
        return cx >= x && cx < x + width && cy >= y && cy < y + height;
    }

    abstract public boolean clicked(double x, double y, int button);

    abstract public boolean dragged(double x, double y, double deltaX, double deltaY, int button);

    abstract public boolean released();

    abstract public boolean keyPressed(int keycode, int modifiers);

    abstract public void render(MatrixStack matrices, double mouseX, double mouseY, double scrollBeingUsed);

    public boolean scroll(double mouseX, double mouseY, double amount) {
        return false;
    }

    abstract public void tickAnim();

    abstract public boolean charTyped(char c, int mods);

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }
}
