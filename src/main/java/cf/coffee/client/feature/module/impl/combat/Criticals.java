/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.combat;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.config.EnumSetting;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleRegistry;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.feature.module.impl.movement.NoFall;
import cf.coffee.client.helper.event.EventType;
import cf.coffee.client.helper.event.Events;
import cf.coffee.client.helper.event.events.PacketEvent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class Criticals extends Module {

    final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.Packet).name("Mode")
            .description("How to deal crits")
            .get());

    public Criticals() {
        super("Criticals", "Makes you deal a perfect 10/10 crit every time", ModuleType.COMBAT);
        Events.registerEventHandler(EventType.PACKET_SEND, event1 -> {
            PacketEvent event = (PacketEvent) event1;
            if (CoffeeMain.client.player == null || CoffeeMain.client.getNetworkHandler() == null) {
                return;
            }
            if (event.getPacket() instanceof PlayerInteractEntityC2SPacket && this.isEnabled()) {
                Vec3d ppos = CoffeeMain.client.player.getPos();
                ModuleRegistry.getByClass(NoFall.class).enabled = false; // disable nofall modifying packets when we send these
                switch (mode.getValue()) {
                    case Packet -> {
                        PlayerMoveC2SPacket.PositionAndOnGround p1 = new PlayerMoveC2SPacket.PositionAndOnGround(ppos.x, ppos.y + 0.2, ppos.z, true);
                        PlayerMoveC2SPacket.PositionAndOnGround p2 = new PlayerMoveC2SPacket.PositionAndOnGround(ppos.x, ppos.y, ppos.z, false);
                        PlayerMoveC2SPacket.PositionAndOnGround p3 = new PlayerMoveC2SPacket.PositionAndOnGround(ppos.x, ppos.y + 0.000011, ppos.z, false);
                        PlayerMoveC2SPacket.PositionAndOnGround p4 = new PlayerMoveC2SPacket.PositionAndOnGround(ppos.x, ppos.y, ppos.z, false);
                        CoffeeMain.client.getNetworkHandler().sendPacket(p1);
                        CoffeeMain.client.getNetworkHandler().sendPacket(p2);
                        CoffeeMain.client.getNetworkHandler().sendPacket(p3);
                        CoffeeMain.client.getNetworkHandler().sendPacket(p4);
                    }
                    case TpHop -> {
                        PlayerMoveC2SPacket.PositionAndOnGround p5 = new PlayerMoveC2SPacket.PositionAndOnGround(ppos.x, ppos.y + 0.02, ppos.z, false);
                        PlayerMoveC2SPacket.PositionAndOnGround p6 = new PlayerMoveC2SPacket.PositionAndOnGround(ppos.x, ppos.y + 0.01, ppos.z, false);
                        CoffeeMain.client.getNetworkHandler().sendPacket(p5);
                        CoffeeMain.client.getNetworkHandler().sendPacket(p6);
                    }
                }
                ModuleRegistry.getByClass(NoFall.class).enabled = true; // re-enable nofall
            }
        });

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

    public enum Mode {
        Packet, TpHop
    }
}
