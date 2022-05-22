/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.screen;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.FastTickable;
import coffee.client.helper.Timer;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class StatsScreen extends ClientScreen implements FastTickable {
    static final List<Float> packetIn = Util.make(() -> {
        List<Float> f = new ArrayList<>();
        for (int i = 0; i < 100; i++)
            f.add(0f);
        return f;
    });
    static final List<Float> packetOut = Util.make(() -> {
        List<Float> f = new ArrayList<>();
        for (int i = 0; i < 100; i++)
            f.add(0f);
        return f;
    });
    final Timer packetUpdater = new Timer();

    @Override
    public void onFastTick() {
        if (packetUpdater.hasExpired(500)) {
            packetUpdater.reset();
            float in = CoffeeMain.client.getNetworkHandler().getConnection().getAveragePacketsReceived();
            packetIn.add(in);
            float out = CoffeeMain.client.getNetworkHandler().getConnection().getAveragePacketsSent();
            packetOut.add(out);
            while (packetIn.size() > 100) {
                packetIn.remove(0);
            }
            while (packetOut.size() > 100) packetOut.remove(0);
        }
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        renderBackground(stack);
        double contentWidth = width;

        List<Vec2f> bezPositions = new ArrayList<>();
        List<Float> pIn = new ArrayList<>(StatsScreen.packetIn);
        List<Float> pOut = new ArrayList<>(StatsScreen.packetOut);
        pIn.removeIf(Objects::isNull);
        float highest = Math.max(pIn.stream()
                .max(Comparator.comparingDouble(value -> (double) value))
                .orElse(0f), pOut.stream().max(Comparator.comparingDouble(value -> (double) value)).orElse(0f));
        double maxHeight = 300;
        float scaleFactor = (float) Math.min(1, maxHeight / highest);
        for (int i = 0; i < pIn.size(); i++) {
            double prog = (i) / (double) (pIn.size() - 3);
            float x = (float) (prog * contentWidth - ((System.currentTimeMillis() - packetUpdater.getLastReset()) / 500f * (1f / (pIn.size() - 1) * contentWidth)));
            float y = (float) height - pIn.get(i) * scaleFactor;
            Vec2f a = new Vec2f(x, y);
            bezPositions.add(a);
        }
        Renderer.R2D.renderBezierCurve(stack, bezPositions.toArray(new Vec2f[0]), 1f, 1f, 0f, 1f, 0.01f);
        bezPositions.clear();
        for (int i = 0; i < pOut.size(); i++) {
            double prog = (i) / (double) (pOut.size() - 3);
            float x = (float) (prog * contentWidth - ((System.currentTimeMillis() - packetUpdater.getLastReset()) / 500f * (1f / (pOut.size() - 1) * contentWidth)));
            float y = (float) height - pOut.get(i) * scaleFactor;
            Vec2f a = new Vec2f(x, y);
            bezPositions.add(a);
        }
        Renderer.R2D.renderBezierCurve(stack, bezPositions.toArray(new Vec2f[0]), 0f, 1f, 1f, 1f, 0.01f);

        super.renderInternal(stack, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
