/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.command.impl;

import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.StreamlineArgumentParser;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.helper.PathFinder;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.PacketEvent;
import coffee.client.helper.event.events.WorldRenderEvent;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Test extends Command {
    PathFinder currentFinder;
    List<PlayerMoveC2SPacket> whitelist = new ArrayList<>();

    public Test() {
        super("Test", "REAL", "test");
        Events.registerEventHandlerClass(this);
    }

    @EventListener(EventType.WORLD_RENDER)
    void render(WorldRenderEvent ev) {
        if (currentFinder == null) {
            return;
        }
        if (currentFinder.startEntry == null) {
            return;
        }
        PathFinder.Entry current = currentFinder.startEntry;
        while (current.next != null) {
            Renderer.R3D.renderLine(ev.getContextStack(), Color.GREEN, Vec3d.of(current.pos).add(0.5, 0, 0.5), Vec3d.of(current.next.pos).add(0.5, 0, 0.5));
            current = current.next;
        }
    }

    @EventListener(EventType.PACKET_SEND)
    void onPacketSend(PacketEvent pe) {
        if (pe.getPacket() instanceof PlayerMoveC2SPacket pmcs && currentFinder != null) {
            if (!whitelist.remove(pmcs)) {
                pe.setCancelled(true);
            }
        }
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        StreamlineArgumentParser a = new StreamlineArgumentParser(args);
        int i = a.consumeInt();
        int i1 = a.consumeInt();
        int i2 = a.consumeInt();
        currentFinder = new PathFinder(client.player.getBlockPos(), new BlockPos(i, i1, i2));
        message("Finding path...");
        new Thread(() -> {
            boolean result = currentFinder.find();
            if (!result) {
                error("Failed to find path :(");
                currentFinder = null;
                return;
            }
            PathFinder.Entry currentEntry = currentFinder.startEntry;
            //            Vec3d lastPos = client.player.getPos();
            //            Vec3d lastDelta = new Vec3d(0, 0, 0);
            //            Vec3d travellable = new Vec3d(0,0,0);
            while (currentEntry.next != null) {
                Vec3d t = Vec3d.of(currentEntry.next.pos).add(.5, 0, .5);
                //                Vec3d currentDelta = Vec3d.of(currentEntry.next.pos).subtract(Vec3d.of(currentEntry.pos));
                //                if (currentDelta.equals(lastDelta)) {
                //                    travellable.add(currentDelta);
                //                }
                //                lastDelta = currentDelta;
                teleport(t);
                Utils.sleep(50);
                currentEntry = currentEntry.next;
            }
            currentFinder = null;
        }).start();
    }

    void teleport(Vec3d d) {
        BlockPos bp = new BlockPos(d).down();
        BlockState bs = client.world.getBlockState(bp);
        double newY = d.y;
        if (!bs.getMaterial().blocksMovement()) {
            newY -= Math.random();
        }
        client.player.updatePosition(d.x, d.y, d.z);
        PlayerMoveC2SPacket p = new PlayerMoveC2SPacket.PositionAndOnGround(d.x, newY, d.z, false);
        whitelist.add(p);
        client.getNetworkHandler().sendPacket(p);
    }
}
