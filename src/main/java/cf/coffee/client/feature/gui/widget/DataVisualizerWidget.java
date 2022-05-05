/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.gui.widget;

import cf.coffee.client.feature.gui.DoesMSAA;
import cf.coffee.client.feature.gui.FastTickable;
import cf.coffee.client.helper.font.FontRenderers;
import cf.coffee.client.helper.render.Renderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class DataVisualizerWidget implements Element, Drawable, Selectable, FastTickable, DoesMSAA {
    final List<Double> data = new ArrayList<>();
    final int maxSize;
    final double width;
    final double height;
    final double x;
    final double y;
    final double minDataHeight;
    final Color c;
    final boolean showScale;

    public DataVisualizerWidget(Color dataColor, boolean showScale, int maxSize, double minDataScale, double height, double width, double x, double y, double... existingData) {
        for (double existingDatum : existingData) {
            data.add(existingDatum);
        }
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.minDataHeight = minDataScale;
        this.maxSize = maxSize;
        this.showScale = showScale;
        this.c = dataColor;
        sizeArray();
    }

    void sizeArray() {
        while (data.size() > maxSize) data.remove(0);
    }

    public void addDataPoint(double d) {
        data.add(d);
        sizeArray();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        List<Double> dataCopy = new ArrayList<>(this.data); // thread safe
        dataCopy.removeIf(Objects::isNull);
        Comparator<Double> asIs = Comparator.comparingDouble(value -> value);
        double lowest = Math.floor(dataCopy.stream().min(asIs).orElse(0d));
        double highest = Math.ceil(dataCopy.stream().max(asIs).orElse(0d));
        int s = dataCopy.size() - 1;
        double maxW = 0;

        double contentWidth = width - maxW;
        for (int i = 1; i < dataCopy.size(); i++) {
            double progress = (double) i / s;
            double renderPointX = progress * contentWidth + maxW;
            double currentPointOfTotal = (dataCopy.get(i) - lowest) / (highest - lowest);
            double previousRenderPointX = ((double) (i - 1) / s) * contentWidth + maxW;
            double prevPointOfTotal = (dataCopy.get(i - 1) - lowest) / (highest - lowest);
            Renderer.R2D.renderLine(matrices, c, x + previousRenderPointX, MathHelper.lerp(prevPointOfTotal, y + height, y), x + renderPointX, MathHelper.lerp(currentPointOfTotal, y + height, y));
        }
        if (showScale) {
            String lowestStr = lowest + "";
            String highestStr = highest + "";
            FontRenderers.getRenderer().drawString(matrices, highestStr, x + 1, y, 0xFFFFFF);
            FontRenderers.getRenderer().drawString(matrices, lowestStr, x + 1, y + height - FontRenderers.getRenderer().getFontHeight(), 0xFFFFFF);
        }

    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public boolean isNarratable() {
        return false;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void onFastTick() {

    }
}
