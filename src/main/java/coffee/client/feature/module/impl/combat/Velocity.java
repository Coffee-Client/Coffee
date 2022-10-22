/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.combat;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.mixin.network.IEntityVelocityUpdateS2CPacketMixin;
import me.x150.jmessenger.MessageSubscription;
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
    final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.Modify).name("Mode").description("How to modify velocity").get());

    public Velocity() {
        super("Velocity", "Modifies all incoming velocity updates", ModuleType.COMBAT);
        multiplierX.showIf(() -> mode.getValue() == Mode.Modify);
        multiplierY.showIf(() -> mode.getValue() == Mode.Modify);
    }

    @MessageSubscription
    void onA(coffee.client.helper.event.impl.PacketEvent.Received pe) {
        if (CoffeeMain.client.player == null) {
            return;
        }
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
                pe.setCancelled(true);
            }
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

    public enum Mode {
        Modify, Ignore
    }
}
