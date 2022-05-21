/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.combat;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.config.DoubleSetting;
import cf.coffee.client.feature.config.EnumSetting;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.helper.event.EventType;
import cf.coffee.client.helper.event.Events;
import cf.coffee.client.helper.event.events.PacketEvent;
import cf.coffee.client.mixin.IEntityVelocityUpdateS2CPacketMixin;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

public class Velocity extends Module {

    final DoubleSetting multiplierX = this.config.create(new DoubleSetting.Builder(0.2).name("Horizontal velocity")
            .description("How much to multiply X and Z velocity by")
            .min(-2.5)
            .max(2.5)
            .precision(1)
            .get());
    final DoubleSetting multiplierY = this.config.create(new DoubleSetting.Builder(0.2).name("Vertical velocity")
            .description("How much to multiply Y velocity by")
            .min(-2.5)
            .max(2.5)
            .precision(1)
            .get());
    final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.Modify).name("Mode")
            .description("How to modify velocity")
            .get());

    public Velocity() {
        super("Velocity", "Modifies all incoming velocity updates", ModuleType.COMBAT);
        multiplierX.showIf(() -> mode.getValue() == Mode.Modify);
        multiplierY.showIf(() -> mode.getValue() == Mode.Modify);
        Events.registerEventHandler(EventType.PACKET_RECEIVE, event -> {
            if (!this.isEnabled() || CoffeeMain.client.player == null) {
                return;
            }
            PacketEvent pe = (PacketEvent) event;
            if (pe.getPacket() instanceof EntityVelocityUpdateS2CPacket packet && packet.getId() == CoffeeMain.client.player.getId()) {
                if (mode.getValue() == Mode.Modify) {
                    double velX = packet.getVelocityX() / 8000d; // don't ask me why they did this
                    double velY = packet.getVelocityY() / 8000d;
                    double velZ = packet.getVelocityZ() / 8000d;
                    velX *= multiplierX.getValue();
                    velY *= multiplierY.getValue();
                    velZ *= multiplierX.getValue();
                    IEntityVelocityUpdateS2CPacketMixin jesusFuckingChrist = (IEntityVelocityUpdateS2CPacketMixin) packet;
                    jesusFuckingChrist.setVelocityX((int) (velX * 8000));
                    jesusFuckingChrist.setVelocityY((int) (velY * 8000));
                    jesusFuckingChrist.setVelocityZ((int) (velZ * 8000));
                } else {
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

    public enum Mode {
        Modify, Ignore
    }
}
