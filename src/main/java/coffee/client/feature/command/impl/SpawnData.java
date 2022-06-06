/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.StreamlineArgumentParser;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.helper.nbt.NbtGroup;
import coffee.client.helper.nbt.NbtList;
import coffee.client.helper.nbt.NbtObject;
import coffee.client.helper.nbt.NbtProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class SpawnData extends Command {
    public SpawnData() {
        super("SpawnData", "Set pre-spawn conditions for spawn eggs", "preSpawn", "spawnData");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        if (index == 0) {
            return new PossibleArgument(ArgumentType.STRING, "position", "velocity", "cursor");
        }
        String s = args[0];
        if (s.equalsIgnoreCase("position") || s.equalsIgnoreCase("velocity")) {
            return switch (index) {
                case 1 -> new PossibleArgument(ArgumentType.NUMBER, "x");
                case 2 -> new PossibleArgument(ArgumentType.NUMBER, "y");
                case 3 -> new PossibleArgument(ArgumentType.NUMBER, "z");
                default -> super.getSuggestionsWithType(index, args);
            };
        }
        return super.getSuggestionsWithType(index, args);
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("position 0 69 420  (sets the spawn position to 0 69 420)",
                "velocity 0 2 0  (sets initial velocity to 2y)",
                "cursor  (sets spawn position to where you're looking)");
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide data point");
        StreamlineArgumentParser parser = new StreamlineArgumentParser(args);
        switch (parser.consumeString().toLowerCase()) {
            case "position" -> {
                validateArgumentsLength(args, 4, "Provide X, Y and Z coordinates");
                ItemStack stack = CoffeeMain.client.player.getInventory().getMainHandStack();
                if (!stack.hasNbt()) {
                    stack.setNbt(new NbtCompound());
                }

                NbtGroup ng = new NbtGroup(new NbtObject("EntityTag",
                        new NbtList("Pos",
                                new NbtProperty(parser.consumeDouble()),
                                new NbtProperty(parser.consumeDouble()),
                                new NbtProperty(parser.consumeDouble()))));
                NbtCompound tag = ng.toCompound();
                stack.getOrCreateNbt().copyFrom(tag);
                CoffeeMain.client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + CoffeeMain.client.player.getInventory().selectedSlot,
                        stack));
                message("Changed Spawning Position");
            }
            case "velocity" -> {
                validateArgumentsLength(args, 4, "Provide X, Y and Z velocity");
                ItemStack stack = CoffeeMain.client.player.getInventory().getMainHandStack();
                if (!stack.hasNbt()) {
                    stack.setNbt(new NbtCompound());
                }
                NbtGroup ng = new NbtGroup(new NbtObject("EntityTag",
                        new NbtList("Motion",
                                new NbtProperty(parser.consumeDouble()),
                                new NbtProperty(parser.consumeDouble()),
                                new NbtProperty(parser.consumeDouble()))));
                NbtCompound tag = ng.toCompound();
                stack.getOrCreateNbt().copyFrom(tag);
                CoffeeMain.client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + CoffeeMain.client.player.getInventory().selectedSlot,
                        stack));
                message("Changed Velocity");
            }
            case "cursor" -> {
                ItemStack stack = CoffeeMain.client.player.getInventory().getMainHandStack();
                if (!stack.hasNbt()) {
                    stack.setNbt(new NbtCompound());
                }
                Vec3d se = Objects.requireNonNull(CoffeeMain.client.player)
                        .raycast(255, CoffeeMain.client.getTickDelta(), true)
                        .getPos();
                NbtGroup ng = new NbtGroup(new NbtObject("EntityTag",
                        new NbtList("Pos", new NbtProperty(se.x), new NbtProperty(se.y), new NbtProperty(se.z))));
                NbtCompound tag = ng.toCompound();
                stack.getOrCreateNbt().copyFrom(tag);
                CoffeeMain.client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + CoffeeMain.client.player.getInventory().selectedSlot,
                        stack));
                message("Changed Spawning Position");
            }
            default -> error("Please use the format >prespawn <position/velocity/cursor> <x> <y> <z>");
        }
    }
}
