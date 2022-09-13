/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.clickgui.element;

import coffee.client.feature.gui.clickgui.ClickGUI;
import coffee.client.feature.gui.element.Element;
import coffee.client.feature.module.Module;
import coffee.client.helper.font.FontRenderers;
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

public class ModuleDisplay extends Element {
    static final double margin = 2;
    static final double actualHeight = 16;
    final Module module;
    final ConfigDisplay cfd;
    final CategoryDisplay parent;
    double leftAnim = 0;

    public ModuleDisplay(Module module, double x, double y, double width, CategoryDisplay parent) {
        super(x, y, width, actualHeight);
        this.module = module;
        this.cfd = new ConfigDisplay(x, y + actualHeight + margin, width, this.module.config);
        this.parent = parent;
    }

    @Override
    public void tickAnimations() {
        this.cfd.tickAnimations();
        double delta = 0.04;
        if (!module.isEnabled()) {
            delta *= -1;
        }
        leftAnim += delta;
        leftAnim = MathHelper.clamp(leftAnim, 0, 1);
    }

    boolean shouldBeActive() {
        return ClickGUI.instance().matchesSearchTerm(module.getName());
    }

    @Override
    public boolean isActive() {
        return shouldBeActive();
    }

    @Override
    public void render(MatrixStack stack, double x, double y) {
        double bruhHeight = actualHeight;
        if (new Rectangle(getPositionX(), getPositionY(), getPositionX() + getWidth(), getPositionY() + bruhHeight).contains(x, y)) {
            int parentIndex = ClickGUI.instance().getIndex(parent); // get index of current element (index 0 = last in render queue)
            if (parentIndex >= 0) { // was the element found? (this should always be yes)
                boolean render = true;
                if (parentIndex > 0) { // are we *NOT* first in queue?
                    for (int i = parentIndex - 1; i >= 0; i--) { // go over all the elements before us
                        Element e = ClickGUI.instance().getChild(i);
                        if (e.inBounds(x, y)) { // is the mouse over said element before us? (will be above us in rendering)
                            render = false; // dont render our tooltip and selection
                            break;
                        }
                    }
                }
                if (render) { // should we render our tooltip and selection?
                    if (!module.isDisabled()) {
                        Renderer.R2D.renderRoundedQuad(stack,
                            new Color(255, 255, 255, 30),
                            getPositionX(),
                            getPositionY(),
                            getPositionX() + getWidth(),
                            getPositionY() + bruhHeight,
                            2,
                            10);
                        ClickGUI.instance().setTooltip(module.getDescription());
                    } else {
                        ClickGUI.instance().setTooltip(module.getDescription() + "\nThis module is disabled: " + module.getDisabledReason());
                    }
                }
            }
        }
        if (leftAnim != 0) {
            double a = Transitions.easeOutExpo(leftAnim);
            ClipStack.globalInstance.addWindow(stack,
                new Rectangle(getPositionX() + 2, getPositionY(), getPositionX() + 2 + 1.5, getPositionY() + 2 + (bruhHeight - 3) * a)); // leave one pixel at the bottom
            Renderer.R2D.renderRoundedQuad(stack,
                new Color(9, 162, 104),
                getPositionX() + 2,
                getPositionY() + 2,
                getPositionX() + 2 + 1.5,
                getPositionY() + 2 + (bruhHeight - 4),
                1.5 / 2,
                6);
            ClipStack.globalInstance.popWindow();
        }
        if (cfd.progress != 0) {
            float pp = (float) Transitions.easeOutExpo(cfd.progress);
            pp = Renderer.transformColor(pp);
            Renderer.setupRender();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            float arrowDim = 3;
            stack.push();
            stack.translate(getPositionX() + getWidth() - arrowDim - 2, getPositionY() + bruhHeight / 2d + arrowDim / 2d, 0);
            Matrix4f m = stack.peek().getPositionMatrix();

            bufferBuilder.vertex(m, -arrowDim, -arrowDim, 0).color(1f, 1f, 1f, pp).next();
            bufferBuilder.vertex(m, 0, 0, 0).color(1f, 1f, 1f, pp).next();
            bufferBuilder.vertex(m, arrowDim, -arrowDim, 0).color(1f, 1f, 1f, pp).next();

            BufferRenderer.drawWithShader(bufferBuilder.end());
            Renderer.endRender();
            stack.pop();
        }
        FontRenderers.getRenderer()
            .drawString(stack,
                module.getName(),
                (float) getPositionX() + 6,
                (float) (getPositionY() + bruhHeight / 2d - FontRenderers.getRenderer().getFontHeight() / 2d),
                1f,
                1f,
                1f,
                module.isDisabled() ? 0.4f : 1f);
        this.cfd.setPositionX(getPositionX());
        this.cfd.setPositionY(getPositionY() + actualHeight + margin);
        if (this.cfd.progress != 0) {
            this.cfd.render(stack, x, y);
        }
    }

    @Override
    public double getHeight() {
        double cfdAnimProg = Transitions.easeOutExpo(this.cfd.progress);
        double cfdHeight = this.cfd.getHeight();
        return actualHeight + margin * cfdAnimProg + cfdHeight;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (new Rectangle(getPositionX(), getPositionY(), getPositionX() + getWidth(), getPositionY() + actualHeight).contains(x, y)) {
            if (button == 0) {
                module.toggle();
            } else if (button == 1) {
                this.cfd.expanded = !this.cfd.expanded;
            }
            return true;
        }
        return cfd.expanded && cfd.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return cfd.expanded && cfd.mouseReleased(x, y, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
        return cfd.expanded && cfd.mouseDragged(x, y, xDelta, yDelta, button);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        return cfd.expanded && cfd.charTyped(c, mods);
    }

    @Override
    public boolean keyPressed(int keyCode, int mods) {
        return cfd.expanded && cfd.keyPressed(keyCode, mods);
    }

    @Override
    public boolean keyReleased(int keyCode, int mods) {
        return cfd.expanded && cfd.keyReleased(keyCode, mods);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        return cfd.expanded && cfd.mouseScrolled(x, y, amount);
    }
}
