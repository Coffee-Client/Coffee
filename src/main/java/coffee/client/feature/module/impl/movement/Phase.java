/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.impl.NoclipQueryEvent;
import coffee.client.helper.event.impl.PacketEvent;
import me.x150.jmessenger.MessageSubscription;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;

import java.util.Objects;

public class Phase extends Module {

    public Phase() {
        super("Phase", "Go through walls when flying (works best with creative)", ModuleType.MOVEMENT);
    }

    @MessageSubscription
    void onNoclipQuery(NoclipQueryEvent event) {
        if (event.getPlayer().getAbilities().flying) {
            event.setShouldNoclip(true);
        }
    }

    @MessageSubscription
    void onPacketSend(PacketEvent.Sent pe) {
        if (CoffeeMain.client.player == null || !CoffeeMain.client.player.getAbilities().flying) {
            return;
        }
        Box p = CoffeeMain.client.player.getBoundingBox(CoffeeMain.client.player.getPose()).offset(0, 0.27, 0).expand(0.25);
        if (p.getYLength() < 2) {
            p = p.expand(0, 1, 0);
        }
        p = p.offset(CoffeeMain.client.player.getPos());
        if (pe.getPacket() instanceof PlayerMoveC2SPacket && !Objects.requireNonNull(CoffeeMain.client.world).isSpaceEmpty(CoffeeMain.client.player, p)) {
            pe.setCancelled(true);
        }
    }

    @Override
    public void tick() {
    }

    public boolean getNoClipState(PlayerEntity pe) {
        return this.isEnabled() && pe.getAbilities().flying;
    }

    @Override
    public void enable() {
        Objects.requireNonNull(CoffeeMain.client.player).setPose(EntityPose.STANDING);
        CoffeeMain.client.player.setOnGround(false);
        CoffeeMain.client.player.fallDistance = 0;
        CoffeeMain.client.player.setVelocity(0, 0, 0);
    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return getNoClipState(CoffeeMain.client.player) ? "Active" : null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        if (Objects.requireNonNull(CoffeeMain.client.player).getAbilities().flying) {
            CoffeeMain.client.player.setPose(EntityPose.STANDING);
            CoffeeMain.client.player.setOnGround(false);
            CoffeeMain.client.player.fallDistance = 0;
        }
    }

    @Override
    public void onHudRender() {

    }
}
