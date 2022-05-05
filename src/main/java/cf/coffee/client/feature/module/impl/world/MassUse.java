/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.world;


import cf.coffee.client.feature.config.DoubleSetting;
import cf.coffee.client.feature.config.EnumSetting;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.helper.event.EventType;
import cf.coffee.client.helper.event.Events;
import cf.coffee.client.helper.event.events.PacketEvent;
import cf.coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MassUse extends Module {
    final List<Packet<?>> dontRepeat = new ArrayList<>();
    final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.Interact).name("Mode").description("How to use the item").get());
    final DoubleSetting uses = this.config.create(new DoubleSetting.Builder(3).name("Uses").description("How many times to use the item").min(1).max(100).precision(0).get());

    public MassUse() {
        super("MassUse", "Uses an item or block several times", ModuleType.WORLD);
        Events.registerEventHandler(EventType.PACKET_SEND, event -> {
            if (!this.isEnabled()) {
                return;
            }
            PacketEvent pe = (PacketEvent) event;
            if (dontRepeat.contains(pe.getPacket())) {
                dontRepeat.remove(pe.getPacket());
                return;
            }
            switch (mode.getValue()) {
                case Interact -> {
                    if (pe.getPacket() instanceof PlayerInteractBlockC2SPacket p1) {
                        PlayerInteractBlockC2SPacket pp = new PlayerInteractBlockC2SPacket(p1.getHand(), p1.getBlockHitResult());
                        for (int i = 0; i < uses.getValue(); i++) {
                            dontRepeat.add(pp);
                            Objects.requireNonNull(client.getNetworkHandler()).sendPacket(pp);
                        }
                    } else if (pe.getPacket() instanceof PlayerInteractItemC2SPacket p1) {
                        PlayerInteractItemC2SPacket pp = new PlayerInteractItemC2SPacket(p1.getHand());
                        for (int i = 0; i < uses.getValue(); i++) {
                            dontRepeat.add(pp);
                            Objects.requireNonNull(client.getNetworkHandler()).sendPacket(pp);
                        }
                    }
                }

                case MassPlace -> {
                    BlockHitResult r = (BlockHitResult) client.crosshairTarget;
                    BlockPos p = r.getBlockPos();
                    if (pe.getPacket() instanceof PlayerInteractBlockC2SPacket) {
                        for (int i = 0; i < uses.getValue(); i++) {
                            PlayerInteractBlockC2SPacket pp = new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, r);
                            dontRepeat.add(pp);
                            client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, p, Direction.UP));
                            client.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, p, Direction.UP));
                            client.player.networkHandler.sendPacket(pp);
                        }
                    }
                }

                case RandomPlace -> {
                    if (pe.getPacket() instanceof PlayerInteractBlockC2SPacket) {
                        Random random = new Random();

                        for (int i = 0; i < uses.getValue(); i++) {
                            BlockPos pos = new BlockPos(client.player.getPos()).add(random.nextInt(13) - 6, random.nextInt(13) - 6, random.nextInt(13) - 6);
                            PlayerInteractBlockC2SPacket pp = Utils.Packets.generatePlace(pos);
                            dontRepeat.add(pp);
                            client.player.networkHandler.sendPacket(pp);
                        }
                    }
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
        Interact, MassPlace, RandomPlace
    }
}
