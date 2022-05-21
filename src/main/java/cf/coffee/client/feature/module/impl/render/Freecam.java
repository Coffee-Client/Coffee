/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.render;

import cf.coffee.client.feature.config.DoubleSetting;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.helper.event.EventListener;
import cf.coffee.client.helper.event.EventType;
import cf.coffee.client.helper.event.events.ChunkRenderQueryEvent;
import cf.coffee.client.helper.event.events.PacketEvent;
import cf.coffee.client.helper.event.events.PlayerNoClipQueryEvent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class Freecam extends Module {

    //    final SliderValue speed = (SliderValue) this.config.create("Speed", 1, 0, 10, 1).description("The speed to fly with");
    final DoubleSetting speed = this.config.create(new DoubleSetting.Builder(1).name("Speed")
            .description("The speed to fly with")
            .min(0)
            .max(10)
            .precision(1)
            .get());
    Vec3d startloc;
    float pitch = 0f;
    float yaw = 0f;
    boolean flewBefore;

    public Freecam() {
        super("Freecam", "Imitates spectator without you having permission to use it", ModuleType.RENDER);
    }

    @EventListener(type = EventType.PACKET_SEND)
    void onPacketSend(PacketEvent event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.setCancelled(true);
        }
        if (event.getPacket() instanceof PlayerInputC2SPacket) {
            event.setCancelled(true);
        }
    }

    @EventListener(type = EventType.NOCLIP_QUERY)
    void onNoclip(PlayerNoClipQueryEvent event) {
        if (event.getPlayer().isOnGround()) return;
        event.setNoClipState(PlayerNoClipQueryEvent.NoClipState.ACTIVE);
    }

    @EventListener(type = EventType.SHOULD_RENDER_CHUNK)
    void shouldRenderChunk(ChunkRenderQueryEvent event) {
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
