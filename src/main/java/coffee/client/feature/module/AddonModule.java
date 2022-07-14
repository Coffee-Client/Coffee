/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module;

public abstract class AddonModule extends Module {
    public AddonModule(String n, String d) {
        super(n, d, ModuleType.ADDON_PROVIDED);
    }
}
