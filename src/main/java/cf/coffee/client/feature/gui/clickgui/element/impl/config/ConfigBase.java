/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.gui.clickgui.element.impl.config;

import cf.coffee.client.feature.config.SettingBase;
import cf.coffee.client.feature.gui.clickgui.element.Element;

public abstract class ConfigBase<C extends SettingBase<?>> extends Element {
    final C configValue;

    public ConfigBase(double x, double y, double width, double height, C configValue) {
        super(x, y, width, height);
        this.configValue = configValue;
    }

    public C getConfigValue() {
        return configValue;
    }
}
