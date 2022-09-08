/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.feature.gui.clickgui.element.ConfigsDisplay;
import coffee.client.helper.config.ConfigInputFile;
import coffee.client.helper.config.ConfigOutputStream;
import net.minecraft.text.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ConfigUtils extends Command {
    public static final File CONFIG_STORAGE = new File(CoffeeMain.BASE, "configs");

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ConfigUtils() {
        super("ConfigUtils", "Config file management", "configUtils", "cu");
        if (!CONFIG_STORAGE.exists()) {
            CONFIG_STORAGE.mkdir();
        }
    }

    public static List<ConfigInputFile> getConfigFiles() {
        List<ConfigInputFile> cfe = new ArrayList<>();
        for (File file : Objects.requireNonNullElse(CONFIG_STORAGE.listFiles(), new File[0])) {
            ConfigInputFile f = new ConfigInputFile(file);
            cfe.add(f);
        }
        return cfe;
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("load abc", "save abc");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        if (index == 0) {
            return new PossibleArgument(ArgumentType.STRING, "load", "save");
        }
        if (args[0].equals("load") && index == 1) {
            return new PossibleArgument(ArgumentType.STRING,
                Arrays.stream(Objects.requireNonNull(CONFIG_STORAGE.listFiles())).map(File::getName).toList().toArray(String[]::new));
        } else if (args[0].equals("save") && index >= 1) {
            return new PossibleArgument(ArgumentType.STRING, "<file name>");
        }
        return new PossibleArgument(ArgumentType.STRING);
    }

    @Override
    public void onExecute(String[] args) throws CommandException {
        validateArgumentsLength(args, 2, "Provide action and file");
        switch (args[0].toLowerCase()) {
            case "load" -> {
                File f = new File(CONFIG_STORAGE.getAbsolutePath() + "/" + String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                if (!f.exists()) {
                    error("That file doesn't exist");
                    return;
                }
                if (!f.isFile()) {
                    error("That's not a file");
                    return;
                }
                ConfigInputFile cif = new ConfigInputFile(f);
                int version = cif.getVersion();
                String name = cif.getName();
                boolean shouldWarn = CoffeeMain.getClientVersion() != version;
                if (shouldWarn) {
                    warn(
                        "The config file you're trying to load was saved with a different coffee version than you have currently. This might lead to some issues. Use with caution");
                }
                message("Loading config file " + name);
                cif.apply();
                if (ConfigsDisplay.instance != null) {
                    ConfigsDisplay.instance.reinit();
                }
                success("Loaded config file!");
            }
            case "save" -> {
                String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                File out = new File(CONFIG_STORAGE.getAbsolutePath() + "/" + name);
                if (out.exists()) {
                    warn("Overwriting file because it already exists");
                    if (!out.delete()) {
                        error("Failed to delete old file! Aborting");
                        return;
                    }
                }
                try (FileOutputStream fos = new FileOutputStream(out); ConfigOutputStream cos = new ConfigOutputStream(fos, name)) {
                    cos.write();
                    if (ConfigsDisplay.instance != null) {
                        ConfigsDisplay.instance.reinit();
                    }
                    MutableText t = Text.literal("Saved config! Click to open");
                    Style s = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click to open")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, out.getAbsolutePath()));
                    t.setStyle(s);
                    Objects.requireNonNull(CoffeeMain.client.player).sendMessage(t, false);
                } catch (Exception e) {
                    error("Couldn't save config: " + e.getLocalizedMessage());
                }
            }
            default -> error("Invalid action, need either load or save");
        }
    }

}
