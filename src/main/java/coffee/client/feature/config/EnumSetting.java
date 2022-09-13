/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.config;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class EnumSetting<T extends Enum<?>> extends SettingBase<T> {
    private T[] values;

    @SuppressWarnings("unchecked")
    public EnumSetting(T defaultValue, String name, String description, List<Consumer<T>> onChanged) {
        super(defaultValue, name, description, onChanged);
        if (!Modifier.isPublic(defaultValue.getClass().getModifiers())) {
            throw new IllegalArgumentException("Enum has to be public!");
        }
        try {
            this.values = (T[]) defaultValue.getClass().getMethod("values").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public T parse(String value) {
        for (T t : values) {
            if (value.equalsIgnoreCase(t.toString())) {
                return t;
            }
        }
        return defaultValue;
    }

    public T[] getValues() {
        return values;
    }

    @Override
    public void setValue(T value) {
        if (Arrays.stream(values).noneMatch(t -> t.equals(value))) {
            return;
        }
        super.setValue(value);
    }

    public static class Builder<T extends Enum<?>> extends SettingBase.Builder<coffee.client.feature.config.EnumSetting.Builder<T>, T, EnumSetting<T>> {
        public Builder(T defaultValue) {
            super(defaultValue);
        }

        @Override
        public EnumSetting<T> get() {
            return new EnumSetting<>(defaultValue, name, description, changed);
        }
    }
}
