/*
 * vclip funny
 * fixxed
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.DoubleArgumentParser;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import net.minecraft.client.network.ClientPlayerEntity;

public class VClip extends Command {
    public VClip() {
        super("vclip", "Teleport vertically", "vclip <amount>");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index, new PossibleArgument(ArgumentType.NUMBER, "<amount>"));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide height");

        double amount = new DoubleArgumentParser().parse(args[0]);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.updatePosition(player.getX(), player.getY() + amount, player.getZ());
        } else {
            throw new CommandException("Player not found");
        }
    }
}

