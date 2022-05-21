package cf.coffee.client.feature.gui.element.impl;

import cf.coffee.client.feature.gui.element.Element;
import cf.coffee.client.helper.font.FontRenderers;
import cf.coffee.client.helper.render.Renderer;
import cf.coffee.client.helper.util.Transitions;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public class ButtonElement extends Element {

    public static final Color STANDARD = new Color(255, 255, 255);
    public static final Color SUCCESS = new Color(0x24FC2B);
    public static final Color DANGER = new Color(0xFF5722);

    final Runnable onPress;
    final Color textColor;
    @Getter
    @Setter
    String text;
    double animProgress = 0;
    boolean isHovered = false;
    @Getter
    @Setter
    boolean enabled = true;
    @Setter
    @Getter
    boolean visible = true;
    public ButtonElement(Color color, double x, double y, double w, double h, String t, Runnable a) {
        super(x, y, w, h);
        this.onPress = a;
        this.text = t;
        this.textColor = color;
    }

    @Override
    public void tickAnimations() {
        double d = 0.04;
        if (!isHovered) {
            d *= -1;
        }
        animProgress += d;
        animProgress = MathHelper.clamp(animProgress, 0, 1);
    }

    @Override
    public void render(MatrixStack matrices, double mouseX, double mouseY) {
        isHovered = inBounds(mouseX, mouseY) && isEnabled();
        if (!isVisible()) return;
        matrices.push();
        matrices.translate(getPositionX() + width / 2d, getPositionY() + height / 2d, 0);
        float animProgress = (float) Transitions.easeOutExpo(this.animProgress);
        matrices.scale(MathHelper.lerp(animProgress, 1f, 1.01f), MathHelper.lerp(animProgress, 1f, 1.01f), 1f);
        double originX = -width / 2d;
        double originY = -height / 2d;
        Renderer.R2D.renderRoundedQuad(matrices, new Color(30, 30, 30), originX, originY, width / 2d, height / 2d, Math.min(height / 2d, 5), 20);
        if (animProgress != 0)
            Renderer.R2D.renderRoundedShadow(matrices, new Color(10, 10, 10, 100), originX, originY, width / 2d, height / 2d, Math.min(height / 2d, 5), 20, animProgress * 3);
        FontRenderers.getRenderer()
                .drawString(matrices, text, -(FontRenderers.getRenderer()
                        .getStringWidth(text)) / 2f, -FontRenderers.getRenderer()
                        .getMarginHeight() / 2f, isEnabled() ? textColor.getRGB() : 0xAAAAAA, false);
        matrices.pop();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (!isVisible()) return false;
        if (inBounds(x, y) && isEnabled() && button == 0) {
            onPress.run();
            return true;
        }
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
