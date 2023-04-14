/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
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
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class ApplyEffect extends Command {
    public ApplyEffect() {
        super("ApplyEffect", "Applies an effect to someone remotely. Requires creative", "applyEffect");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index,
            new PossibleArgument(ArgumentType.STRING,
                () -> Objects.requireNonNull(CoffeeMain.client.world)
                    .getPlayers()
                    .stream()
                    .map(abstractClientPlayerEntity -> abstractClientPlayerEntity.getGameProfile().getName())
                    .toList()
                    .toArray(String[]::new)),
            new PossibleArgument(ArgumentType.STRING, Registries.STATUS_EFFECT.getIds().stream().map(Identifier::toString).toArray(String[]::new)),
            new PossibleArgument(ArgumentType.NUMBER, "<amplifier>"),
            new PossibleArgument(ArgumentType.NUMBER, "<duration ticks>"));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        StreamlineArgumentParser argParser = new StreamlineArgumentParser(args);
        PlayerEntity target = argParser.consumePlayerEntityFromName(false);
        String effect = argParser.consumeString();
        int amp = argParser.consumeInt();
        int dur = argParser.consumeInt();
        Identifier id = Utils.throwSilently(() -> new Identifier(effect));
        if (id == null) {
            throw new CommandException("Invalid potion effect");
        }
        StatusEffect statusEffect = Registries.STATUS_EFFECT.get(id);
        int rawId = StatusEffect.getRawId(statusEffect);

        Vec3d pos = target.getPos();

        ItemStack previous = client.player.getMainHandStack();

        ItemStack newStack = new ItemStack(Items.BAT_SPAWN_EGG);
        NbtGroup group = new NbtGroup(new NbtObject("EntityTag",
            new NbtProperty("Duration", 5),
            new NbtList("Effects", new NbtObject("", new NbtProperty("Amplifier", amp), new NbtProperty("Duration", dur), new NbtProperty("Id", rawId))),
            new NbtProperty("Radius", 10),
            new NbtProperty("WaitTime", 1),
            new NbtProperty("id", "minecraft:area_effect_cloud"),
            new NbtList("Pos", new NbtProperty(pos.x), new NbtProperty(pos.y + 1), new NbtProperty(pos.z))));
        newStack.setNbt(group.toCompound());

        BlockHitResult bhr = new BlockHitResult(pos, Direction.DOWN, BlockPos.ofFloored(pos), false);
        client.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(Utils.Inventory.slotIndexToId(client.player.getInventory().selectedSlot), newStack));
        client.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bhr, Utils.increaseAndCloseUpdateManager(CoffeeMain.client.world)));
        client.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(Utils.Inventory.slotIndexToId(client.player.getInventory().selectedSlot), previous));
    }
}
