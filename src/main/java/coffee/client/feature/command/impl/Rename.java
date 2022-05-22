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
import coffee.client.helper.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;

import java.util.Objects;

public class Rename extends Command {

    public Rename() {
        super("Rename", "Renames an item (requires creative)", "rename", "rn", "name");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(index, new PossibleArgument(ArgumentType.STRING, "(new name)"));
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 1, "Provide new name");

        if (Objects.requireNonNull(CoffeeMain.client.player).getInventory().getMainHandStack().isEmpty()) {
            error("You're not holding anything");
            return;
        }
        ItemStack iStack = CoffeeMain.client.player.getInventory().getMainHandStack();
        iStack.setCustomName(Text.of("§r" + String.join(" ", args).replaceAll("&", "§")));
        if (!CoffeeMain.client.interactionManager.hasCreativeInventory()) {
            warn("You dont have creative mode; the item will only be renamed client side");
        } else {
            CoffeeMain.client.getNetworkHandler()
                    .sendPacket(new CreativeInventoryActionC2SPacket(Utils.Inventory.slotIndexToId(CoffeeMain.client.player.getInventory().selectedSlot), iStack));
        }
    }
}
