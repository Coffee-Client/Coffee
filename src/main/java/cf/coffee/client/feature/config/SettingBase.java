/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * A class depicting a setting
 *
 * @param <V> The type of which value should be stored here
 */
public abstract class SettingBase<V> {
    /**
     * The name and description of this setting
     */
    public final String name, description;
    /**
     * The default value of this setting
     */
    final V defaultValue;
    final List<BooleanSupplier> suppliers = new ArrayList<>();
    final List<Consumer<V>> onChanged;
    /**
     * The current value of this setting
     */
    V value;

    /**
     * Constructs a new Setting
     *
     * @param defaultValue The default value
     * @param name         The name
     * @param description  The description
     */
    public SettingBase(V defaultValue, String name, String description, List<Consumer<V>> onChanged) {
        this.name = name;
        this.description = description;
        this.defaultValue = this.value = defaultValue;
        this.onChanged = onChanged;
    }

    public void showIf(BooleanSupplier supplier) {
        suppliers.add(supplier);
    }

    public String getConfigSave() {
        return getValue().toString();
    }

    public boolean shouldShow() {
        return suppliers.stream().allMatch(BooleanSupplier::getAsBoolean);
    }

    /**
     * Parses a string to its value, not implemented in the base class
     *
     * @param value The value we want to parse
     * @return The parsed output
     */
    public abstract V parse(String value);

    /**
     * Parses and sets a value, implemented because intellij idea is slightly retarded
     *
     * @param value The value we want to parse
     */
    public void accept(String value) {
        this.setValue(this.parse(value));
    }

    /**
     * Gets the current value of this setting
     *
     * @return The value of this setting
     */
    public V getValue() {
        return value;
    }

    /**
     * Sets the value of this setting
     *
     * @param value The new value
     */
    public void setValue(V value) {
        this.value = value;
        if (this.onChanged != null) for (Consumer<V> vConsumer : this.onChanged) {
            vConsumer.accept(value);
        }
    }

    public void reset() {
        this.setValue(this.getDefaultValue());
    }

    /**
     * Gets the description of this setting
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the name of this setting
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the default value of this setting
     *
     * @return The default value
     */
    public V getDefaultValue() {
        return defaultValue;
    }

    /**
     * Builds a new Setting
     *
     * @param <B> The builder class
     * @param <V> The type of value we want to parse
     * @param <S> The setting class
     */
    @SuppressWarnings("unchecked")
    public abstract static class Builder<B extends Builder<?, ?, ?>, V, S extends SettingBase<?>> {
        /**
         * Event listener when the value changed
         */
        final List<Consumer<V>> changed = new ArrayList<>();
        /**
         * Name and description
         */
        String name = "none", description = "";
        /**
         * The default value
         */
        V defaultValue;

        /**
         * Constructs a new builder
         *
         * @param defaultValue The default value
         */
        protected Builder(V defaultValue) {
            this.defaultValue = defaultValue;
        }

        /**
         * Sets the name of this setting
         *
         * @param name The name
         * @return The current builder
         */
        public B name(String name) {
            this.name = name;
            return getThis();
        }

        /**
         * Sets the description of this setting
         *
         * @param description The description
         * @return The current builder
         */
        public B description(String description) {
            this.description = description;
            return getThis();
        }

        /**
         * Sets the default value of this setting
         *
         * @param defaultValue The default value
         * @return The current builder
         */
        public B defaultValue(V defaultValue) {
            this.defaultValue = defaultValue;
            return getThis();
        }

        /**
         * Sets the changed listener of this setting
         *
         * @param changed The listener
         * @return The current builder
         */
        public B onChanged(Consumer<V> changed) {
            this.changed.add(changed);
            return getThis();
        }

        /**
         * Constructs the setting, not implemented in base class
         *
         * @return The setting
         */
        public abstract S get();

        protected B getThis() {
            return (B) this;
        }
    }
}
