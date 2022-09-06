/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.util.math.Direction;

public class CheckCmd extends Command {
    public CheckCmd() {
        super("CheckCmd", "Check if command blocks are enabled", "checkCmd");
    }

    @Override
    public void onExecute(String[] args) {
        message("Checking command blocks");
        CoffeeMain.client.player.networkHandler.sendPacket(new UpdateCommandBlockC2SPacket(CoffeeMain.client.player.getBlockPos().offset(Direction.DOWN, 1),
            "/",
            CommandBlockBlockEntity.Type.AUTO,
            false,
            false,
            false));
    }
}
