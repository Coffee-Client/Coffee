package coffee.client.feature.gui.element.impl;

import coffee.client.feature.gui.element.Element;
import coffee.client.helper.font.adapter.FontAdapter;
import lombok.Setter;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class TextElement extends Element {
    FontAdapter fa;
    @Setter
    String text;
    @Setter
    boolean center;
    @Setter
    Color color;

    public TextElement(FontAdapter renderer, String text, Color color, boolean center, double x, double y, double width, double height) {
        super(x, y, width, height);
        this.fa = renderer;
        this.text = text;
        this.center = center;
        this.color = color;
    }

    public TextElement(FontAdapter renderer, String text, Color color, boolean center, double x, double y, double width) {
        this(renderer, text, color, center, x, y, width, renderer.getFontHeight());
    }

    public TextElement(FontAdapter renderer, String text, Color color, boolean center, double x, double y) {
        this(renderer, text, color, center, x, y, renderer.getStringWidth(text));
    }

    @Override
    public void tickAnimations() {

    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        if (center)
            fa.drawCenteredString(stack, text, getPositionX() + getWidth() / 2d, getPositionY() + getHeight() / 2d - fa.getFontHeight() / 2d, color.getRGB());
        else fa.drawString(stack, text, getPositionX(), getPositionY(), color.getRGB());
    }


    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return false;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return false;
    }

    @Override
    public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
        return false;
    }

    @Override
    public boolean charTyped(char c, int mods) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int mods) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int mods) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        return false;
    }
}
