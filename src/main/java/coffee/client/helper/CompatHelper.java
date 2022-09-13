/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.helper;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.movement.NoLevitation;
import coffee.client.feature.module.impl.movement.NoPush;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Map;

public class CompatHelper {
    static final Map<String, Runnable> modsToLookOutFor = Util.make(new HashMap<>(), stringRunnableHashMap -> {
        stringRunnableHashMap.put("meteor-client", () -> disableModule(NoLevitation.class, "Meteor client is loaded"));
        stringRunnableHashMap.put("lithium", () -> disableModule(NoPush.class, "Lithium is loaded"));
    });
    static boolean anyFound = false;

    static <T extends Module> void disableModule(Class<T> clazz, String reason) {
        Module c = ModuleRegistry.getByClass(clazz);
        c.setDisabled(true);
        c.setDisabledReason(reason);
    }

    public static void init() {
        modsToLookOutFor.forEach((s, runnable) -> {
            if (FabricLoader.getInstance().isModLoaded(s)) {
                CoffeeMain.log(Level.WARN, "Found incompatible mod " + s + " loaded, some features might not be available");
                if (!anyFound) {
                    anyFound = true;
                }
                runnable.run();
            }
        });
        if (!wereAnyFound()) {
            CoffeeMain.log(Level.INFO, "No compatability issues found, all good");
        }
    }

    public static boolean wereAnyFound() {
        return anyFound;
    }
}
