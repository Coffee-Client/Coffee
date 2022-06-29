/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.clickgui.element.impl;

import coffee.client.feature.gui.clickgui.ClickGUI;
import coffee.client.feature.gui.clickgui.element.Element;
import coffee.client.feature.gui.clickgui.theme.Theme;
import coffee.client.feature.gui.clickgui.theme.ThemeManager;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.ClipStack;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.render.Scroller;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class CategoryDisplay extends Element {
    static final FontAdapter cfr = FontRenderers.getCustomSize(20);
    @Getter
    final List<ModuleDisplay> md = new ArrayList<>();
    @Getter
    final ModuleType mt;
    final Scroller scroller = new Scroller(0);
    boolean selected = false;
    @Getter
    @Setter
    boolean open = true;
    double openAnim = 1;

    public CategoryDisplay(double x, double y, ModuleType mt) {
        super(x, y, 100, 500);
        this.mt = mt;
        for (Module module : ModuleRegistry.getModules()) {
            if (module.getModuleType() == mt) {
                ModuleDisplay md1 = new ModuleDisplay(0, 0, module);
                md.add(md1);
            }
        }
    }

    @Override
    public boolean clicked(double x, double y, int button) {
        double r = 5;
        if (x >= this.x && x < this.x + this.width && y >= this.y && y < this.y + headerHeight()) {
            if (button == 0) {
                selected = true;
                return true;
            } else if (button == 1) {
                open = !open;
            }
        } else if (x >= this.x && x < this.x + width && y >= this.y + headerHeight() && y < this.y + this.height - r) {
            for (ModuleDisplay moduleDisplay : getModules()) {
                if (moduleDisplay.clicked(x, y - scroller.getScroll(), button)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean dragged(double x, double y, double deltaX, double deltaY, int button) {
        if (selected) {
            this.x += deltaX;
            this.y += deltaY;
            return true;
        } else {
            for (ModuleDisplay moduleDisplay : getModules()) {
                if (moduleDisplay.dragged(x, y - scroller.getScroll(), deltaX, deltaY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean released() {
        selected = false;
        for (ModuleDisplay moduleDisplay : getModules()) {
            moduleDisplay.released();
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keycode, int modifiers) {
        for (ModuleDisplay moduleDisplay : getModules()) {
            if (moduleDisplay.keyPressed(keycode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    List<ModuleDisplay> getModules() {
        return md.stream().filter(moduleDisplay -> {
            char[] charsToSearchFor = ClickGUI.instance().searchTerm.toLowerCase().toCharArray();
            for (char c : charsToSearchFor) {
                if (!moduleDisplay.module.getName().toLowerCase().contains(String.valueOf(c))) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean scroll(double mouseX, double mouseY, double amount) {
        // not needed for now
        if (amount == 0 || inBounds(mouseX, mouseY)) {
            //            scroll += amount * 10;
            double contentHeight = getModules().stream().map(ModuleDisplay::getHeight).reduce(Double::sum).orElse(0d);
            double viewerHeight = Math.min(contentHeight, 350);
            double elScroll = contentHeight - viewerHeight;
            scroller.setBounds(0, elScroll);
            scroller.scroll(amount);
            return true;
        }
        return super.scroll(mouseX, mouseY, amount);
    }

    float headerHeight() {
        float padding = 3;
        return padding + cfr.getFontHeight() + padding;
    }

    @Override
    public void render(MatrixStack matrices, double mouseX, double mouseY, double scrollBeingUsed) {
        double mouseX1 = mouseX;
        double mouseY1 = mouseY;

        Theme theme = ThemeManager.getMainTheme();
        double openAnim = this.openAnim < 0.5 ? (1 - sqrt(1 - pow(2 * this.openAnim, 2))) / 2 : (sqrt(1 - pow(-2 * this.openAnim + 2, 2)) + 1) / 2;
        //        Renderer.R2D.fill(matrices, theme.getHeader(), x, y, x + width, y + headerHeight());
        double r = 3;
        double hheight = headerHeight();
        double texPad = 4;
        double texDim = hheight - texPad * 2;

        double modHeight = getModules().stream().map(ModuleDisplay::getHeight).reduce(Double::sum).orElse(0d);
        double modHeightUnclamped = modHeight;
        modHeight = Math.min(modHeight, 350);
        modHeight = Math.min(modHeight, modHeight * openAnim); // looks weird but works

        this.height = headerHeight() + modHeight; // pre calc height

        if (modHeight != 0) {
            height += r * openAnim;
        }
        scroll(mouseX1, mouseY1, 0);
        Renderer.R2D.renderRoundedQuad(matrices, theme.getHeader(), x, y, x + width, y + this.height, r, 10);


        RenderSystem.setShaderTexture(0, mt.getTex());
        Renderer.R2D.renderTexture(matrices, x + texPad, y + texPad, texDim, texDim, 0, 0, texDim, texDim, texDim, texDim);
        //        cfr.drawCenteredString(matrices,mt.getName(),x+texPad+texDim+texPad,y+headerHeight()/2d-cfr.getFontHeight()/2d,0xFFFFFF);
        cfr.drawCenteredString(matrices, mt.getName(), x + width / 2d, y + headerHeight() / 2d - cfr.getFontHeight() / 2d, 0xFFFFFF);
        double ct = 1;
        double cw = 6;
        matrices.push();
        matrices.translate(x + width - texPad - cw, y + headerHeight() / 2d - ct / 2d, 0);
        matrices.push();
        matrices.multiply(new Quaternion(0f, 0f, (float) (1 - openAnim) * 90f, true));
        Renderer.R2D.renderQuad(matrices, Color.WHITE, -cw / 2d, -ct / 2d, cw / 2d, ct / 2d);
        matrices.pop();
        matrices.multiply(new Quaternion(0f, 0f, (float) (1 - openAnim) * 180f, true));
        Renderer.R2D.renderQuad(matrices, Color.WHITE, -cw / 2d, -ct / 2d, cw / 2d, ct / 2d);
        matrices.pop();
        if (openAnim != 0) {
            // rounding the height in the final param makes it more smooth, otherwise scissor will do something with the start y and it will rattle like shit
            ClipStack.globalInstance.addWindow(matrices,
                    new Rectangle(x, y + headerHeight(), x + width, y + Math.round(this.height) - (modHeight != 0 ? r : 0))
            );
            double y = headerHeight();
            matrices.push();
            matrices.translate(0, scroller.getScroll(), 0);
            if (!(mouseX1 >= x && mouseX1 < x + width && mouseY1 >= this.y + headerHeight() && mouseY1 < this.y + this.height - (modHeight != 0 ? r : 0))) {
                mouseX1 = -1000;
                mouseY1 = -1000;
            }
            for (ModuleDisplay moduleDisplay : getModules()) {
                moduleDisplay.setX(this.x);
                moduleDisplay.setY(this.y + y);
                if (!(moduleDisplay.getY() + scroller.getScroll() > this.y + height || moduleDisplay.getY() + moduleDisplay.getHeight() + scroller.getScroll() < this.y + headerHeight())) {
                    moduleDisplay.render(matrices, mouseX1, mouseY1 - scroller.getScroll(), scrollBeingUsed);
                }
                y += moduleDisplay.getHeight();
            }
            matrices.pop();

            if (modHeightUnclamped > 350) {
                double elScroll = modHeightUnclamped - modHeight;
                double scrollIndex = (scroller.getScroll() * -1) / elScroll;

                double ratio = modHeight / modHeightUnclamped;
                double scrollerHeight = ratio * modHeight;
                double sbW = 2;
                Renderer.R2D.renderQuad(matrices,
                        new Color(20, 20, 20, 150),
                        x + width - sbW,
                        this.y + headerHeight(),
                        x + width,
                        this.y + headerHeight() + modHeight
                );
                double scrollerStartY = MathHelper.lerp(scrollIndex, this.y + headerHeight(), this.y + headerHeight() + modHeight - scrollerHeight);
                Renderer.R2D.renderRoundedQuad(matrices,
                        new Color(40, 40, 40, 200),
                        x + width - sbW,
                        scrollerStartY,
                        x + width,
                        scrollerStartY + scrollerHeight,
                        sbW / 2d,
                        20
                );
            }
            ClipStack.globalInstance.popWindow();
        }
        //        FontRenderers.getRenderer().drawCenteredString(matrices, getModules().size() + " modules", this.x + this.width / 2d, this.y + this.height - 1 - FontRenderers.getRenderer().getMarginHeight(), 0xFFFFFF);
    }

    @Override
    public void tickAnim() {
        scroller.tick();
        double oaDelta = 0.02;
        if (!open) {
            oaDelta *= -1;
        }
        openAnim += oaDelta;
        openAnim = MathHelper.clamp(openAnim, 0, 1);
        for (ModuleDisplay moduleDisplay : getModules()) {
            moduleDisplay.tickAnim();
        }
        //        smoothScroll = Transitions.transition(smoothScroll, scroll, 7, 0);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        for (ModuleDisplay moduleDisplay : getModules()) {
            if (moduleDisplay.charTyped(c, mods)) {
                return true;
            }
        }
        return false;
    }
}
