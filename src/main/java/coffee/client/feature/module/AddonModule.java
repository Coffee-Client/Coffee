/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module;

public abstract class AddonModule extends Module {
    public AddonModule(String n, String d) {
        super(n, d, ModuleType.ADDON_PROVIDED);
    }
}
