/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.events.MouseEvent;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ClickTP extends Module {

    private static BlockPos targeted = new BlockPos(0, 0, 0);
    final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.Normal).name("Mode").description("The way to teleport").get());
    final BooleanSetting onlyctrl = this.config.create(new BooleanSetting.Builder(false).name("Only Ctrl").description("Only teleport when the control key is pressed").get());

    public ClickTP() {
        super("ClickTP", "Teleport somewhere you clicked to", ModuleType.MOVEMENT);
        //        Events.registerEventHandlerClass(this);
    }

    private static int lengthTo(BlockPos p) {
        Vec3d v = new Vec3d(p.getX(), p.getY(), p.getZ());
        return (int) roundToN(v.distanceTo(client.player.getPos()), 0);
    }

    public static double roundToN(double x, int n) {
        if (n == 0) {
            return Math.floor(x);
        }
        double factor = Math.pow(10, n);
        return Math.round(x * factor) / factor;
    }

    @Override
    public void tick() {
        BlockHitResult ray = (BlockHitResult) client.player.raycast(200, client.getTickDelta(), true);
        targeted = new BlockPos(ray.getBlockPos());
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

    @EventListener(value = EventType.MOUSE_EVENT)
    void onMouseEvent(MouseEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (event.getButton() == 1 && event.getAction() == 1) {
            if (CoffeeMain.client.currentScreen != null) {
                return;
            }
            BlockHitResult ray = (BlockHitResult) client.player.raycast(200, client.getTickDelta(), true);
            int rd = lengthTo(ray.getBlockPos());
            int raycastdistance = rd / 7;
            BlockHitResult blockHitResult = (BlockHitResult) client.player.raycast(200, client.getTickDelta(), true);
            BlockPos d = new BlockPos(blockHitResult.getBlockPos());
            BlockPos dest = new BlockPos(d.getX() + 0.5, d.getY(), d.getZ() + 0.5);
            dest = dest.offset(Direction.UP, 1);

            if (onlyctrl.getValue() && !client.options.sprintKey.isPressed()) {
                return;
            }

            switch (mode.getValue()) {
                case Normal -> client.player.updatePosition(dest.getX(), dest.getY(), dest.getZ());

                case Split -> {
                    client.player.jump();
                    ClientPlayerEntity player = client.player;
                    Vec3d playerpos = player.getPos();
                    double xn = dest.getX() - playerpos.x;
                    double yn = dest.getY() - playerpos.y;
                    double zn = dest.getZ() - playerpos.z;
                    double x = xn / raycastdistance;
                    double y = yn / raycastdistance;
                    double z = zn / raycastdistance;
                    for (int i = 0; i < raycastdistance; i++) {
                        client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(player.getX() + x, player.getY() + y, player.getZ() + z, true));
                    }
                    player.updatePosition(dest.getX(), dest.getY(), dest.getZ());
                }

                case Tween -> new Thread(() -> {
                    int rdd = lengthTo(ray.getBlockPos());
                    BlockPos destt = new BlockPos(blockHitResult.getBlockPos());
                    client.player.jump();
                    ClientPlayerEntity player = client.player;
                    Vec3d playerpos = player.getPos();
                    double xn = destt.getX() - playerpos.x;
                    double yn = destt.getY() - playerpos.y;
                    double zn = destt.getZ() - playerpos.z;
                    double x = xn / rdd;
                    double y = yn / rdd;
                    double z = zn / rdd;
                    for (int i = 0; i < rdd; i++) {
                        client.player.updatePosition(player.getX() + x, player.getY() + y, player.getZ() + z);
                        try {
                            Thread.sleep(7);
                        } catch (Exception ignored) {
                        }
                        client.player.setVelocity(0, 0, 0);
                    }
                }).start();

                case Experimental -> new Thread(() -> {
                    int rdd = lengthTo(ray.getBlockPos());
                    BlockPos destt = new BlockPos(blockHitResult.getBlockPos());
                    client.player.jump();
                    ClientPlayerEntity player = client.player;
                    Vec3d playerpos = player.getPos();
                    double xn = destt.getX() - playerpos.x;
                    double yn = destt.getY() - playerpos.y;
                    double zn = destt.getZ() - playerpos.z;
                    double x = xn / rdd;
                    double y = yn / rdd;
                    double z = zn / rdd;
                    for (int i = 0; i < rdd; i++) {
                        client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(player.getX() + x, player.getY() + y, player.getZ() + z, true));
                        try {
                            Thread.sleep(10);
                        } catch (Exception ignored) {
                        }
                        client.player.setVelocity(0, 0, 0);
                    }
                }).start();
            }
        }
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        Vec3d vp = new Vec3d(targeted.getX(), targeted.getY(), targeted.getZ());
        Renderer.R3D.renderFilled(matrices, Utils.getCurrentRGB(), vp, new Vec3d(1, 1, 1));
    }

    @Override
    public void onHudRender() {

    }

    public enum Mode {
        Split, Normal, Tween, Experimental
    }
}
