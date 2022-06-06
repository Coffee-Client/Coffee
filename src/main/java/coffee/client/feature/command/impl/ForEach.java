/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.IntegerArgumentParser;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.PacketEvent;
import coffee.client.helper.util.Utils;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForEach extends Command {
    final ExecutorService runner = Executors.newFixedThreadPool(1);
    String partial;
    boolean recieving;

    public ForEach() {
        super("ForEach", "Do something for each player", "forEach", "for", "fe");
        Events.registerEventHandler(EventType.PACKET_RECEIVE, event -> {
            if (!recieving) {
                return;
            }
            PacketEvent pe = (PacketEvent) event;
            if (pe.getPacket() instanceof CommandSuggestionsS2CPacket packet) {
                Suggestions all = packet.getSuggestions();
                for (Suggestion i : all.getList()) {
                    String name = i.getText();
                    if (name.contains(CoffeeMain.client.player.getName().toString())) {
                        continue;
                    }
                    CoffeeMain.client.player.sendChatMessage(partial.replaceAll("%s", name));
                    message(partial.replaceAll("%s", name));
                }
                message("Foreach operation completed");
                recieving = false;
            }
        });
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("player 1000 /msg %s you stink",
                "tab 10 /kick %s Server wipe",
                "tab 0 /ban %s MOLED LLLLL");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index,
                new PossibleArgument(ArgumentType.STRING, "player", "tab"),
                new PossibleArgument(ArgumentType.NUMBER, "(delay)"),
                new PossibleArgument(ArgumentType.NUMBER, "(message)"));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 3, "Provide source, delay and message");
        int delay = new IntegerArgumentParser().parse(args[1]);
        switch (args[0]) {
            case "player" -> {
                for (PlayerListEntry playerListEntry : Objects.requireNonNull(CoffeeMain.client.getNetworkHandler())
                        .getPlayerList()) {
                    if (Utils.Players.isPlayerNameValid(playerListEntry.getProfile()
                            .getName()) && !playerListEntry.getProfile()
                            .getId()
                            .equals(Objects.requireNonNull(CoffeeMain.client.player).getUuid())) {
                        runner.execute(() -> {
                            try {
                                CoffeeMain.client.player.sendChatMessage(String.join(" ",
                                                Arrays.copyOfRange(args, 2, args.length))
                                        .replaceAll("%s", playerListEntry.getProfile().getName()));
                                Thread.sleep(delay);
                            } catch (Exception ignored) {
                            }
                        });
                    }
                }
            }
            case "tab" -> {
                partial = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                CoffeeMain.client.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(0,
                        partial + " "));
                recieving = true;
            }
            default -> error("Argument 1 has to be either player or tab");
        }
    }
}
