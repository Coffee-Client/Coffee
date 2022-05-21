/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.movement;

import cf.coffee.client.feature.config.DoubleSetting;
import cf.coffee.client.feature.config.EnumSetting;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.helper.event.EventType;
import cf.coffee.client.helper.event.Events;
import cf.coffee.client.helper.event.events.PacketEvent;
import cf.coffee.client.mixin.IPlayerMoveC2SPacketAccessor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * @see IPlayerMoveC2SPacketAccessor
 */
public class NoFall extends Module {

    final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.OnGround).name("Mode")
            .description("How to spoof packets (packet drowns the others out, use with caution)")
            .get());
    final DoubleSetting fallDist = this.config.create(new DoubleSetting.Builder(3).name("Fall distance")
            .description("How much to fall before breaking the fall")
            .min(1)
            .max(10)
            .precision(1)
            .get());
    public boolean enabled = true;

    public NoFall() {
        super("NoFall", "Prevents fall damage", ModuleType.MOVEMENT);

        this.fallDist.showIf(() -> mode.getValue() != Mode.OnGround);
        Events.registerEventHandler(EventType.PACKET_SEND, event1 -> {
            if (!this.isEnabled() || !enabled) {
                return;
            }
            PacketEvent event = (PacketEvent) event1;
            if (event.getPacket() instanceof PlayerMoveC2SPacket) {
                if (mode.getValue() == Mode.OnGround) {
                    ((IPlayerMoveC2SPacketAccessor) event.getPacket()).setOnGround(true);
                }
            }
        });
    }

    @Override
    public void tick() {
        if (client.player == null || client.getNetworkHandler() == null) {
            return;
        }
        if (client.player.fallDistance > fallDist.getValue()) {
            switch (mode.getValue()) {
                case Packet -> client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
                case BreakFall -> {
                    client.player.setVelocity(0, 0.1, 0);
                    client.player.fallDistance = 0;
                }
            }
        }
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return mode.getValue().name();
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }

    public enum Mode {
        OnGround, Packet, BreakFall
    }
}
