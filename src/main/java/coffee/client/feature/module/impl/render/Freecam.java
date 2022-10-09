/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.impl.ChunkRenderQuery;
import coffee.client.helper.event.impl.NoclipQueryEvent;
import coffee.client.helper.event.impl.PacketEvent;
import me.x150.jmessenger.MessageSubscription;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class Freecam extends Module {
    final DoubleSetting speed = this.config.create(new DoubleSetting.Builder(2).name("Speed").description("The speed to fly with").min(0).max(10).precision(1).get());
    Vec3d startloc;
    float pitch = 0f;
    float yaw = 0f;
    boolean flewBefore;

    public Freecam() {
        super("Freecam", "Imitates spectator without you having permission to use it", ModuleType.RENDER);
    }

    @MessageSubscription
    void onPacketSend(PacketEvent.Sent event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.setCancelled(true);
        }
        if (event.getPacket() instanceof PlayerInputC2SPacket) {
            event.setCancelled(true);
        }
    }

    @MessageSubscription
    void onNoclip(NoclipQueryEvent event) {
        if (event.getPlayer().isOnGround()) {
            return;
        }
        event.setShouldNoclip(true);
    }

    @MessageSubscription
    void shouldRenderChunk(ChunkRenderQuery event) {
        event.setShouldRender(true);
    }

    @Override
    public void tick() {
        Objects.requireNonNull(client.player).getAbilities().setFlySpeed((float) (this.speed.getValue() + 0f) / 20f);
        client.player.getAbilities().flying = true;
    }

    @Override
    public void enable() {
        startloc = Objects.requireNonNull(client.player).getPos();
        pitch = client.player.getPitch();
        yaw = client.player.getYaw();
        client.gameRenderer.setRenderHand(false);
        flewBefore = client.player.getAbilities().flying;
        client.player.setOnGround(false);
    }

    @Override
    public void disable() {
        if (startloc != null) {
            Objects.requireNonNull(client.player).updatePosition(startloc.x, startloc.y, startloc.z);
        }
        startloc = null;
        Objects.requireNonNull(client.player).setYaw(yaw);
        client.player.setPitch(pitch);
        yaw = pitch = 0f;
        client.gameRenderer.setRenderHand(true);
        client.player.getAbilities().flying = flewBefore;
        client.player.getAbilities().setFlySpeed(0.05f);
        client.player.setVelocity(0, 0, 0);
    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        Objects.requireNonNull(client.player).setSwimming(false);
        client.player.setPose(EntityPose.STANDING);
    }

    @Override
    public void onHudRender() {

    }
}
