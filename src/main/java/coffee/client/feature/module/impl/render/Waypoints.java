/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.annotation.Setting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.Rotation;
import coffee.client.helper.config.ConfigContainer;
import coffee.client.helper.event.EventSystem;
import coffee.client.helper.event.impl.ConfigSaveEvent;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Rotations;
import coffee.client.helper.util.Utils;
import lombok.Data;
import me.x150.jmessenger.MessageSubscription;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Level;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Waypoints extends Module {
    static final List<Sightpoint> staticSightpoints = Util.make(new ArrayList<>(), (e) -> {
        e.add(new Sightpoint("0°", 0, Color.WHITE, true));
        e.add(new Sightpoint("90°", 90, Color.WHITE, true));
        e.add(new Sightpoint("180°", 180, Color.WHITE, true));
        e.add(new Sightpoint("270°", 270, Color.WHITE, true));
    });
    public static List<Waypoint> waypoints = new ArrayList<>();
    public static ConfigContainer conf = new ConfigContainer(new File(CoffeeMain.BASE, "waypoints.coffee"));
    List<Runnable> real = new ArrayList<>();
    @Setting(name = "Tracers", description = "Shows tracers pointing to the waypoints")
    boolean tracers = true;
    @Setting(name = "Show compass", description = "Shows a compass-like navigator on the top of the screen")
    boolean showCompass = true;

    public Waypoints() {
        super("Waypoints", "Allows you to save locations on servers", ModuleType.RENDER);
        conf.reload();
        Config config1 = conf.get(Config.class);
        waypoints = (config1 == null || config1.getWaypoints() == null) ? new ArrayList<>() : new ArrayList<>(config1.getWaypoints());

        EventSystem.manager.registerSubscribers(new Object() { // keep registered
            @MessageSubscription
            void onStop(ConfigSaveEvent ev) {
                CoffeeMain.log(Level.INFO, "Saving waypoints");
                Config c = new Config();
                c.setWaypoints(waypoints);
                conf.set(c);
                conf.save();
            }
        });
    }

    public static List<Sightpoint> getSightpoints() {
        CopyOnWriteArrayList<Sightpoint> sightpoints = new CopyOnWriteArrayList<>(staticSightpoints);
        for (Waypoint waypoint : waypoints) {
            Rotation pitchYaw = Rotations.getPitchYawFromOtherEntity(client.gameRenderer.getCamera().getPos(), waypoint.position);
            sightpoints.add(new Sightpoint(waypoint.name, pitchYaw.getYaw(), waypoint.color, false));
        }
        return sightpoints;
    }


    @Override
    public void tick() {

    }

    @Override
    public void enable() {

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
        waypoints.stream()
            .sorted(Comparator.comparingDouble(value -> -value.position.distanceTo(CoffeeMain.client.gameRenderer.getCamera().getPos())))
            .forEachOrdered(waypoint -> {
                if (tracers) {
                    Renderer.R3D.renderLine(matrices, waypoint.color, Renderer.R3D.getCrosshairVector(), waypoint.position);
                }

                //                double distancePlayer = waypoint.position.distanceTo(Utils.getInterpolatedEntityPosition(client.player));
                Vec3d interpolatedEntityPosition = Utils.getInterpolatedEntityPosition(client.player);
                double distancePlayer = Utils.dist(waypoint.position.x, waypoint.position.z, interpolatedEntityPosition.x, interpolatedEntityPosition.z);
                double subbed1 = (fadeDistancePlayer - distancePlayer) / fadeDistancePlayer;
                subbed1 = MathHelper.clamp(subbed1, 0, 1);
                subbed1 = 1 - subbed1;
                Renderer.R3D.renderFilled(
                    matrices,
                    Renderer.Util.modify(waypoint.color, -1, -1, -1, (int) (subbed1 * 255)),
                    new Vec3d(waypoint.position.x - .2, CoffeeMain.client.world.getBottomY(), waypoint.position.z - .2),
                    new Vec3d(.4, CoffeeMain.client.world.getHeight(), .4)
                );
                Vec3d screenSpaceCoordinate = Renderer.R2D.getScreenSpaceCoordinate(waypoint.position, matrices);
                if (Renderer.R2D.isOnScreen(screenSpaceCoordinate)) {
                    real.add(() -> {
                        String t = waypoint.getName();
                        float width = FontRenderers.getRenderer().getStringWidth(t) + 4;
                        Renderer.R2D.renderRoundedQuad(
                            Renderer.R3D.getEmptyMatrixStack(),
                            new Color(20, 20, 20, 255),
                            screenSpaceCoordinate.x - width / 2d,
                            screenSpaceCoordinate.y - FontRenderers.getRenderer().getFontHeight() / 2d - 2,
                            screenSpaceCoordinate.x + width / 2d,
                            screenSpaceCoordinate.y + FontRenderers.getRenderer().getFontHeight() / 2d + 2,
                            5,
                            10
                        );
                        FontRenderers.getRenderer()
                            .drawCenteredString(
                                Renderer.R3D.getEmptyMatrixStack(),
                                t,
                                screenSpaceCoordinate.x,
                                screenSpaceCoordinate.y - FontRenderers.getRenderer().getFontHeight() / 2d,
                                1f,
                                1f,
                                1f,
                                1f
                            );
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
        if (showCompass) {
            double center = client.getWindow().getScaledWidth() / 2d;
            double width = 160;
            double offset = width / 2d;
            MatrixStack stack = new MatrixStack();
            for (Sightpoint sightpoint : getSightpoints()) {
                stack.push();
                double x = calculatePosOffset(sightpoint.yawTarget, width);
                double focus = calculateInterest(sightpoint.yawTarget);
                stack.translate(center + x - offset, 5 + (sightpoint.major ? 5 : 15), 0);
                stack.scale((float) focus, (float) focus, 1);

                FontAdapter fa = sightpoint.major ? FontRenderers.getCustomSize(22) : FontRenderers.getCustomSize(16);
                fa.drawCenteredString(stack, sightpoint.label, 0, -fa.getFontHeight() / 2d, sightpoint.color.getRGB());
                stack.pop();
            }
        }
    }

    double calculatePosOffset(float targetYaw, double width) {
        float yaw = MathHelper.wrapDegrees(client.gameRenderer.getCamera().getYaw() - targetYaw) + 180;
        return (1 - yaw / 360) * width;
    }

    double calculateInterest(float targetYaw) {
        float yaw = MathHelper.wrapDegrees(client.gameRenderer.getCamera().getYaw() - targetYaw) + 180;
        double i = (.5 - Math.abs((yaw) / 360 - .5)) * 2; // 0-1
        i = i * 1.5; // 0-1.5
        i -= 0.5; // -.5-1
        return MathHelper.clamp(i, 0, 1); // [-0.5]-0-1
    }

    record Sightpoint(String label, float yawTarget, Color color, boolean major) {
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
