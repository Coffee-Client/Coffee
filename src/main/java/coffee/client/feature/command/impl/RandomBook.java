/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.IntegerArgumentParser;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomBook extends Command {

    public RandomBook() {
        super("RandomBook", "Writes random books", "RandomBook", "rbook");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index,
                new PossibleArgument(ArgumentType.STRING, "ascii", "raw", "unicode"),
                new PossibleArgument(ArgumentType.NUMBER, "(pages)")
        );
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 2, "Provide mode and pages");
        int size = new IntegerArgumentParser().parse(args[1]);
        switch (args[0].toLowerCase()) {
            case "raw" -> {
                List<String> title = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    title.add(String.valueOf((char) 2048).repeat(266));
                }

                Optional<String> pages = Optional.of("Raw");
                client.player.networkHandler.sendPacket(new BookUpdateC2SPacket(client.player.getInventory().selectedSlot, title, pages));
            }

            case "ascii" -> {
                Random r = new Random();
                List<String> title3 = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    StringBuilder page = new StringBuilder();
                    for (int j = 0; j < 266; j++) {
                        page.append((char) r.nextInt(25) + 97);
                    }
                    title3.add(page.toString());
                }


                Optional<String> pages3 = Optional.of("Ascii");
                client.player.networkHandler.sendPacket(new BookUpdateC2SPacket(client.player.getInventory().selectedSlot, title3, pages3));
            }

            case "unicode" -> {
                IntStream chars = new Random().ints(0, 0x10FFFF + 1);
                String text = chars.limit(210L * Math.round(size)).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
                List<String> title2 = new ArrayList<>();
                Optional<String> pages2 = Optional.of("Unicode");

                for (int t = 0; t < size; t++) {
                    title2.add(text.substring(t * 210, (t + 1) * 210));
                }

                client.player.networkHandler.sendPacket(new BookUpdateC2SPacket(client.player.getInventory().selectedSlot, title2, pages2));
            }
        }
    }
}
