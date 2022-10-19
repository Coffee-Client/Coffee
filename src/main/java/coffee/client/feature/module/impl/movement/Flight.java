/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.feature.module.impl.exploit.Robowalk;
import coffee.client.helper.util.Timer;
import coffee.client.helper.util.Utils;
import coffee.client.mixin.network.IPlayerMoveC2SPacketMixin;
import me.x150.jmessenger.MessageSubscription;
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
    final BooleanSetting bypassVanillaAc = this.config.create(new BooleanSetting.Builder(true).name("Bypass vanilla AC")
        .description("Whether to bypass the vanilla anticheat")
        .get());
    final DoubleSetting speed = this.config.create(new DoubleSetting.Builder(2).name("Speed").description("How fast you fly").min(0).max(10).get());
    final List<Packet<?>> queue = new ArrayList<>();
    final Timer lag = new Timer();
    boolean flewBefore = false;
    long lastModify = System.currentTimeMillis();
    long endModifyCycle = System.currentTimeMillis();
    boolean holdModify = false;
    double holdOn = 0d;
    double lastY = 0;
    public Flight() {
        super("Flight", "Allows you to fly without having permission to", ModuleType.MOVEMENT);
    }

    static PlayerMoveC2SPacket.Full upgrade(PlayerMoveC2SPacket p) {
        Vec3d pos = client.player.getPos();
        float pitch = client.player.getPitch();
        float yaw = client.player.getYaw();
        PlayerMoveC2SPacket.Full full = new PlayerMoveC2SPacket.Full(p.getX(pos.x), p.getY(pos.y), p.getZ(pos.z), yaw, pitch, p.isOnGround());
        if (ModuleRegistry.getByClass(Robowalk.class).isEnabled()) {
            Robowalk.processPacket(full);
        }
        return full;
    }

    @MessageSubscription
    void onPacketSend(coffee.client.helper.event.impl.PacketEvent.Sent pe) {
        Packet<?> packet = pe.getPacket();
        if (packet instanceof ClientCommandC2SPacket p && p.getMode() == ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY) {
            pe.setCancelled(true);
        }
        if (packet instanceof PlayerMoveC2SPacket p) {
            Vec3d lsp = client.player.getPos();
            double y = p.getY(lsp.y);
            if (bypassVanillaAc.getValue()) {
                if (holdModify) {
                    p = upgrade(p);
                    y = p.getY(lsp.y);
                    pe.setPacket(p);
                    IPlayerMoveC2SPacketMixin pp = ((IPlayerMoveC2SPacketMixin) p);
                    if (endModifyCycle - System.currentTimeMillis() < 0) {
                        holdModify = false;
                        endModifyCycle = System.currentTimeMillis();
                        pp.setX(p.getX(lsp.x)); // default to current pos in case this one does NOT change position
                        pp.setZ(p.getZ(lsp.z));
                        pp.setY(y); // put us back on regular y if we end the hold cycle to reset the conditions
                        pp.setChangePosition(true);
                    } else {
                        pp.setX(p.getX(lsp.x)); // default to current pos in case this one does NOT change position
                        pp.setZ(p.getZ(lsp.z));
                        pp.setY(holdOn);
                        pp.setChangePosition(true);
                    }
                } else if (System.currentTimeMillis() - lastModify > 1000) {
                    p = upgrade(p);
                    y = p.getY(lsp.y);
                    pe.setPacket(p);
                    lastModify = System.currentTimeMillis();
                    endModifyCycle = System.currentTimeMillis() + 50;
                    this.holdModify = true;
                    if (!client.world.getBlockState(client.player.getBlockPos().down()).getMaterial().blocksMovement()) {
                        double delta = Math.max(0, y - lastY);
                        delta += 0.05d;
                        IPlayerMoveC2SPacketMixin pp = ((IPlayerMoveC2SPacketMixin) p);
                        pp.setX(p.getX(lsp.x)); // default to last known server pos in case this one does NOT change position
                        pp.setZ(p.getZ(lsp.z));
                        pp.setY(y - delta);
                        pp.setChangePosition(true);
                        holdOn = y - delta;
                    }
                }
            }

            lastY = y;

        }
    }

    @Override
    public void tick() {
        // Utils.setClientTps(20F);
        if (CoffeeMain.client.player == null || CoffeeMain.client.world == null || CoffeeMain.client.getNetworkHandler() == null) {
            return;
        }
        double speed = this.speed.getValue();
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
                        CoffeeMain.client.world.addImportantParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            true,
                            vp.x,
                            vp.y,
                            vp.z,
                            (r.nextDouble() * 0.25) - .125,
                            (r.nextDouble() * 0.25) - .125,
                            (r.nextDouble() * 0.25) - .125);
                    }
                }
            }
            case Walk -> {
                if (lag.hasExpired(490L)) {
                    lag.reset();
                    for (int i = 0; i < 3; i++) {
                        if (!queue.isEmpty()) {
                            Utils.sendPacketNoEvent(queue.get(0));
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
        this.lastY = client.player.getPos().getY();
        if (mode.getValue() == FlightMode.Walk) {
            applyDamage(1);
            applyDamage(1);
        }
        flewBefore = Objects.requireNonNull(CoffeeMain.client.player).getAbilities().flying;
        CoffeeMain.client.player.setOnGround(false);
        Objects.requireNonNull(CoffeeMain.client.getNetworkHandler())
            .sendPacket(new ClientCommandC2SPacket(CoffeeMain.client.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
    }

    @MessageSubscription
    void giveTwoShits(coffee.client.helper.event.impl.PacketEvent.Sent event) {
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
