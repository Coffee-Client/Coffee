/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.addon;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.CommandRegistry;
import coffee.client.feature.config.ModuleConfig;
import coffee.client.feature.config.SettingBase;
import coffee.client.feature.gui.clickgui.ClickGUI;
import coffee.client.feature.gui.notifications.hudNotif.HudNotification;
import coffee.client.feature.module.AddonModule;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.helper.AddonClassLoader;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.base.Event;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class AddonManager {
    public static final File ADDON_DIRECTORY = new File(CoffeeMain.BASE, "addons");
    public static final File ADDON_RESOURCE_CACHE = new File(ADDON_DIRECTORY, ".res_cache");
    private static final int[] EXPECTED_CLASS_SIGNATURE = new int[] { 0xCA, 0xFE, 0xBA, 0xBE };
    public static AddonManager INSTANCE;
    private final List<AddonEntry> loadedAddons = new ArrayList<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private AddonManager() {
        INSTANCE = this;
        if (!ADDON_DIRECTORY.isDirectory()) {
            ADDON_DIRECTORY.delete();
        }
        if (!ADDON_DIRECTORY.exists()) {
            ADDON_DIRECTORY.mkdir();
        }
        if (!ADDON_RESOURCE_CACHE.isDirectory()) {
            ADDON_RESOURCE_CACHE.delete();
        }
        if (!ADDON_RESOURCE_CACHE.exists()) {
            ADDON_RESOURCE_CACHE.mkdir();
        }
        initializeAddons();

        Events.registerEventHandlerClass(this);
    }

    public static void init() {
        new AddonManager();
    }

    @EventListener(value = EventType.GAME_EXIT)
    @SuppressWarnings("unused")
    void saveConfig(Event event) {
        try {
            Files.walkFileTree(ADDON_RESOURCE_CACHE.toPath(), new FileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    //noinspection ResultOfMethodCallIgnored
                    file.toFile().delete();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Addon> getLoadedAddons() {
        return loadedAddons.stream().map(addonEntry -> addonEntry.registeredAddon).toList();
    }

    void initializeAddons() {
        for (File file : Objects.requireNonNull(ADDON_DIRECTORY.listFiles())) {
            if (file.getName().endsWith(".jar")) {
                safeLoadAddon(file);
            }
        }
        dispatchEnable();
    }

    Addon safeLoadAddon(File file) {
        CoffeeMain.log(Level.INFO, "Attempting to load addon " + file.getName());
        try {
            return loadAddon(file);
        } catch (Throwable e) {
            CoffeeMain.log(Level.ERROR, "Failed to load " + file.getName());
            e.printStackTrace();
            if (e instanceof NoClassDefFoundError noClassDefFoundError) {
                CoffeeMain.log(Level.INFO,
                    "This error is in releation to the class file being remapped for the wrong dev environment. If you're running this in a dev environment, this is on you. In this case, please ask the developer(s) for a \"dev\" jar, and use that instead. If not, please report this error to the addon developer(s).");
                CoffeeMain.log(Level.INFO, "(Some additional information about the error: ERR:CLASS_MISSING, class " + noClassDefFoundError.getMessage() + " not found)");
            }
            if (e instanceof IncompatibleClassChangeError) {
                CoffeeMain.log(Level.INFO,
                    "This error either occurs because the addon is heavily obfuscated and the obfuscator is bad, or because the addon is built on an outdated coffee SDK. Please report this error to the addon developer(s).");
            }
            if (e instanceof ClassCastException) {
                CoffeeMain.log(Level.INFO, "This error probably occurs because of an outdated coffee SDK. Please report this error to the addon developer(s).");
            }
            if (e instanceof NoSuchFileException) {
                CoffeeMain.log(Level.INFO, "This error means that the file was deleted when trying to load the addon, nothing to worry about.");
                HudNotification.create("Addon " + file.getName() + " was deleted, removing.", 5000, HudNotification.Type.ERROR);
            }
        }
        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void discoverNewAddons() {
        for (File file : Objects.requireNonNull(ADDON_DIRECTORY.listFiles())) {
            if (loadedAddons.stream().anyMatch(addonEntry -> addonEntry.sourceFile.getAbsoluteFile().equals(file.getAbsoluteFile()))) {
                continue;
            }
            if (file.getName().endsWith(".jar")) {
                if (safeLoadAddon(file) == null) {
                    CoffeeMain.log(Level.WARN, "Renaming addon to prevent re-discovery because it failed to load");
                    file.renameTo(new File(file.getAbsolutePath() + ".disabled"));
                }
            }
        }
    }

    public void loadFromFile(File f) {
        File file = new File(ADDON_DIRECTORY, f.getName());
        try {
            Files.copy(f.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            safeLoadAddon(file);
        } catch (Exception e) {
            e.printStackTrace();
            HudNotification.create("Failed to copy addon " + f.getName(), 5000, HudNotification.Type.ERROR);
        }
    }

    public void reload(Addon addon) {
        addon.reloaded();
        Map<String, ModuleConfig> storedConfig = new ConcurrentHashMap<>();
        for (ModuleRegistry.AddonModuleEntry customModule : ModuleRegistry.getCustomModules()) {
            if (customModule.addon() == addon) {
                storedConfig.put(customModule.module().getName(), customModule.module().config);
            }
        }
        if (addon.isEnabled()) {
            disableAddon(addon);
        }
        AddonEntry meant = null;
        for (AddonEntry loadedAddon : loadedAddons) {
            if (loadedAddon.registeredAddon == addon) {
                meant = loadedAddon;
            }
        }
        if (meant != null) {
            loadedAddons.remove(meant);
            Addon loadedAddon = safeLoadAddon(meant.sourceFile);
            if (loadedAddon != null) {
                enableAddon(loadedAddon);
                for (ModuleRegistry.AddonModuleEntry customModule : ModuleRegistry.getCustomModules()) {
                    if (customModule.addon() == loadedAddon) {
                        Module additionalModule = customModule.module();
                        if (storedConfig.containsKey(additionalModule.getName())) {
                            List<SettingBase<?>> amog = additionalModule.config.getSettings(); // new config
                            for (SettingBase<?> setting : storedConfig.get(additionalModule.getName()).getSettings()) { // old saved config
                                for (SettingBase<?> settingBase : amog) { // merge
                                    if (settingBase.name.equals(setting.name)) { // if new name equals old name of setting
                                        settingBase.accept(setting.getConfigSave()); // set val
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void disableAddon(Addon addon) {
        if (!addon.isEnabled()) {
            throw new IllegalStateException("Addon already disabled");
        }
        addon.onDisable();
        ModuleRegistry.clearCustomModules(addon);
        CommandRegistry.clearCustomCommands(addon);
    }

    public void enableAddon(Addon addon) {
        if (addon.isEnabled()) {
            throw new IllegalStateException("Addon already enabled");
        }
        addon.onEnable();
        List<AddonModule> customModules = addon.getAdditionalModules();
        List<Command> customCommands = addon.getAdditionalCommands();
        if (customModules != null) {
            for (AddonModule additionalModule : customModules) {
                CoffeeMain.log(Level.INFO, "Loading module " + additionalModule.getName() + " from addon " + addon.name);
                ModuleRegistry.registerAddonModule(addon, additionalModule);
            }
        }
        if (customCommands != null) {
            for (Command customCommand : customCommands) {
                CoffeeMain.log(Level.INFO, "Loading command " + customCommand.getName() + " from addon " + addon.name);
                CommandRegistry.registerCustomCommand(addon, customCommand);
            }
        }
    }

    void dispatchEnable() {
        for (AddonEntry loadedAddon : loadedAddons) {
            enableAddon(loadedAddon.registeredAddon);
        }
        ClickGUI.reInit();
    }

    Addon loadAddon(File location) throws Exception {
        for (File file : Objects.requireNonNull(ADDON_RESOURCE_CACHE.listFiles())) {
            if (file.getName().startsWith(Math.abs(location.getName().hashCode()) + "-")) {
                // noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
        AddonClassLoader classLoader = new AddonClassLoader(AddonManager.class.getClassLoader());
        JarFile jf = new JarFile(location);
        Class<Addon> mainClass = null;
        for (JarEntry jarEntry : jf.stream().toList()) {
            if (jarEntry.isDirectory()) {
                continue;
            }
            InputStream stream = jf.getInputStream(jarEntry);
            if (jarEntry.getName().endsWith(".class")) {
                byte[] classBytes = stream.readAllBytes();

                byte[] cSig = Arrays.copyOfRange(classBytes, 0, 4);
                int[] cSigP = new int[4];
                for (int i = 0; i < cSig.length; i++) {
                    cSigP[i] = Byte.toUnsignedInt(cSig[i]);
                }
                if (!Arrays.equals(cSigP, EXPECTED_CLASS_SIGNATURE)) {
                    throw new IllegalStateException("Invalid class file signature for " + jarEntry.getName() + ": expected 0x" + Arrays.stream(EXPECTED_CLASS_SIGNATURE)
                        .mapToObj(value -> Integer.toHexString(value).toUpperCase())
                        .collect(Collectors.joining()) + ", got 0x" + Arrays.stream(cSigP)
                        .mapToObj(value -> Integer.toHexString(value).toUpperCase())
                        .collect(Collectors.joining()));
                }
                Class<?> loadedClass = classLoader.defineAndGetClass(classBytes);
                if (Addon.class.isAssignableFrom(loadedClass)) {
                    if (mainClass != null) {
                        throw new IllegalStateException("Jarfile " + location.getName() + " has multiple main classes");
                    }

                    // noinspection unchecked
                    mainClass = (Class<Addon>) loadedClass;

                }
            } else {
                File cacheFile = new File(ADDON_RESOURCE_CACHE,
                    Math.abs(location.getName().hashCode()) + "-" + Integer.toHexString((int) Math.floor(Math.random() * 0xFFFFFF)));
                FileUtils.writeByteArrayToFile(cacheFile, stream.readAllBytes());
                classLoader.defineResource(jarEntry.getName(), cacheFile.toURI().toURL());
            }
            stream.close();
        }
        if (mainClass == null) {
            throw new IllegalStateException("Jarfile " + location.getName() + " does not have a main class");
        }
        Addon mainClassA;
        try {
            mainClassA = mainClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Jarfile " + location.getName() + " has invalid main class: No constructor without arguments");
        }
        CoffeeMain.log(Level.INFO, "Discovered addon " + mainClassA.name + " by " + String.join(", ", mainClassA.developers));
        loadedAddons.add(new AddonEntry(location, mainClassA.name, mainClassA.description, mainClassA.developers, mainClassA));
        return mainClassA;
    }

    record AddonEntry(File sourceFile, String name, String description, String[] devs, Addon registeredAddon) {
    }
}
