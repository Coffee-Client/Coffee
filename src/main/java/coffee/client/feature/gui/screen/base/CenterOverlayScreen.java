/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.screen.base;

import coffee.client.feature.gui.FastTickable;
import coffee.client.feature.gui.element.Element;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.Renderer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.Comparator;

@RequiredArgsConstructor
public class CenterOverlayScreen extends AAScreen {
    final Screen parent;
    @NonNull String title;
    @NonNull String description;

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (parent != null) {
            parent.render(matrices, mouseX, mouseY, delta);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void initInternal() {
        if (parent != null) {
            parent.init(client, width, height);
        }
        super.initInternal();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        if (parent != null) {
            parent.resize(client, width, height);
        }
        super.resize(client, width, height);
    }

    @Override
    public void onFastTick() {
        if (parent instanceof FastTickable f) {
            f.onFastTick();
        }
        super.onFastTick();
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        double minX = getElements().stream().map(Element::getPositionX).min(Comparator.comparingDouble(value -> value)).orElse(0d);
        double maxX = getElements().stream().map(element -> element.getPositionX() + element.getWidth()).max(Comparator.comparingDouble(value -> value)).orElse(0d);
        double minY = getElements().stream().map(Element::getPositionY).min(Comparator.comparingDouble(value -> value)).orElse(0d);
        double maxY = getElements().stream().map(element -> element.getPositionY() + element.getHeight()).max(Comparator.comparingDouble(value -> value)).orElse(0d);
        double centerX = width / 2d;
        double centerY = height / 2d;
        double elementWidth = (maxX - minX);
        double elementHeight = (maxY - minY);
        double currentCenterX = minX + elementWidth / 2d;
        double currentCenterY = minY + elementHeight / 2d;
        double offsetX = centerX - currentCenterX;
        double offsetY = centerY - currentCenterY;
        for (Element element : getElements()) {
            element.setPositionX(element.getPositionX() + offsetX);
            element.setPositionY(element.getPositionY() + offsetY);
        }
        double padding = 5;
        Renderer.R2D.renderQuad(stack, new Color(0, 0, 0, 150), 0, 0, width, height);
        FontAdapter fa = FontRenderers.getCustomSize(40);
        FontAdapter normal = FontRenderers.getRenderer();
        fa.drawString(stack, title, 5, 5, 0xFFFFFF);
        normal.drawString(stack, description, 5, 5 + fa.getFontHeight(), 0xBBBBBB);
        Renderer.R2D.renderRoundedQuadWithShadow(stack, new Color(20, 20, 20), centerX - elementWidth / 2d - padding, centerY - elementHeight / 2d - padding, centerX + elementWidth / 2d + padding, centerY + elementHeight / 2d + padding, 5, 20);
        super.renderInternal(stack, mouseX, mouseY, delta);
    }
}
