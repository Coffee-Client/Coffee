/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.helper.manager.HologramManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Objects;

public class Hologram extends Command {

    public Hologram() {
        super("Hologram", "Generates a hologram without needing op (requires creative)", "hologram", "holo", "hlg");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index,
                new PossibleArgument(ArgumentType.STRING, "(flags)"),
                new PossibleArgument(ArgumentType.STRING, "(message)"));
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("E Hello spawn egg",
                "EB I am baby",
                "BVM You can see and interact with me",
                "BGVM Help I'm a falling child which you can interact with!");
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 2, "Provide flags and text");
        String options = args[0].toLowerCase();
        boolean generateAsBaby = false;
        boolean generateAsEgg = false;
        boolean makeGravity = false;
        boolean makeVisible = false;
        boolean marker = true;
        for (char c : options.toCharArray()) {
            switch (c) {
                case 'e' -> generateAsEgg = true;
                case 'b' -> generateAsBaby = true;
                case 'g' -> makeGravity = true;
                case 'v' -> makeVisible = true;
                case 'm' -> marker = false;
                case 'n' -> {
                }
                default -> {
                    error("Unknown option \"" + c + "\". Valid options:");
                    message("  N = None (Placeholder)");
                    message("  E = Makes a spawn egg instead of an armor stand item");
                    message("  B = Makes the hologram entity small");
                    message("  G = Makes the hologram have gravity");
                    message("  V = Makes the hologram entity visible");
                    message("  M = Makes the hologram entity not a marker (enable interactions and hitbox)");
                    return;
                }
            }
        }
        String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Vec3d pos = Objects.requireNonNull(CoffeeMain.client.player).getPos();
        BlockPos displayable = CoffeeMain.client.player.getBlockPos();
        message("Armor stand config:");
        message("  Text: " + text);
        message("  Is baby: " + (generateAsBaby ? "Yes" : "No"));
        message("  Is egg: " + (generateAsEgg ? "Yes" : "No"));
        message("  Is invisible: " + (!makeVisible ? "Yes" : "No"));
        message("  Has gravity: " + (makeGravity ? "Yes" : "No"));
        message("  Is marker: " + (marker ? "Yes" : "No"));
        message("  Pos: " + displayable.getX() + ", " + displayable.getY() + ", " + displayable.getZ());
        HologramManager.Hologram h = HologramManager.generateDefault(text, pos)
                .isEgg(generateAsEgg)
                .isSmall(generateAsBaby)
                .hasGravity(makeGravity)
                .isVisible(makeVisible)
                .isMarker(marker);
        ItemStack stack = h.generate();
        message("Dont forget to open your inventory before placing");
        CoffeeMain.client.player.getInventory().addPickBlock(stack);
    }
}
