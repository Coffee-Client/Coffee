/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.config;

import lombok.SneakyThrows;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.function.Consumer;

/**
 * A setting describing a boolean
 */
public class BooleanSetting extends SettingBase<Boolean> {

    public BooleanSetting(Boolean defaultValue, String name, String description, List<Consumer<Boolean>> onChanged) {
        super(defaultValue, name, description, onChanged);
    }

    @Override
    @SneakyThrows
    public void serialize(DataOutputStream stream) {
        stream.writeBoolean(getValue());
    }

    @Override
    @SneakyThrows
    public void deserialize(DataInputStream stream) {
        this.setValue(stream.readBoolean());
    }

    @Override
    public Boolean parse(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1");
    }

    public static class Builder extends SettingBase.Builder<BooleanSetting.Builder, Boolean, BooleanSetting> {

        public Builder(Boolean defaultValue) {
            super(defaultValue);
        }

        @Override
        public BooleanSetting get() {
            return new BooleanSetting(defaultValue, name, description, changed);
        }
    }
}
