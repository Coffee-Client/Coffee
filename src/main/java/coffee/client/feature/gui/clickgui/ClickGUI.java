/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.clickgui;

import coffee.client.feature.gui.clickgui.element.CategoryDisplay;
import coffee.client.feature.gui.clickgui.element.ConfigsDisplay;
import coffee.client.feature.gui.element.Element;
import coffee.client.feature.gui.screen.base.AAScreen;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.manager.ShaderManager;
import coffee.client.helper.render.AlphaOverride;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import coffee.client.helper.util.Utils;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClickGUI extends AAScreen {
    private static ClickGUI instance;
    boolean initialized = false;
    boolean closing = false;
    double progress = 0;

    @Getter
    String searchTerm = "", oldSearchTerm = "";

    double searchAnim = 0;

    double tooltipX, tooltipY;
    String tooltipContent;

    public static void reInit() {
        if (instance != null) {
            instance.initWidgets();
        }
    }

    public static ClickGUI instance() {
        if (instance == null) {
            instance = new ClickGUI();
        }
        return instance;
    }

    public BitSet matchesSearchTerm(String content) {
        return Utils.searchMatches(content, searchTerm);
    }

    public boolean isSearchActive() {
        return !searchTerm.isEmpty();
    }

    public void setTooltip(String content) {
        tooltipX = Utils.Mouse.getMouseX();
        tooltipY = Utils.Mouse.getMouseY() + 10;
        tooltipContent = content;
    }

    @Override
    protected void init() {
        initInternal(); // do not clear the elements on the way in
    }

    @Override
    protected void initInternal() {
        closing = false;
        if (initialized) {
            return;
        }
        initialized = true;
        initWidgets();
    }

    public void initWidgets() {
        clearWidgets();
        double x = 5;
        double lineWidth = 0;
        double y = 5;
        for (ModuleType value : ModuleType.values()) {
            if (value == ModuleType.HIDDEN) {
                continue;
            }
            CategoryDisplay gd = new CategoryDisplay(value, 0, 0, 100);
            if (y + gd.getHeight() > height - 5) {
                y = 5;
                x += lineWidth + 5;
                lineWidth = 0;
            }
            gd.setPositionX(x);
            gd.setPositionY(y);
            y += gd.getHeight() + 5;
            lineWidth = Math.max(lineWidth, gd.getWidth());
            addChild(gd);
        }
        ConfigsDisplay configsDisplay = new ConfigsDisplay(x, y, 100);
        if (y + configsDisplay.getHeight() > height - 5) {
            y = 5;
            x += lineWidth + 5;
        }
        configsDisplay.setPositionX(x);
        configsDisplay.setPositionY(y);
        addChild(configsDisplay);
    }

    @Override
    public void close() {
        closing = true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onFastTick() {
        double delta = 0.03;
        if (closing) {
            delta *= -1;
        }
        progress += delta;
        progress = MathHelper.clamp(progress, 0, 1);

        double d = 0.04;
        if (searchTerm.isEmpty()) {
            d *= -1;
        }
        searchAnim += d;
        searchAnim = MathHelper.clamp(searchAnim, 0, 1);

        super.onFastTick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean b = iterateOverChildren(element -> element.keyPressed(keyCode, modifiers));
        if (b) {
            return true;
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> {
                if (!searchTerm.isEmpty()) {
                    searchTerm = "";
                } else {
                    closing = true;
                }
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (!searchTerm.isEmpty()) {
                    searchTerm = searchTerm.substring(0, searchTerm.length() - 1);
                    if (!searchTerm.isEmpty()) {
                        oldSearchTerm = searchTerm;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        boolean b = super.charTyped(chr, modifiers);
        if (b) {
            return true;
        }
        if (searchTerm.isEmpty()) {
            oldSearchTerm = "";
        }
        searchTerm += chr;
        oldSearchTerm += chr;
        return true;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (closing && progress == 0) {
            super.close();
        }
        double interpolated = Transitions.easeOutExpo(progress);
        ShaderManager.BLUR.getEffect().setUniformValue("progress", (float) interpolated);
        ShaderManager.BLUR.render(delta);
        matrices.push();
        double maxScale = 1.02;
        double minScale = 1;
        double scale = MathHelper.lerp(interpolated, maxScale, minScale);
        double inverse = 1 - scale;
        matrices.translate(inverse * width / 2, inverse * height / 2, 0);
        matrices.scale((float) scale, (float) scale, 1);
        AlphaOverride.pushAlphaMul((float) interpolated);

        super.render(matrices, mouseX, mouseY, delta);
        AlphaOverride.popAlphaMul();
        matrices.pop();

    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        List<Element> elcpy = new ArrayList<>(getElements());
        Collections.reverse(elcpy);
        for (Element element : elcpy) {
            element.render(stack, mouseX, mouseY);
        }
        if (searchAnim != 0) {
            stack.push();
            double pad = 2;
            double hei = pad + FontRenderers.getRenderer().getFontHeight() + pad;
            stack.translate(0, (hei + pad) * (1 - Transitions.easeOutExpo(searchAnim)), 0);
            double textWid = FontRenderers.getRenderer().getStringWidth(oldSearchTerm);
            Renderer.R2D.renderRoundedQuad(stack,
                new Color(20, 20, 20),
                width - pad - pad - textWid - pad,
                height - pad - hei,
                width - pad,
                height - pad,
                5,
                2,
                2,
                2,
                10);
            FontRenderers.getRenderer()
                .drawString(stack, oldSearchTerm, width - pad - pad - textWid, height - pad - pad - FontRenderers.getRenderer().getFontHeight(), 0xFFFFFF);
            stack.pop();
        }
        if (tooltipContent != null) {
            String[] split = tooltipContent.split("\n");
            double height = FontRenderers.getRenderer().getFontHeight() * split.length + 2;
            double width = Arrays.stream(split).map(s -> FontRenderers.getRenderer().getStringWidth(s)).max(Comparator.comparingDouble(value -> value)).orElse(0f) + 4f;
            double tooltipX = Math.min(this.tooltipX, this.width - 4 - width);

            Renderer.R2D.renderRoundedQuadWithShadow(stack, new Color(30, 30, 30), tooltipX, tooltipY, tooltipX + width, tooltipY + height, 2, 6);
            double y = 0;
            for (String s : split) {
                FontRenderers.getRenderer().drawString(stack, s, tooltipX + 2, tooltipY + 1 + y, 0xFFFFFF);
                y += FontRenderers.getRenderer().getFontHeight();
            }

            tooltipContent = null;
        }
    }
}
