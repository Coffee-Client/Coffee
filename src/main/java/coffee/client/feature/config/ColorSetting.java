/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.config;

import lombok.SneakyThrows;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

    @Override
    @SneakyThrows
    public void serialize(DataOutputStream stream) {
        stream.writeInt(this.value.getRGB());
    }

    @Override
    @SneakyThrows
    public void deserialize(DataInputStream stream) {
        this.setValue(new Color(stream.readInt()));
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
