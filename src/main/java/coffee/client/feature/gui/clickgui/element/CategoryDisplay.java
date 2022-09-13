/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.clickgui.element;

import coffee.client.feature.gui.clickgui.ClickGUI;
import coffee.client.feature.gui.element.Element;
import coffee.client.feature.gui.element.impl.FlexLayoutElement;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.render.textures.Texture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class CategoryDisplay extends Element {
    static final double maxHeight = 250;
    final ModuleType type;
    final List<Module> modules = new ArrayList<>();
    final FlexLayoutElement layout;
    final FontAdapter titleRenderer = FontRenderers.getRenderer();


    boolean held = false;

    public CategoryDisplay(ModuleType type, double x, double y, double width) {
        super(x, y, width, 0);
        this.type = type;
        for (Module module : ModuleRegistry.getModules()) {
            if (module.getModuleType() == type) {
                this.modules.add(module);
            }
        }
        List<ModuleDisplay> displays = new ArrayList<>();
        for (Module module : modules) {
            displays.add(new ModuleDisplay(module, 0, 0, width - 4, this));
        }
        layout = new FlexLayoutElement(FlexLayoutElement.LayoutDirection.DOWN, x + 2, y, width - 4, maxHeight, 2, displays.toArray(ModuleDisplay[]::new));

        double nh = Math.min(layout.getActualHeight(), maxHeight);
        if (nh != 0) {
            nh += 2;
        }
        setHeight(nh);
    }

    @Override
    public void tickAnimations() {
        layout.tickAnimations();
    }

    double headerHeight() {
        return Math.round(titleRenderer.getFontHeight() + 5 * 2);
    }

    @Override
    public double getHeight() {
        return Math.round(headerHeight() + super.getHeight());
    }

    @Override
    public void render(MatrixStack stack, double mouseX, double mouseY) {
        double nh = Math.min(layout.getActualHeight(), maxHeight);
        if (nh != 0) {
            nh += 2;
        }
        setHeight(nh);
        layout.updateScroller();
        Renderer.R2D.renderRoundedQuadWithShadow(stack,
            new Color(20, 20, 20),
            getPositionX(),
            getPositionY(),
            getPositionX() + getWidth(),
            getPositionY() + getHeight(),
            3,
            10);
        double iconPad = 4;
        double iconDims = headerHeight() - iconPad * 2;
        RenderSystem.enableBlend();
        Texture.MODULE_TYPES.bindAndDraw(stack, getPositionX() + iconPad, getPositionY() + iconPad, iconDims, iconDims, type.getTex());

        titleRenderer.drawString(stack,
            type.getName(),
            (float) (getPositionX() + iconDims + iconPad * 2),
            (float) (getPositionY() + headerHeight() / 2d - Math.round(titleRenderer.getFontHeight()) / 2d),
            1f,
            1f,
            1f,
            1f);
        layout.setPositionX(getPositionX() + 2);
        layout.setPositionY(getPositionY() + headerHeight());
        layout.render(stack, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (new Rectangle(getPositionX(), getPositionY(), getPositionX() + getWidth(), getPositionY() + headerHeight()).contains(x, y)) {
            held = true;
            ClickGUI.instance().removeChild(this);
            ClickGUI.instance().addChild(0, this); // add to back of queue
            return true;
        }
        return layout.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (held) {
            held = false;
            return true;
        }
        return layout.mouseReleased(x, y, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, double xDelta, double yDelta, int button) {
        if (held) {
            setPositionX(getPositionX() + xDelta);
            setPositionY(getPositionY() + yDelta);
            return true;
        }
        return layout.mouseDragged(x, y, xDelta, yDelta, button);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        return layout.charTyped(c, mods);
    }

    @Override
    public boolean keyPressed(int keyCode, int mods) {
        return layout.keyPressed(keyCode, mods);
    }

    @Override
    public boolean keyReleased(int keyCode, int mods) {
        return layout.keyReleased(keyCode, mods);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        return layout.mouseScrolled(x, y, amount);
    }
}
