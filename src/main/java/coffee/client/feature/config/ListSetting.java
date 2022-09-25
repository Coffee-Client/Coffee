/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.config;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ListSetting<T extends Enum<?>> extends SettingBase<ListSetting.FlagSet<T>> {
    private List<T> values;

    @SuppressWarnings("unchecked")
    public ListSetting(String name, String desc, List<Consumer<FlagSet<T>>> onChanged, FlagSet<T> defaultVal, Class<T> cValue) {
        super(defaultVal, name, desc, onChanged);
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
    public FlagSet<T> parse(String value) {
        String[] split = value.split(",");
        List<T> t = new ArrayList<>();
        for (T tv : values) {
            if (Arrays.stream(split).anyMatch(s -> s.equals(tv.name()))) {
                t.add(tv);
            }
        }
        FlagSet<T> ts = new FlagSet<>();
        ts.setChecked(t);
        return ts;
    }

    public List<T> getAllValues() {
        return values;
    }

    public boolean isChecked(T value) {
        return this.value.isSet(value);
    }

    @Override
    public String getConfigSave() {
        return value.getChecked().stream().map(t -> t.name()).collect(Collectors.joining(","));
    }

    public static class FlagSet<T extends Enum<?>> {
        @Getter
        @Setter
        List<T> checked = new ArrayList<>();
        @Getter
        Class<T> componentType;

        @SafeVarargs
        @SuppressWarnings("unchecked")
        public FlagSet(T... defaultChecked) {
            this.checked.addAll(Arrays.asList(defaultChecked));
            this.componentType = (Class<T>) defaultChecked.getClass().componentType();
        }

        public void set(T t) {
            if (!checked.contains(t)) {
                checked.add(t);
            }
        }

        public void unset(T t) {
            checked.remove(t);
        }

        public boolean isSet(T t) {
            return checked.contains(t);
        }
    }

    public static class Builder<T extends Enum<?>> extends SettingBase.Builder<Builder<T>, FlagSet<T>, ListSetting<T>> {
        Class<T> componentType;

        /**
         * Constructs a new builder
         *
         * @param defaultValue The default value
         */
        @SafeVarargs
        @SuppressWarnings("unchecked")
        public Builder(T... defaultValue) {
            super(new FlagSet<>(defaultValue));
            this.componentType = (Class<T>) defaultValue.getClass().componentType();
        }

        public Builder(FlagSet<T> f) {
            super(f);
            this.componentType = f.getComponentType();
        }

        @Override
        public ListSetting<T> get() {
            return new ListSetting<>(name, description, changed, defaultValue, componentType);
        }
    }
}
