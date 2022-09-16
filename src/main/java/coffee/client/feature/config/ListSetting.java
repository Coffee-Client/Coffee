/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.config;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ListSetting<T extends Enum<?>> extends SettingBase<List<T>> {
    private List<T> values;

    @SuppressWarnings("unchecked")
    public ListSetting(String name, String desc, List<Consumer<List<T>>> onChanged, List<T> defaultChecked, Class<T> cValue) {
        super(defaultChecked, name, desc, onChanged);
        if (!Modifier.isPublic(cValue.getModifiers())) {
            throw new IllegalArgumentException("Enum has to be public!");
        }
        try {
            this.values = List.of((T[]) cValue.getMethod("values").invoke(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<T> parse(String value) {
        String[] split = value.split(",");
        List<T> t = new ArrayList<>();
        for (T tv : values) {
            if (Arrays.stream(split).anyMatch(s -> s.equals(tv.name()))) {
                t.add(tv);
            }
        }
        return t;
    }

    public List<T> getAllValues() {
        return values;
    }

    public boolean isChecked(T value) {
        return this.value.contains(value);
    }

    @Override
    public String getConfigSave() {
        return value.stream().map(t -> t.name()).collect(Collectors.joining(","));
    }

    public static class Builder<T extends Enum<?>> extends SettingBase.Builder<Builder<T>, List<T>, ListSetting<T>> {
        Class<T> componentType;

        /**
         * Constructs a new builder
         *
         * @param defaultValue The default value
         */
        @SafeVarargs
        @SuppressWarnings("unchecked")
        public Builder(T... defaultValue) {
            super(new ArrayList<>(List.of(defaultValue)));
            this.componentType = (Class<T>) defaultValue.getClass().componentType();
        }

        @Override
        public ListSetting<T> get() {
            return new ListSetting<>(name, description, changed, defaultValue, componentType);
        }
    }
}
