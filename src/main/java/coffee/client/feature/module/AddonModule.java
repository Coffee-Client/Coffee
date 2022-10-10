/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module;

@SuppressWarnings("unused")
public abstract class AddonModule extends Module {
    public AddonModule(String n, String d) {
        super(n, d, ModuleType.ADDON_PROVIDED);
    }
}
