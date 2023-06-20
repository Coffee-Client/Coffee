/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.world;


import coffee.client.CoffeeMain;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.impl.PacketEvent;
import coffee.client.helper.util.Utils;
import me.x150.jmessenger.MessageSubscription;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MassUse extends Module {
    final List<Packet<?>> dontRepeat = new ArrayList<>();
    final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.Interact).name("Mode").description("How to use the item").get());
    final DoubleSetting uses = this.config.create(new DoubleSetting.Builder(3).name("Uses")
        .description("How many times to use the item")
        .min(1)
        .max(100)
        .precision(0)
        .get());

    public MassUse() {
        super("MassUse", "Uses an item or block several times", ModuleType.WORLD);
    }

    @MessageSubscription
    void onP(PacketEvent.Sent pe) {
        if (dontRepeat.contains(pe.getPacket())) {
            dontRepeat.remove(pe.getPacket());
            return;
        }
        switch (mode.getValue()) {
            case Interact -> {
                if (pe.getPacket() instanceof PlayerInteractBlockC2SPacket p1) {
                    PlayerInteractBlockC2SPacket pp = new PlayerInteractBlockC2SPacket(
                        p1.getHand(),
                        p1.getBlockHitResult(),
                        Utils.increaseAndCloseUpdateManager(CoffeeMain.client.world)
                    );
                    for (int i = 0; i < uses.getValue(); i++) {
                        dontRepeat.add(pp);
                        Objects.requireNonNull(client.getNetworkHandler()).sendPacket(pp);
                    }
                } else if (pe.getPacket() instanceof PlayerInteractItemC2SPacket p1) {
                    PlayerInteractItemC2SPacket pp = new PlayerInteractItemC2SPacket(p1.getHand(), Utils.increaseAndCloseUpdateManager(CoffeeMain.client.world));
                    for (int i = 0; i < uses.getValue(); i++) {
                        dontRepeat.add(pp);
                        Objects.requireNonNull(client.getNetworkHandler()).sendPacket(pp);
                    }
                }
            }

            case MassPlace -> {
                if (pe.getPacket() instanceof PlayerInteractBlockC2SPacket) {
                    HitResult hr = client.crosshairTarget;
                    if (hr instanceof BlockHitResult r) {
                        BlockPos p = r.getBlockPos();

                        for (int i = 0; i < uses.getValue(); i++) {
                            PlayerInteractBlockC2SPacket pp = new PlayerInteractBlockC2SPacket(
                                Hand.MAIN_HAND,
                                r,
                                Utils.increaseAndCloseUpdateManager(CoffeeMain.client.world)
                            );
                            dontRepeat.add(pp);
                            client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, p, Direction.UP));
                            client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, p, Direction.UP));
                            client.player.networkHandler.sendPacket(pp);
                        }
                    }
                }
            }

            case RandomPlace -> {
                if (pe.getPacket() instanceof PlayerInteractBlockC2SPacket) {
                    Random random = new Random();

                    for (int i = 0; i < uses.getValue(); i++) {
                        BlockPos pos = BlockPos.ofFloored(client.player.getPos()).add(random.nextInt(13) - 6, random.nextInt(13) - 6, random.nextInt(13) - 6);
                        PlayerInteractBlockC2SPacket pp = Utils.Packets.generatePlace(pos);
                        dontRepeat.add(pp);
                        client.player.networkHandler.sendPacket(pp);
                    }
                }
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
        Interact, MassPlace, RandomPlace
    }
}
