/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.event.impl;

import coffee.client.helper.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.Packet;

@AllArgsConstructor
public abstract class PacketEvent extends Event {
    @Getter
    Packet<?> packet;

    public static class Received extends PacketEvent {
        public Received(Packet<?> packet) {
            super(packet);
        }
    }

    public static class Sent extends PacketEvent {
        public Sent(Packet<?> packet) {
            super(packet);
        }
    }
}
