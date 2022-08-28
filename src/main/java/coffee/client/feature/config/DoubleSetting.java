/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.config;

import lombok.SneakyThrows;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.function.Consumer;

public class DoubleSetting extends SettingBase<Double> {
    final int precision;
    final double min;
    final double max;

    public DoubleSetting(Double defaultValue, String name, String description, int precision, double min, double max, List<Consumer<Double>> onChanged) {
        super(defaultValue, name, description, onChanged);
        this.precision = precision;
        this.min = min;
        this.max = max;
    }

    @Override
    @SneakyThrows
    public void serialize(DataOutputStream stream) {
        stream.writeDouble(this.getValue());
    }

    @Override
    @SneakyThrows
    public void deserialize(DataInputStream stream) {
        this.setValue(stream.readDouble());
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public int getPrecision() {
        return precision;
    }

    @Override
    public Double parse(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    @Override
    public void setValue(Double value) {
        if (value > max || value < min) {
            return;
        }
        super.setValue(value);
    }

    public static class Builder extends SettingBase.Builder<coffee.client.feature.config.DoubleSetting.Builder, Double, DoubleSetting> {
        double min = Double.NEGATIVE_INFINITY, max = Double.POSITIVE_INFINITY;
        int precision = 2;

        public Builder(double defaultValue) {
            super(defaultValue);
        }

        public coffee.client.feature.config.DoubleSetting.Builder precision(int precision) {
            this.precision = precision;
            return this;
        }

        public coffee.client.feature.config.DoubleSetting.Builder min(double min) {
            this.min = min;
            return this;
        }

        public coffee.client.feature.config.DoubleSetting.Builder max(double max) {
            this.max = max;
            return this;
        }

        @Override
        public DoubleSetting get() {
            return new DoubleSetting(defaultValue, name, description, precision, min, max, changed);
        }
    }
}
