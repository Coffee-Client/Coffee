/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.widget;

import coffee.client.feature.gui.DoesMSAA;
import coffee.client.feature.gui.FastTickable;
import coffee.client.feature.gui.HasSpecialCursor;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Cursor;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public class RoundButton implements Element, Drawable, Selectable, FastTickable, DoesMSAA, HasSpecialCursor {

    public static final Color STANDARD = new Color(255, 255, 255);
    public static final Color SUCCESS = new Color(0x24FC2B);
    public static final Color DANGER = new Color(0xFF5722);

    final Runnable onPress;
    final Color textColor;
    String text;
    double x, y, width, height;
    double animProgress = 0;
    boolean isHovered = false;
    boolean enabled = true;
    @Setter
    @Getter
    boolean visible = true;

    public RoundButton(Color color, double x, double y, double w, double h, String t, Runnable a) {
        this.onPress = a;
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.text = t;
        this.textColor = color;
    }

    @Override
    public long getCursor() {
        return Cursor.CLICK;
    }

    @Override
    public boolean shouldApplyCustomCursor() {
        return isHovered && isVisible();
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void onFastTick() {
        double d = 0.04;
        if (!isHovered) {
            d *= -1;
        }
        animProgress += d;
        animProgress = MathHelper.clamp(animProgress, 0, 1);

    }

    double easeInOutQuint(double x) {
        return x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2;
    }

    boolean inBounds(double cx, double cy) {
        return cx >= x && cx < x + width && cy >= y && cy < y + height;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        isHovered = inBounds(mouseX, mouseY) && isEnabled();
        if (!isVisible()) {
            return;
        }
        matrices.push();
        matrices.translate(x + width / 2d, y + height / 2d, 0);
        float animProgress = (float) Transitions.easeOutExpo(this.animProgress);
        matrices.scale(MathHelper.lerp(animProgress, 1f, 1.01f), MathHelper.lerp(animProgress, 1f, 1.01f), 1f);
        double originX = -width / 2d;
        double originY = -height / 2d;
        Renderer.R2D.renderRoundedQuad(matrices, new Color(30, 30, 30), originX, originY, width / 2d, height / 2d, Math.min(height / 2d, 5), 20);
        if (animProgress != 0) {
            Renderer.R2D.renderRoundedShadow(matrices, new Color(10, 10, 10, 100), originX, originY, width / 2d, height / 2d, Math.min(height / 2d, 5), 20,
                    animProgress * 3);
        }
        FontRenderers.getRenderer()
                .drawString(matrices, text, -(FontRenderers.getRenderer().getStringWidth(text)) / 2f, -FontRenderers.getRenderer().getMarginHeight() / 2f,
                        isEnabled() ? textColor.getRGB() : 0xAAAAAA, false);
        matrices.pop();
    }

    @Override
    public SelectionType getType() {
        return isHovered ? SelectionType.HOVERED : SelectionType.NONE;
    }


    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) {
            return false;
        }
        if (inBounds(mouseX, mouseY) && isEnabled() && button == 0) {
            onPress.run();
            return true;
        }
        return false;
    }
}
