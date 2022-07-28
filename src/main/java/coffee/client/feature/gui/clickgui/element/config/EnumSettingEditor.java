/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.clickgui.element.config;

import coffee.client.feature.config.EnumSetting;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.ClipStack;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnumSettingEditor extends SettingEditor<EnumSetting<?>> {

    static final FontAdapter fa = FontRenderers.getCustomSize(14);
    final double headerPad = 4;
    final List<Enum<?>> ese = new ArrayList<>();
    double expandProg = 0;
    boolean expanded = false;

    public EnumSettingEditor(double x, double y, double width, EnumSetting<?> confValue) {
        super(x, y, width, 0, confValue);

        this.ese.addAll(Arrays.asList(confValue.getValues()));
        setHeight(fa.getFontHeight() + headerPad);
    }

    double headerHeight() {
        return fa.getFontHeight() + 4;
    }

    @Override
    public double getHeight() {
        return headerHeight() + (ese.size() * (fa.getFontHeight() + 2)) * Transitions.easeOutExpo(expandProg);
    }

    @Override
    public void tickAnimations() {
        double d = 0.02;
        if (!expanded) {
            d *= -1;
        }
        expandProg += d;
        expandProg = MathHelper.clamp(expandProg, 0, 1);
    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        super.render(stack, mouseX, mouseY);
        Renderer.R2D.renderRoundedQuad(stack,
                new Color(40, 40, 40),
                getPositionX(),
                getPositionY(),
                getPositionX() + getWidth(),
                getPositionY() + getHeight(),
                2,
                8
        );
        fa.drawString(stack, configValue.name, getPositionX() + headerPad / 2d, getPositionY() + headerHeight() / 2d - fa.getFontHeight() / 2d, 0xFFFFFF);
        if (expandProg != 1) {
            fa.drawString(stack,
                    configValue.getValue().name(),
                    (float) (getPositionX() + getWidth() - fa.getStringWidth(configValue.getValue().name()) - 2),
                    (float) (getPositionY() + headerHeight() / 2d - fa.getFontHeight() / 2d),
                    1f,
                    1f,
                    1f,
                    (float) Transitions.easeOutExpo(1 - expandProg)
            );
        }
        if (expandProg != 0) {
            float pp = (float) Transitions.easeOutExpo(expandProg);
            Renderer.setupRender();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            float arrowDim = 3;
            stack.push();
            stack.translate(getPositionX() + getWidth() - arrowDim - 2, getPositionY() + headerHeight() / 2d + arrowDim / 2d, 0);
            Matrix4f m = stack.peek().getPositionMatrix();

            bufferBuilder.vertex(m, -arrowDim, -arrowDim, 0).color(1f, 1f, 1f, pp).next();
            bufferBuilder.vertex(m, 0, 0, 0).color(1f, 1f, 1f, pp).next();
            bufferBuilder.vertex(m, arrowDim, -arrowDim, 0).color(1f, 1f, 1f, pp).next();

            BufferRenderer.drawWithShader(bufferBuilder.end());
            Renderer.endRender();
            stack.pop();

            ClipStack.globalInstance.addWindow(stack,
                    new Rectangle(getPositionX(), getPositionY() + headerHeight(), getPositionX() + getWidth(), getPositionY() + getHeight())
            );

            double offsetY = 0;
            double ballDim = 3;
            double innerBallDim = 2;
            for (Enum<?> enumSettingEntry : ese) {
                Renderer.R2D.renderCircle(stack,
                        new Color(20, 20, 20),
                        getPositionX() + 2 + ballDim,
                        getPositionY() + headerHeight() + offsetY + fa.getFontHeight() / 2d,
                        ballDim,
                        20
                );
                if (enumSettingEntry.ordinal() == configValue.getValue().ordinal()) {
                    Renderer.R2D.renderCircle(stack,
                            new Color(9, 162, 104),
                            getPositionX() + 2 + ballDim,
                            getPositionY() + headerHeight() + offsetY + fa.getFontHeight() / 2d,
                            innerBallDim,
                            20
                    );
                }
                fa.drawString(stack, enumSettingEntry.name(), getPositionX() + ballDim * 2 + 4, getPositionY() + headerHeight() + offsetY, 0xFFFFFF);
                offsetY += fa.getFontHeight() + 2;
            }

            ClipStack.globalInstance.popWindow();
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (new Rectangle(getPositionX(), getPositionY(), getPositionX() + getWidth(), getPositionY() + headerHeight()).contains(x, y)) {
            expanded = !expanded;
            return true;
        } else if (new Rectangle(getPositionX(), getPositionY() + headerHeight(), getPositionX() + getWidth(), getPositionY() + getHeight()).contains(x,
                y
        ) && expanded) {
            double offsetY = 0;
            for (Enum<?> enumSettingEntry : ese) {
                double startY = getPositionY() + headerHeight() + offsetY;
                double endY = getPositionY() + headerHeight() + offsetY + fa.getFontHeight() + 2;
                if (startY <= y && endY > y) {
                    configValue.accept(enumSettingEntry.name()); // generic moment
                    break; // found our value, cancel
                }
                offsetY += fa.getFontHeight() + 2;
            }
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

}
