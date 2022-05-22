/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.PacketEvent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;

public class AntiOffhandCrash extends Module {

    public AntiOffhandCrash() {
        super("AntiOffhandCrash", "Prevents you from getting crashed by OffhandCrash", ModuleType.MISC);
        Events.registerEventHandler(EventType.PACKET_RECEIVE, event1 -> {
            if (!this.isEnabled()) {
                return;
            }
            PacketEvent event = (PacketEvent) event1;
            if (event.getPacket() instanceof PlaySoundS2CPacket) {
                if (((PlaySoundS2CPacket) event.getPacket()).getSound() == SoundEvents.ITEM_ARMOR_EQUIP_GENERIC) {
                    event.setCancelled(true);
                }
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
}
