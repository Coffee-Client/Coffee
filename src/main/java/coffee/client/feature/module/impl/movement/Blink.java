/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.movement;

import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import me.x150.jmessenger.MessageSubscription;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;

import java.util.ArrayList;
import java.util.List;

public class Blink extends Module {

    final List<Packet<?>> queue = new ArrayList<>();
    final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.Delay).name("Mode")
        .description("Whether to delay or remove the packets being sent")
        .get());

    public Blink() {
        super("Blink", "Delay or cancel outgoing packets", ModuleType.MOVEMENT);
    }

    @MessageSubscription
    void onPacket(coffee.client.helper.event.impl.PacketEvent.Sent event) {
        if (event.getPacket() instanceof KeepAliveC2SPacket) {
            return;
        }
        event.setCancelled(true);
        if (mode.getValue() == Mode.Delay) {
            queue.add(event.getPacket());
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
        return queue.size() + "";
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }

    public enum Mode {
        Delay, Drop
    }
}
