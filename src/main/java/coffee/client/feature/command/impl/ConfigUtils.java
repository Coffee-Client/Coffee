/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.command.impl;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.coloring.ArgumentType;
import coffee.client.feature.command.coloring.PossibleArgument;
import coffee.client.feature.command.coloring.StaticArgumentServer;
import coffee.client.feature.command.exception.CommandException;
import coffee.client.feature.config.SettingBase;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ConfigUtils extends Command {
    static final File CONFIG_STORAGE = new File(CoffeeMain.BASE, "configs");

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ConfigUtils() {
        super("ConfigUtils", "Config file management", "configUtils", "cu");
        if (!CONFIG_STORAGE.exists()) {
            CONFIG_STORAGE.mkdir();
        }
    }

    @Override
    public ExamplesEntry getExampleArguments() {
        return new ExamplesEntry("load abc", "save abc");
    }

    @Override
    public PossibleArgument getSuggestionsWithType(int index, String[] args) {
        return StaticArgumentServer.serveFromStatic(
                index,
                new PossibleArgument(ArgumentType.STRING, "load", "save"),
                new PossibleArgument(ArgumentType.STRING,
                        Arrays.stream(Objects.requireNonNull(CONFIG_STORAGE.listFiles())).map(File::getName).toList().toArray(String[]::new)
                )
        );
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
                try {
                    for (Module module : ModuleRegistry.getModules()) {
                        for (SettingBase<?> dynamicValue : module.config.getSettings()) {
                            dynamicValue.reset();
                        }
                    }
                    String config = FileUtils.readFileToString(f, Charsets.UTF_8);
                    JsonObject root = JsonParser.parseString(config).getAsJsonObject();
                    for (JsonElement jsonElement : root.get("config").getAsJsonArray()) {
                        JsonObject current = jsonElement.getAsJsonObject();
                        String moduleName = current.get("name").getAsString();
                        Module m = ModuleRegistry.getByName(moduleName);
                        if (m == null) {
                            warn("Config includes invalid module name \"" + moduleName + "\"");
                            continue;
                        }
                        for (JsonElement pairs : current.get("pairs").getAsJsonArray()) {
                            JsonObject c = pairs.getAsJsonObject();
                            String cname = c.get("key").getAsString();
                            String value = c.get("value").getAsString();
                            SettingBase<?> val = m.config.get(cname);
                            if (val != null) {
                                val.accept(value);
                            }
                        }
                    }
                    List<Module> shouldBeEnabled = new ArrayList<>();
                    for (JsonElement enabled : root.get("enabled").getAsJsonArray()) {
                        String n = enabled.getAsString();
                        Module m = ModuleRegistry.getByName(n);
                        if (m == null) {
                            warn("Config includes invalid module name \"" + n + "\"");
                            continue;
                        }
                        shouldBeEnabled.add(m);
                        if (!m.isEnabled()) {
                            m.setEnabled(true);
                        }
                    }
                    for (Module module : ModuleRegistry.getModules()) {
                        if (!shouldBeEnabled.contains(module) && module.isEnabled()) {
                            module.setEnabled(false);
                        }
                    }
                    success("Loaded config file!");
                } catch (Exception e) {
                    error("Couldn't load config: " + e.getLocalizedMessage());
                }
            }
            case "save" -> {
                File out = new File(CONFIG_STORAGE.getAbsolutePath() + "/" + String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                if (out.exists()) {
                    warn("Overwriting file because it already exists");
                    if (!out.delete()) {
                        error("Failed to delete old file! Aborting");
                        return;
                    }
                }
                try {
                    JsonObject base = new JsonObject();
                    JsonArray enabled = new JsonArray();
                    JsonArray config = new JsonArray();
                    for (Module module : ModuleRegistry.getModules()) {
                        if (module.getName().equalsIgnoreCase("Alts")) {
                            continue; // we do NOT want to include this
                        }
                        if (module.isEnabled()) {
                            enabled.add(module.getName());
                        }
                        JsonObject currentConfig = new JsonObject();
                        currentConfig.addProperty("name", module.getName());
                        JsonArray pairs = new JsonArray();
                        for (SettingBase<?> dynamicValue : module.config.getSettings()) {
                            if (dynamicValue.getValue().equals(dynamicValue.getDefaultValue())) {
                                continue; // no need to save that
                            }
                            JsonObject jesus = new JsonObject();
                            jesus.addProperty("key", dynamicValue.getName());
                            jesus.addProperty("value", dynamicValue.getConfigSave());
                            pairs.add(jesus);
                        }
                        if (pairs.size() == 0) {
                            continue;
                        }
                        currentConfig.add("pairs", pairs);
                        config.add(currentConfig);
                    }
                    base.add("enabled", enabled);
                    base.add("config", config);
                    FileUtils.writeStringToFile(out, base.toString(), Charsets.UTF_8, false);
                    MutableText t = Text.literal("[§9A§r] Saved config! Click to open");
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
