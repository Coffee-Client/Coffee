/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.List;

// https://github.com/ViaVersion/ViaFabric/commit/c21985cc24ca92c48ed168e050ab81a5320a01f5
@Pseudo
@Mixin(targets = "com.viaversion.fabric.common.provider.AbstractFabricPlatform",remap = false)
public class AbstractFabricPlatformMixin {
    private static final List<String> blacklistedNamesThatShouldntBeBlacklisted = List.of("guardian", "gaslight", "nochatreports");

    /**
     * @author 0x150
     * @reason Viafabric can suck my ass
     */
    @Overwrite
    public final boolean hasPlugin(String name) {
        if (blacklistedNamesThatShouldntBeBlacklisted.contains(name)) {
            return false;
        }
        return FabricLoader.getInstance().isModLoaded(name);
    }
}
