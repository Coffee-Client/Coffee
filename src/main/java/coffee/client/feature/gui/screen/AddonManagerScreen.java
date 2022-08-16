/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.screen;

import coffee.client.feature.addon.Addon;
import coffee.client.feature.addon.AddonManager;
import coffee.client.feature.gui.FastTickable;
import coffee.client.feature.gui.clickgui.ClickGUI;
import coffee.client.feature.gui.screen.base.ClientScreen;
import coffee.client.feature.gui.widget.RoundButton;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.font.adapter.impl.QuickFontAdapter;
import coffee.client.helper.render.ClipStack;
import coffee.client.helper.render.GameTexture;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.render.Scroller;
import coffee.client.helper.util.Timer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.opengl.GL40C;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AddonManagerScreen extends ClientScreen implements FastTickable {
    final Timer discoverTimer = new Timer();
    final Scroller scroller = new Scroller(0);
    final double WIDGET_WIDTH = 600;
    final double WIDGET_HEIGHT = 300;
    final List<AddonViewer> viewerList = new ArrayList<>();

    @Override
    public void onFastTick() {
        scroller.tick();
        for (AddonViewer addonViewer : viewerList) {
            addonViewer.onFastTick();
        }
        if (discoverTimer.hasExpired(5000)) {
            discoverTimer.reset();
            AddonManager.INSTANCE.discoverNewAddons();
            for (Addon loadedAddon : AddonManager.INSTANCE.getLoadedAddons()) {
                if (viewerList.stream().noneMatch(addonViewer -> addonViewer.addon == loadedAddon)) {
                    viewerList.add(new AddonViewer(loadedAddon, WIDGET_WIDTH - 10));
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (new Rectangle(width / 2d - WIDGET_WIDTH / 2d, height / 2d - WIDGET_HEIGHT / 2d, width / 2d + WIDGET_WIDTH / 2d, height / 2d + WIDGET_HEIGHT / 2d).contains(mouseX, mouseY)) {
            double contentHeight = viewerList.stream().map(addonViewer -> addonViewer.getHeight() + 5).reduce(Double::sum).orElse(0d) + 5;
            double entitledScroll = Math.max(0, contentHeight - WIDGET_HEIGHT);
            scroller.setBounds(0, entitledScroll);
            scroller.scroll(amount);

        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void filesDragged(List<Path> paths) {
        for (Path path : paths) {
            File file = path.toFile();
            if (file.getName().endsWith(".jar")) {
                AddonManager.INSTANCE.loadFromFile(file);
            }
        }
    }

    @Override
    protected void init() {
        reInitViewers();
        RoundButton openFolder = new RoundButton(RoundButton.STANDARD, 5, 5, 100, 20, "Open folder", () -> Util.getOperatingSystem().open(AddonManager.ADDON_DIRECTORY));
        this.addDrawableChild(openFolder);
    }

    void reInitViewers() {
        viewerList.clear();
        for (Addon loadedAddon : AddonManager.INSTANCE.getLoadedAddons()) {
            viewerList.add(new AddonViewer(loadedAddon, WIDGET_WIDTH - 10));
        }
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        renderBackground(stack);
        Renderer.R2D.renderRoundedQuad(stack, new Color(20, 20, 20), width / 2d - WIDGET_WIDTH / 2d, height / 2d - WIDGET_HEIGHT / 2d, width / 2d + WIDGET_WIDTH / 2d, height / 2d + WIDGET_HEIGHT / 2d,
                5, 20);
        ClipStack.globalInstance.addWindow(stack, new Rectangle(width / 2d - WIDGET_WIDTH / 2d, height / 2d - WIDGET_HEIGHT / 2d, width / 2d + WIDGET_WIDTH / 2d, height / 2d + WIDGET_HEIGHT / 2d));
        double yOffset = 0;
        double xRoot = width / 2d - WIDGET_WIDTH / 2d + 5;
        double yRoot = height / 2d - WIDGET_HEIGHT / 2d + 5;
        if (viewerList.isEmpty()) {
            QuickFontAdapter customSize = FontRenderers.getCustomSize(40);
            QuickFontAdapter customSize1 = FontRenderers.getCustomSize(30);
            customSize.drawCenteredString(stack, "No addons", width / 2d, height / 2d - customSize.getFontHeight(), 0xAAAAAA);
            customSize1.drawCenteredString(stack, "Drag some in to load them", width / 2d, height / 2d, 0xAAAAAA);
        }
        for (AddonViewer addonViewer : new ArrayList<>(viewerList)) {
            addonViewer.render(stack, xRoot, yRoot + yOffset + scroller.getScroll(), mouseX, mouseY);
            yOffset += addonViewer.getHeight() + 5;
        }
        ClipStack.globalInstance.popWindow();
        super.renderInternal(stack, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (AddonViewer addonViewer : new ArrayList<>(viewerList)) {
            addonViewer.clicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    class AddonViewer implements FastTickable {
        static final double iconDimensions = 64;
        static final double padding = 5;
        final Addon addon;
        final double width;
        final RoundButton reload;
        RoundButton disable;

        public AddonViewer(Addon addon, double width) {
            this.addon = addon;
            this.width = width;
            disable = new RoundButton(RoundButton.STANDARD, 0, 0, 60, 20, addon.isEnabled() ? "Disable" : "Enable", () -> {
                if (addon.isEnabled()) {
                    AddonManager.INSTANCE.disableAddon(addon);
                } else {
                    AddonManager.INSTANCE.enableAddon(addon);
                }
                disable.setText(addon.isEnabled() ? "Disable" : "Enable");
                ClickGUI.reInit();
            });
            reload = new RoundButton(RoundButton.STANDARD, 0, 0, 60, 20, "Reload", () -> {
                AddonManager.INSTANCE.reload(addon);
                reInitViewers();
                ClickGUI.reInit();
            });
        }

        public void render(MatrixStack stack, double x, double y, int mouseX, int mouseY) {

            Color background = new Color(25, 25, 25);
            Renderer.R2D.renderRoundedQuad(stack, background, x, y, x + width, y + getHeight(), 5, 20);
            RenderSystem.enableBlend();
            RenderSystem.colorMask(false, false, false, true);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
            RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            Renderer.R2D.renderRoundedQuadInternal(stack.peek().getPositionMatrix(), background.getRed() / 255f, background.getGreen() / 255f, background.getBlue() / 255f, 1, x + padding, y + padding,
                    x + padding + iconDimensions, y + padding + iconDimensions, 6, 10);

            RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
            Identifier icon = addon.getIcon();
            if (icon == null) {
                icon = GameTexture.ICONS_ADDON_PROVIDED.getWhere();
            }
            RenderSystem.setShaderTexture(0, icon);
            if (!addon.isEnabled()) {
                RenderSystem.setShaderColor(0.6f, 0.6f, 0.6f, 1f);
            }
            Renderer.R2D.renderTexture(stack, x + padding, y + padding, iconDimensions, iconDimensions, 0, 0, iconDimensions, iconDimensions, iconDimensions, iconDimensions);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            FontAdapter title = FontRenderers.getCustomSize(30);
            FontAdapter normal = FontRenderers.getRenderer();
            double entireHeight = title.getFontHeight() + normal.getFontHeight() * 2d;
            if (addon.isEnabled()) {
                title.drawString(stack, addon.name, (float) (x + padding + iconDimensions + padding), (float) (y + getHeight() / 2d - entireHeight / 2d), 0xFFFFFF);
            } else {
                title.drawString(stack, addon.name, (float) (x + padding + iconDimensions + padding), (float) (y + getHeight() / 2d - entireHeight / 2d), 0.6f, 0.6f, 0.6f, 1f);
            }
            normal.drawString(stack, addon.description, (float) (x + padding + iconDimensions + padding), (float) (y + getHeight() / 2d - entireHeight / 2d + title.getFontHeight()), 0.6f, 0.6f, 0.6f,
                    1f);
            normal.drawString(stack, "Developer(s): " + String.join(", ", addon.developers), (float) (x + padding + iconDimensions + padding),
                    (float) (y + getHeight() / 2d - entireHeight / 2d + title.getFontHeight() + normal.getFontHeight()), 0.6f, 0.6f, 0.6f, 1f);

            double buttonRowHeight = disable.getHeight() + padding + reload.getHeight();

            disable.setX(x + width - disable.getWidth() - padding);
            disable.setY(y + getHeight() / 2d - buttonRowHeight / 2d);
            disable.render(stack, mouseX, mouseY, 0);
            reload.setX(x + width - disable.getWidth() - padding);
            reload.setY(y + getHeight() / 2d - buttonRowHeight / 2d + disable.getHeight() + padding);
            reload.render(stack, mouseX, mouseY, 0);
        }

        public void clicked(double mouseX, double mouseY, int button) {
            disable.mouseClicked(mouseX, mouseY, button);
            reload.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void onFastTick() {
            disable.onFastTick();
            reload.onFastTick();
        }

        public double getHeight() {
            return iconDimensions + padding * 2;
        }
    }
}
