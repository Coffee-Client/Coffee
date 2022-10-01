/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.combat;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.PacketEvent;
import coffee.client.helper.util.Rotations;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class KnockbackPlus extends Module {


    public KnockbackPlus() {
        super("KnockbackPlus", "Makes you give the thing you're hitting extra knockback", ModuleType.COMBAT);}

    @EventListener(EventType.PACKET_SEND)
    void onPacketSend(PacketEvent pe) {
        if (CoffeeMain.client.player == null || CoffeeMain.client.getNetworkHandler() == null) {
            return;
        }
        if (pe.getPacket() instanceof PlayerInteractEntityC2SPacket && this.isEnabled()) {
            Vec3d a = Objects.requireNonNull(CoffeeMain.client.player).getPos().subtract(0, 1e-10, 0);
            Objects.requireNonNull(CoffeeMain.client.getNetworkHandler())
                    .sendPacket(new ClientCommandC2SPacket(CoffeeMain.client.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            PlayerMoveC2SPacket p = new PlayerMoveC2SPacket.PositionAndOnGround(a.x, a.y, a.z, true);
            CoffeeMain.client.getNetworkHandler().sendPacket(p);
        }
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

    }

    @Override
    public void onHudRender() {

    }
}
