/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.DoubleArgumentParser;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class HClip extends Command {
    public HClip() {
        super("HClip", "Teleport horizontally", "hclip");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index, new PossibleArgument(ArgumentType.NUMBER, "<amount>"));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide distance");

        double brik = new DoubleArgumentParser().parse(args[0]);
        Vec3d forward = Vec3d.fromPolar(0, CoffeeMain.client.player.getYaw()).normalize();

        if (CoffeeMain.client.player.getAbilities().creativeMode) {
            CoffeeMain.client.player.updatePosition(CoffeeMain.client.player.getX() + forward.x * brik,
                CoffeeMain.client.player.getY(),
                CoffeeMain.client.player.getZ() + forward.z * brik);
        } else {
            clip(brik);
        }
    }

    private void clip(double blocks) {
        Vec3d pos = CoffeeMain.client.player.getPos();
        Vec3d forward = Vec3d.fromPolar(0, CoffeeMain.client.player.getYaw()).normalize();
        float oldy = CoffeeMain.client.player.getYaw();
        float oldp = CoffeeMain.client.player.getPitch();
        sendPosition(pos.x, pos.y + 9, pos.z);
        sendPosition(pos.x, pos.y + 18, pos.z);
        sendPosition(pos.x, pos.y + 27, pos.z);
        sendPosition(pos.x, pos.y + 36, pos.z);
        sendPosition(pos.x, pos.y + 45, pos.z);
        sendPosition(pos.x, pos.y + 54, pos.z);
        sendPosition(pos.x, pos.y + 63, pos.z);
        sendPosition(pos.x + forward.x * blocks, CoffeeMain.client.player.getY(), pos.z + forward.z * blocks);
        sendPosition(CoffeeMain.client.player.getX(), CoffeeMain.client.player.getY() - 9, CoffeeMain.client.player.getZ());
        sendPosition(CoffeeMain.client.player.getX(), CoffeeMain.client.player.getY() - 9, CoffeeMain.client.player.getZ());
        sendPosition(CoffeeMain.client.player.getX(), CoffeeMain.client.player.getY() - 9, CoffeeMain.client.player.getZ());
        sendPosition(CoffeeMain.client.player.getX(), CoffeeMain.client.player.getY() - 9, CoffeeMain.client.player.getZ());
        sendPosition(CoffeeMain.client.player.getX(), CoffeeMain.client.player.getY() - 9, CoffeeMain.client.player.getZ());
        sendPosition(CoffeeMain.client.player.getX(), CoffeeMain.client.player.getY() - 9, CoffeeMain.client.player.getZ());
        sendPosition(CoffeeMain.client.player.getX(), CoffeeMain.client.player.getY() - 8.9, CoffeeMain.client.player.getZ());
        sendPosition(CoffeeMain.client.player.getX(), CoffeeMain.client.player.getY(), CoffeeMain.client.player.getZ());
        CoffeeMain.client.player.setYaw(oldy);
        CoffeeMain.client.player.setPitch(oldp);
    }


    private void sendPosition(double x, double y, double z) {
        CoffeeMain.client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true));
    }
}
