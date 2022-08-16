/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.annotation.Setting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.ConfigContainer;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.events.base.NonCancellableEvent;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import lombok.Data;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Waypoints extends Module {
    public static List<Waypoint> waypoints = new ArrayList<>();
    public static ConfigContainer conf = new ConfigContainer(new File(CoffeeMain.BASE, "waypoints.coffee"), "");
    List<Runnable> real = new ArrayList<>();

    @Setting(name = "Tracers", description = "Shows tracers pointing to the waypoints")
    boolean tracers = true;

    public Waypoints() {
        super("Waypoints", "Allows you to save locations on servers", ModuleType.RENDER);
        conf.reload();
        Config config1 = conf.get(Config.class);
        waypoints = (config1 == null || config1.getWaypoints() == null) ? new ArrayList<>() : new ArrayList<>(config1.getWaypoints());
    }

    @EventListener(EventType.CONFIG_SAVE)
    void onStop(NonCancellableEvent ev) {
        Config c = new Config();
        c.setWaypoints(waypoints);
        conf.set(c);
        conf.save();
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {
        System.out.println(waypoints);
    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        double fadeDistancePlayer = 10;
        waypoints.stream().sorted(Comparator.comparingDouble(value -> -value.position.distanceTo(CoffeeMain.client.gameRenderer.getCamera().getPos()))).forEachOrdered(waypoint -> {
            if (tracers) {
                Renderer.R3D.renderLine(matrices, waypoint.color, Renderer.R3D.getCrosshairVector(), waypoint.position);
            }
            double distancePlayer = waypoint.position.distanceTo(Utils.getInterpolatedEntityPosition(client.player));
            double subbed1 = (fadeDistancePlayer - distancePlayer) / fadeDistancePlayer;
            subbed1 = MathHelper.clamp(subbed1, 0, 1);
            subbed1 = 1 - subbed1;
            Renderer.R3D.renderFilled(matrices, Renderer.Util.modify(waypoint.color, -1, -1, -1, (int) (subbed1 * 255)),
                    new Vec3d(waypoint.position.x - .2, CoffeeMain.client.world.getBottomY(), waypoint.position.z - .2), new Vec3d(.4, CoffeeMain.client.world.getHeight(), .4));
            Vec3d screenSpaceCoordinate = Renderer.R2D.getScreenSpaceCoordinate(waypoint.position, matrices);
            if (Renderer.R2D.isOnScreen(screenSpaceCoordinate)) {
                real.add(() -> {
                    String t = waypoint.getName();
                    float width = FontRenderers.getRenderer().getStringWidth(t) + 4;
                    Renderer.R2D.renderRoundedQuad(Renderer.R3D.getEmptyMatrixStack(), new Color(20, 20, 20, 255), screenSpaceCoordinate.x - width / 2d,
                            screenSpaceCoordinate.y - FontRenderers.getRenderer().getFontHeight() / 2d - 2, screenSpaceCoordinate.x + width / 2d,
                            screenSpaceCoordinate.y + FontRenderers.getRenderer().getFontHeight() / 2d + 2, 5, 10);
                    FontRenderers.getRenderer().drawCenteredString(Renderer.R3D.getEmptyMatrixStack(), t, screenSpaceCoordinate.x, screenSpaceCoordinate.y - FontRenderers.getRenderer().getFontHeight() / 2d, 1f, 1f, 1f, 1f);
                });
            }
        });
    }

    @Override
    public void onHudRender() {
        for (Runnable runnable : real) {
            runnable.run();
        }
        real.clear();
    }

    @Data
    public static class Waypoint {
        String name;
        Vec3d position;
        Color color;
    }

    @Data
    public static class Config {
        List<Waypoint> waypoints;
    }
}
