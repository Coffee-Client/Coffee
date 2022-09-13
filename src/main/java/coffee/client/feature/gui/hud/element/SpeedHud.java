/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.feature.gui.hud.element;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.theme.ThemeManager;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.render.Hud;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Timer;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SpeedHud extends HudElement {
    final List<Double> speedSaved = new ArrayList<>();
    final Timer update = new Timer();

    public SpeedHud() {
        super("Speed", CoffeeMain.client.getWindow().getScaledWidth() / 2d - 160 / 2d, CoffeeMain.client.getWindow().getScaledHeight() - 40 - 64, 160, 64);
    }

    @Override
    public void renderIntern(MatrixStack stack) {
        if (ModuleRegistry.getByClass(Hud.class).speed.getValue()) {
            double size = speedSaved.size();
            double incrX = width / (size - 1);
            double x = incrX;

            Comparator<Double> dc = Comparator.comparingDouble(value -> value);
            List<Double> speeds = new ArrayList<>(speedSaved);
            speeds = speeds.stream().filter(Objects::nonNull).toList();

            double max = Math.max(0.7, speeds.stream().max(dc).orElse(1d));
            double min = 0;

            double previous;
            if (speeds.size() > 0) {
                previous = height - ((speeds.get(0) - min) / max) * height;
            } else {
                previous = 0;
            }
            for (int i = 1; i < speeds.size(); i++) {
                double ppr = Math.sin(Math.toRadians(((double) i / speeds.size() + (System.currentTimeMillis() % 3000) / -3000d) * 360 * 3)) + 1;
                ppr /= 2d;
                double aDouble = speeds.get(i);
                double prog = ((aDouble - min) / max);
                double y = height - prog * height;

                Renderer.R2D.renderLine(stack,
                    Renderer.Util.lerp(ThemeManager.getMainTheme().getActive(), ThemeManager.getMainTheme().getAccent(), ppr),
                    x - incrX,
                    previous,
                    x,
                    y);

                x += incrX;
                previous = y;
            }
        }
    }

    @Override
    public void fastTick() {
        if (update.hasExpired(50)) { // update when velocity gets updated
            double speedCombined = CoffeeMain.client.player.getVelocity().length();
            double last = speedSaved.isEmpty() ? speedCombined : speedSaved.get(speedSaved.size() - 1);
            speedSaved.add((speedCombined + last) / 2d);
            while (speedSaved.size() > 50) {
                speedSaved.remove(0);
            }
            update.reset();
        }
        super.fastTick();
    }
}
