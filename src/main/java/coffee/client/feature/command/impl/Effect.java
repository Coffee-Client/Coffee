/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.argument.IntegerArgumentParser;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.exception.CommandException;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

public class Effect extends Command {

    public Effect() {
        super("Effect", "Gives you an effect client side", "effect", "eff");
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("give 3 100 255", "clear");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        if (index == 0) {
            return new PossibleArgument(ArgumentType.STRING, "give", "clear");
        } else if (args[0].equalsIgnoreCase("give")) {
            return switch (index) {
                case 1 -> new PossibleArgument(ArgumentType.NUMBER, "(effect id)");
                case 2 -> new PossibleArgument(ArgumentType.NUMBER, "(duration)");
                case 3 -> new PossibleArgument(ArgumentType.NUMBER, "(strength)");
                default -> super.getSuggestionsWithType(index, args);
            };
        }
        return super.getSuggestionsWithType(index, args);
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        if (CoffeeMain.client.player == null) {
            return;
        }
        validateArgumentsLength(args, 1, "Provide action");
        switch (args[0].toLowerCase()) {
            case "give" -> {
                validateArgumentsLength(args, 4, "Provide id, duration and strength");
                IntegerArgumentParser iap = new IntegerArgumentParser();
                int id = iap.parse(args[1]);
                int duration = iap.parse(args[2]);
                int strength = iap.parse(args[3]);
                StatusEffect effect = StatusEffect.byRawId(id);
                if (effect == null) {
                    error("Didnt find that status effect");
                    return;
                }
                StatusEffectInstance inst = new StatusEffectInstance(effect, duration, strength);
                CoffeeMain.client.player.addStatusEffect(inst);
            }
            case "clear" -> {
                for (StatusEffectInstance statusEffect : CoffeeMain.client.player.getStatusEffects().toArray(new StatusEffectInstance[0])) {
                    CoffeeMain.client.player.removeStatusEffect(statusEffect.getEffectType());
                }
            }
            default -> error("Choose one of \"give\" and \"clear\"");
        }
    }
}
