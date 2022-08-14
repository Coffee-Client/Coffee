/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.ConfigContainer;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.events.base.NonCancellableEvent;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Renderer;
import lombok.Data;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Waypoints extends Module {
    public static List<Waypoint> waypoints = new ArrayList<>();
    public static ConfigContainer conf = new ConfigContainer(new File(CoffeeMain.BASE, "waypoints.coffee"), "");
    List<Runnable> real = new ArrayList<>();

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
        double fadeDistance = 20;
        double maxDist = 200;
        for (Waypoint waypoint : waypoints) {
            double distance = waypoint.position.distanceTo(client.gameRenderer.getCamera().getPos());
            double subbed = Math.max(maxDist - distance, 0);
            double scaled = MathHelper.clamp(subbed / fadeDistance, 0, 1);
            double opacity = scaled;
            Vec3d screenSpaceCoordinate = Renderer.R2D.getScreenSpaceCoordinate(waypoint.position, matrices);
            if (Renderer.R2D.isOnScreen(screenSpaceCoordinate)) {
                real.add(() -> {
                    String t = waypoint.getName();
                    float width = FontRenderers.getRenderer().getStringWidth(t) + 4;
                    Vec2f f = Renderer.R2D.renderTooltip(
                            Renderer.R3D.getEmptyMatrixStack(),
                            screenSpaceCoordinate.x,
                            screenSpaceCoordinate.y,
                            width,
                            FontRenderers.getRenderer().getFontHeight() + 2,
                            new Color(20, 20, 20),
                            false
                    );
                    FontRenderers.getRenderer().drawString(Renderer.R3D.getEmptyMatrixStack(), t, f.x + 2, f.y + 1, 1f, 1f, 1f, (float) opacity);
                });
            }
        }
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
