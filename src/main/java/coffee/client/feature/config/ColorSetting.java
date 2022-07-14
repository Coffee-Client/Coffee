/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.config;

import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;

public class ColorSetting extends SettingBase<Color> {
    /**
     * Constructs a new Setting
     *
     * @param defaultValue The default value
     * @param name         The name
     * @param description  The description
     */
    public ColorSetting(Color defaultValue, String name, String description, List<Consumer<Color>> onChanged) {
        super(defaultValue, name, description, onChanged);
    }

    @Override
    public String getConfigSave() {
        return this.value.getRGB() + "";
    }

    @Override
    public Color parse(String value) {
        try {
            return new Color(Integer.parseInt(value), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getDefaultValue();
    }

    public static class Builder extends SettingBase.Builder<coffee.client.feature.config.ColorSetting.Builder, Color, ColorSetting> {

        public Builder(Color defaultValue) {
            super(defaultValue);
        }

        @Override
        public ColorSetting get() {
            return new ColorSetting(defaultValue, name, description, changed);
        }
    }
}
