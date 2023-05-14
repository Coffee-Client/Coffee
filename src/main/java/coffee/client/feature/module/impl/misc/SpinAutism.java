/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.util.Rotations;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

import java.util.Objects;

public class SpinAutism extends Module {
    final DoubleSetting speed = this.config.create(new DoubleSetting.Builder(5).name("Delay")
                                                                               .description("How much to wait when spinning")
                                                                               .min(0)
                                                                               .max(100)
                                                                               .precision(0)
                                                                               .get());
    int timeout = 0;

    public SpinAutism() {
        super("SpinAutism", "Spins around like a maniac and throws whatever you have", ModuleType.MISC);
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
    public void onFastTick() {
        timeout--; // decrease timeout
        if (timeout > 0) {
            return; // if timeout isn't expired, do nothing
        }
        timeout = (int) Math.floor(speed.getValue()); // timeout expired, set it back to full
        Rotations.setClientPitch((float) ((Math.random() * 60) - 30));
        Rotations.setClientYaw((float) (Math.random() * 360));
        PlayerInteractItemC2SPacket p = new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, Utils.increaseAndCloseUpdateManager(CoffeeMain.client.world));
        Objects.requireNonNull(CoffeeMain.client.getNetworkHandler()).sendPacket(p);
        PlayerMoveC2SPacket p1 = new PlayerMoveC2SPacket.LookAndOnGround(
            Rotations.getClientYaw(),
            Rotations.getClientPitch(),
            Objects.requireNonNull(CoffeeMain.client.player).isOnGround()
        );
        CoffeeMain.client.getNetworkHandler().sendPacket(p1);
    }

    @Override
    public void onHudRender() {

    }
}
