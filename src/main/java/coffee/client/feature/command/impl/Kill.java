/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.StreamlineArgumentParser;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.helper.nbt.NbtGroup;
import coffee.client.helper.nbt.NbtList;
import coffee.client.helper.nbt.NbtObject;
import coffee.client.helper.nbt.NbtProperty;
import coffee.client.helper.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class Kill extends Command {
    public Kill() {
        super("Kill", "Kills someone in render distance", "kill");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index,
            new PossibleArgument(ArgumentType.STRING,
                Objects.requireNonNull(CoffeeMain.client.world)
                    .getPlayers()
                    .stream()
                    .map(abstractClientPlayerEntity -> abstractClientPlayerEntity.getGameProfile().getName())
                    .toList()
                    .toArray(String[]::new)));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        StreamlineArgumentParser a = new StreamlineArgumentParser(args);
        PlayerEntity playerEntity = a.consumePlayerEntityFromName(false);
        Vec3d pos = playerEntity.getPos();
        ItemStack current = client.player.getMainHandStack();
        ItemStack newStack = new ItemStack(Items.BAT_SPAWN_EGG);
        NbtGroup group = new NbtGroup(new NbtObject("EntityTag",
            new NbtProperty("Duration", 5),
            new NbtList("Effects", new NbtObject("", new NbtProperty("Amplifier", 125), new NbtProperty("Id", 6))),
            new NbtProperty("Radius", 10),
            new NbtProperty("WaitTime", 1),
            new NbtProperty("id", "minecraft:area_effect_cloud"),
            new NbtList("Pos", new NbtProperty(pos.x), new NbtProperty(pos.y + 1), new NbtProperty(pos.z))));
        newStack.setNbt(group.toCompound());
        BlockHitResult bhr = new BlockHitResult(pos, Direction.DOWN, new BlockPos(pos), false);
        client.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(Utils.Inventory.slotIndexToId(client.player.getInventory().selectedSlot), newStack));
        client.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bhr, Utils.increaseAndCloseUpdateManager(CoffeeMain.client.world)));
        //        client.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(Utils.Inventory.slotIndexToId(client.player.getInventory().selectedSlot), current));
    }
}

