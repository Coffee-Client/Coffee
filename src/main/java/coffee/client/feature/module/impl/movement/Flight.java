/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.PacketEvent;
import coffee.client.helper.util.Timer;
import coffee.client.helper.util.Utils;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Flight extends Module {

    final EnumSetting<FlightMode> mode = this.config.create(new EnumSetting.Builder<>(FlightMode.Vanilla).name("Mode").description("How you fly").get());
    final BooleanSetting bypassVanillaAc = this.config.create(new BooleanSetting.Builder(true).name("Bypass vanilla AC").description("Whether to bypass the vanilla anticheat").get());
    final DoubleSetting speed = this.config.create(new DoubleSetting.Builder(2).name("Speed").description("How fast you fly").min(0).max(10).get());
    final List<Packet<?>> queue = new ArrayList<>();
    final Timer lag = new Timer();
    int bypassTimer = 0;
    boolean flewBefore = false;


    public Flight() {
        super("Flight", "Allows you to fly without having permission to", ModuleType.MOVEMENT);
        Events.registerEventHandler(EventType.PACKET_SEND, event -> {
            if (!this.isEnabled()) {
                return;
            }
            PacketEvent pe = (PacketEvent) event;
            if (pe.getPacket() instanceof ClientCommandC2SPacket p && p.getMode() == ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY) {
                event.setCancelled(true);
            }
        }, 0);
    }

    @Override
    public void tick() {
        Utils.setClientTps(20F);
        if (CoffeeMain.client.player == null || CoffeeMain.client.world == null || CoffeeMain.client.getNetworkHandler() == null) {
            return;
        }
        double speed = this.speed.getValue();
        if (bypassVanillaAc.getValue()) {
            bypassTimer++;
            if (bypassTimer > 10) {
                bypassTimer = 0;
                Vec3d p = CoffeeMain.client.player.getPos();
                CoffeeMain.client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(p.x, p.y - 0.2, p.z, false));
                CoffeeMain.client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(p.x, p.y + 0.2, p.z, false));
            }
        }
        switch (mode.getValue()) {
            case Vanilla -> {
                CoffeeMain.client.player.getAbilities().setFlySpeed((float) (this.speed.getValue() + 0f) / 20f);
                CoffeeMain.client.player.getAbilities().flying = true;
            }
            case Static -> {
                GameOptions go = CoffeeMain.client.options;
                float y = CoffeeMain.client.player.getYaw();
                int mx = 0, my = 0, mz = 0;

                if (go.jumpKey.isPressed()) {
                    my++;
                }
                if (go.backKey.isPressed()) {
                    mz++;
                }
                if (go.leftKey.isPressed()) {
                    mx--;
                }
                if (go.rightKey.isPressed()) {
                    mx++;
                }
                if (go.sneakKey.isPressed()) {
                    my--;
                }
                if (go.forwardKey.isPressed()) {
                    mz--;
                }
                double ts = speed / 2;
                double s = Math.sin(Math.toRadians(y));
                double c = Math.cos(Math.toRadians(y));
                double nx = ts * mz * s;
                double nz = ts * mz * -c;
                double ny = ts * my;
                nx += ts * mx * -c;
                nz += ts * mx * -s;
                Vec3d nv3 = new Vec3d(nx, ny, nz);
                CoffeeMain.client.player.setVelocity(nv3);
            }
            case Jetpack -> {
                if (CoffeeMain.client.options.jumpKey.isPressed()) {
                    assert CoffeeMain.client.player != null;
                    CoffeeMain.client.player.addVelocity(0, speed / 30, 0);
                    Vec3d vp = CoffeeMain.client.player.getPos();
                    Random r = new Random();
                    for (int i = 0; i < 10; i++) {
                        CoffeeMain.client.world.addImportantParticle(ParticleTypes.SOUL_FIRE_FLAME, true, vp.x, vp.y, vp.z, (r.nextDouble() * 0.25) - .125, (r.nextDouble() * 0.25) - .125, (r.nextDouble() * 0.25) - .125);
                    }
                }
            }
            case Walk -> {
                if (lag.hasExpired(490L)) {
                    lag.reset();
                    for (int i = 0; i < 3; i++) {
                        if (!queue.isEmpty()) {
                            Utils.sendPacket(queue.get(0));
                            queue.remove(0);
                        }
                    }
                }
                GameOptions go = CoffeeMain.client.options;
                float y = CoffeeMain.client.player.getYaw();
                client.player.setOnGround(true);
                client.player.setSprinting(true);
                client.player.strideDistance = 0.1F;
                Utils.setClientTps(10F);
                CoffeeMain.client.player.getAbilities().flying = false;
                int mx = 0, my = 0, mz = 0;

                if (go.jumpKey.isPressed()) {
                    my++;
                }
                if (go.backKey.isPressed()) {
                    mz++;
                }
                if (go.leftKey.isPressed()) {
                    mx--;
                }
                if (go.rightKey.isPressed()) {
                    mx++;
                }
                if (go.sneakKey.isPressed()) {
                    my--;
                }
                if (go.forwardKey.isPressed()) {
                    mz--;
                }
                double ts = speed / 2;
                double s = Math.sin(Math.toRadians(y));
                double c = Math.cos(Math.toRadians(y));
                double nx = ts * mz * s;
                double nz = ts * mz * -c;
                double ny = ts * my;
                nx += ts * mx * -c;
                nz += ts * mx * -s;
                Vec3d nv3 = new Vec3d(nx, ny, nz);
                CoffeeMain.client.player.setVelocity(nv3);
            }

        }
    }

    private void applyDamage(int amount) {
        Vec3d pos = CoffeeMain.client.player.getPos();
        sendPosition(pos.x, pos.y + amount + 2.1, pos.z, false);
        sendPosition(pos.x, pos.y + 0.05, pos.z, false);
        sendPosition(pos.x, pos.y, pos.z, true);
    }

    private void sendPosition(double x, double y, double z, boolean onGround) {
        CoffeeMain.client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround));
    }

    @Override
    public void enable() {
        if (mode.getValue() == FlightMode.Walk) {
            applyDamage(1);
            applyDamage(1);
        }
        bypassTimer = 0;
        flewBefore = Objects.requireNonNull(CoffeeMain.client.player).getAbilities().flying;
        CoffeeMain.client.player.setOnGround(false);
        Objects.requireNonNull(CoffeeMain.client.getNetworkHandler()).sendPacket(new ClientCommandC2SPacket(CoffeeMain.client.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
    }

    @EventListener(value = EventType.PACKET_SEND)
    void giveTwoShits(PacketEvent event) {
        if (mode.getValue() == FlightMode.Walk) {
            if (!this.isEnabled()) {
                return;
            }
            if (client.player == null || client.world == null) {
                setEnabled(false);
                return;
            }
            if (event.getPacket() instanceof KeepAliveC2SPacket) {
                return;
            }
            queue.add(event.getPacket());
            event.setCancelled(true);
        }
    }

    @Override
    public void disable() {
        Objects.requireNonNull(CoffeeMain.client.player).getAbilities().flying = flewBefore;
        CoffeeMain.client.player.getAbilities().setFlySpeed(0.05f);
        Utils.setClientTps(20F);
        if (client.player == null || client.getNetworkHandler() == null) {
            queue.clear();
            return;
        }
        for (Packet<?> packet : queue.toArray(new Packet<?>[0])) {
            client.getNetworkHandler().sendPacket(packet);
        }
        queue.clear();
    }

    @Override
    public String getContext() {
        return mode.getValue() + "";
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }

    public enum FlightMode {
        Vanilla, Static, ThreeD, Jetpack, Walk
    }
}
