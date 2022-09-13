/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command;

import coffee.client.feature.addon.Addon;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.feature.command.impl.ApplyVel;
import coffee.client.feature.command.impl.Author;
import coffee.client.feature.command.impl.Ban;
import coffee.client.feature.command.impl.Bind;
import coffee.client.feature.command.impl.CheckCmd;
import coffee.client.feature.command.impl.Config;
import coffee.client.feature.command.impl.ConfigUtils;
import coffee.client.feature.command.impl.Damage;
import coffee.client.feature.command.impl.Debugger;
import coffee.client.feature.command.impl.Drop;
import coffee.client.feature.command.impl.EVclip;
import coffee.client.feature.command.impl.Effect;
import coffee.client.feature.command.impl.Equip;
import coffee.client.feature.command.impl.FakeItem;
import coffee.client.feature.command.impl.FakeNick;
import coffee.client.feature.command.impl.ForEach;
import coffee.client.feature.command.impl.Gamemode;
import coffee.client.feature.command.impl.HClip;
import coffee.client.feature.command.impl.Help;
import coffee.client.feature.command.impl.Hologram;
import coffee.client.feature.command.impl.Inject;
import coffee.client.feature.command.impl.Invsee;
import coffee.client.feature.command.impl.ItemData;
import coffee.client.feature.command.impl.ItemExploit;
import coffee.client.feature.command.impl.ItemSpoof;
import coffee.client.feature.command.impl.Kickall;
import coffee.client.feature.command.impl.Kill;
import coffee.client.feature.command.impl.MessageSpam;
import coffee.client.feature.command.impl.Panic;
import coffee.client.feature.command.impl.RageQuit;
import coffee.client.feature.command.impl.RandomBook;
import coffee.client.feature.command.impl.Rename;
import coffee.client.feature.command.impl.Reset;
import coffee.client.feature.command.impl.Say;
import coffee.client.feature.command.impl.SelfDestruct;
import coffee.client.feature.command.impl.SocketKick;
import coffee.client.feature.command.impl.SpawnData;
import coffee.client.feature.command.impl.Taco;
import coffee.client.feature.command.impl.Test;
import coffee.client.feature.command.impl.TitleLag;
import coffee.client.feature.command.impl.Toggle;
import coffee.client.feature.command.impl.VClip;
import coffee.client.feature.command.impl.ViewNbt;
import coffee.client.helper.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandRegistry {
    private static final List<Command> vanillaCommands = new ArrayList<>();
    private static final List<CustomCommandEntry> customCommands = new ArrayList<>();
    private static final List<Command> sharedCommands = new ArrayList<>();

    static {
        rebuildSharedCommands();
    }

    public static void registerCustomCommand(Addon addon, Command command) {
        for (CustomCommandEntry e : customCommands) {
            if (e.command.getClass() == command.getClass()) {
                throw new IllegalStateException("Command " + command.getClass().getSimpleName() + " already registered");
            }
        }
        customCommands.add(new CustomCommandEntry(addon, command));
        rebuildSharedCommands();
    }

    public static void clearCustomCommands(Addon addon) {
        customCommands.removeIf(customCommandEntry -> customCommandEntry.addon == addon);
        rebuildSharedCommands();
    }

    private static void rebuildSharedCommands() {
        sharedCommands.clear();
        sharedCommands.addAll(vanillaCommands);
        for (CustomCommandEntry customCommand : customCommands) {
            sharedCommands.add(customCommand.command);
        }
    }

    public static void init() {
        vanillaCommands.clear();
        vanillaCommands.add(new Toggle());
        vanillaCommands.add(new Config());
        vanillaCommands.add(new Gamemode());
        vanillaCommands.add(new Effect());
        vanillaCommands.add(new Hologram());
        vanillaCommands.add(new Help());
        vanillaCommands.add(new ForEach());
        vanillaCommands.add(new Drop());
        vanillaCommands.add(new Panic());
        vanillaCommands.add(new Rename());
        vanillaCommands.add(new ViewNbt());
        vanillaCommands.add(new Say());
        vanillaCommands.add(new ConfigUtils());
        vanillaCommands.add(new Invsee());
        vanillaCommands.add(new RageQuit());
        vanillaCommands.add(new FakeItem());
        vanillaCommands.add(new Taco());
        vanillaCommands.add(new Bind());
        vanillaCommands.add(new Test());
        vanillaCommands.add(new Kickall());
        vanillaCommands.add(new Inject());
        vanillaCommands.add(new ApplyVel());
        vanillaCommands.add(new Author());
        vanillaCommands.add(new Ban());
        vanillaCommands.add(new CheckCmd());
        vanillaCommands.add(new Damage());
        vanillaCommands.add(new Equip());
        vanillaCommands.add(new EVclip());
        vanillaCommands.add(new ItemSpoof());
        vanillaCommands.add(new HClip());
        vanillaCommands.add(new ItemData());
        vanillaCommands.add(new TitleLag());
        vanillaCommands.add(new SpawnData());
        vanillaCommands.add(new VClip());
        vanillaCommands.add(new MessageSpam());
        vanillaCommands.add(new RandomBook());
        vanillaCommands.add(new SocketKick());
        vanillaCommands.add(new SelfDestruct());
        vanillaCommands.add(new ItemExploit());
        vanillaCommands.add(new FakeNick());
        vanillaCommands.add(new Reset());
        vanillaCommands.add(new Kill());
        vanillaCommands.add(new Debugger());

        rebuildSharedCommands();
    }

    public static List<Command> getCommands() {
        return sharedCommands;
    }

    public static void execute(String command) {
        if (command.isEmpty() || command.isBlank()) {
            return; // nothing to execute
        }
        String[] spl = command.split(" +");
        if (spl.length == 0) {
            return; // this shouldnt happen in theory
        }
        String cmd = spl[0].toLowerCase();
        String[] args = Arrays.copyOfRange(spl, 1, spl.length);
        Command c = CommandRegistry.getByAlias(cmd);
        if (c == null) {
            Utils.Logging.error("Command \"" + cmd + "\" not found");
        } else {
            try {
                c.onExecute(args);
            } catch (CommandException cex) {
                Utils.Logging.error(cex.getMessage());
                if (cex.getPotentialFix() != null) {
                    Utils.Logging.error("Potential fix: " + cex.getPotentialFix());
                }
            } catch (Exception e) {
                Utils.Logging.error("Error while running command " + command);
                e.printStackTrace();
            }
        }
    }

    public static Command getByAlias(String n) {
        for (Command command : getCommands()) {
            for (String alias : command.getAliases()) {
                if (alias.equalsIgnoreCase(n)) {
                    return command;
                }
            }
        }
        return null;
    }

    record CustomCommandEntry(Addon addon, Command command) {
    }
}
