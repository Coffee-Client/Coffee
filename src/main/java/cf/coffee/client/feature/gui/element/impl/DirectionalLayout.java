package cf.coffee.client.feature.gui.element.impl;

import cf.coffee.client.feature.gui.element.Element;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;
import java.util.function.Function;

public class DirectionalLayout extends Element {
    @Getter
    List<Element> elements;
    LayoutDirection direction;
    double padding;
    public DirectionalLayout(DirectionalLayout.LayoutDirection direction, double x, double y, double width, double height, double padding, Element... elements) {
        super(x, y, width, height);
        this.elements = List.of(elements);
        this.direction = direction;
        this.padding = padding;
    }

    @Override
    public void tickAnimations() {
        for (Element element : elements) {
            element.tickAnimations();
        }
    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        double posX = 0, posY = 0;
        for (Element element : elements) {
            element.setPositionX(getPositionX()+posX* direction.mulX);
            element.setPositionY(getPositionY()+posY* direction.mulY);
            element.render(stack, mouseX, mouseY);
            posX += element.getWidth()+padding;
            posY += element.getHeight()+padding;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return iterateOverChildren(element -> element.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return iterateOverChildren(element -> element.mouseReleased(mouseX, mouseY, button));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY,double deltaX, double deltaY, int button) {
        return iterateOverChildren(element -> element.mouseDragged(mouseX, mouseY,deltaX, deltaY, button));
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return iterateOverChildren(element -> element.charTyped(chr, modifiers));
    }

    @Override
    public boolean keyPressed(int keyCode,int modifiers) {
        return iterateOverChildren(element -> element.keyPressed(keyCode, modifiers));
    }

    @Override
    public boolean keyReleased(int keyCode, int modifiers) {
        return iterateOverChildren(element -> element.keyReleased(keyCode, modifiers));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return iterateOverChildren(element -> element.mouseScrolled(mouseX, mouseY, amount));
    }

    private boolean iterateOverChildren(Function<Element, Boolean> supp) {
        for (Element element : getElements()) {
            if (supp.apply(element)) return true;
        }
        return false;
    }

    @AllArgsConstructor
    public enum LayoutDirection {
        DOWN(0, 1), RIGHT(1, 0), TOP(0, -1), LEFT(-1, 0);
        final int mulX, mulY;
    }

}
