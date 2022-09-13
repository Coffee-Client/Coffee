/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.function.Consumer;

public class RangeSetting extends SettingBase<RangeSetting.Range> {
    @Getter
    final int precision;
    @Getter
    final double min, min1;
    @Getter
    final double max, max1;

    public RangeSetting(Range defaultValue, String name, String description, int precision, double min, double max, double min1, double max1, List<Consumer<Range>> onChanged) {
        super(defaultValue, name, description, onChanged);
        this.precision = precision;
        this.min = min;
        this.max = max;
        this.min1 = min1;
        this.max1 = max1;
    }

    @Override
    @SneakyThrows
    public void serialize(DataOutputStream stream) {
        stream.writeDouble(this.getValue().min);
        stream.writeDouble(this.getValue().max);
    }

    @Override
    @SneakyThrows
    public void deserialize(DataInputStream stream) {
        this.setValue(new Range(stream.readDouble(), stream.readDouble()));
    }

    @Override
    public Range parse(String value) {
        try {
            String[] sep = value.split(":");
            double min = Double.parseDouble(sep[0]);
            double max = Double.parseDouble(sep[1]);
            return new Range(min, max);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    @Override
    public String getConfigSave() {
        return getValue().getMin() + ":" + getValue().getMax();
    }

    @Override
    public void setValue(Range value) {
        if (value.min < this.min || value.min > this.max || value.max < this.min1 || value.max > this.max1) {
            return;
        }
        super.setValue(value);
    }

    @AllArgsConstructor
    @ToString
    public static class Range {
        @Getter
        double min, max;
    }

    public static class Builder extends SettingBase.Builder<coffee.client.feature.config.RangeSetting.Builder, Range, RangeSetting> {
        double min = Double.NEGATIVE_INFINITY, max = Double.POSITIVE_INFINITY;
        double min1 = Double.NEGATIVE_INFINITY, max1 = Double.POSITIVE_INFINITY;
        int precision = 2;

        public Builder(Range defaultValue) {
            super(defaultValue);
        }

        public coffee.client.feature.config.RangeSetting.Builder precision(int precision) {
            this.precision = precision;
            return this;
        }

        public coffee.client.feature.config.RangeSetting.Builder lowerMin(double min) {
            this.min = min;
            return this;
        }

        public coffee.client.feature.config.RangeSetting.Builder lowerMax(double max) {
            this.max = max;
            return this;
        }

        public RangeSetting.Builder upperMin(double min1) {
            this.min1 = min1;
            return this;
        }

        public RangeSetting.Builder upperMax(double max1) {
            this.max1 = max1;
            return this;
        }

        public Builder uniformMin(double min) {
            return this.lowerMin(min).upperMin(min);
        }

        public Builder uniformMax(double max) {
            return this.lowerMax(max).upperMax(max);
        }

        @Override
        public RangeSetting get() {
            return new RangeSetting(defaultValue, name, description, precision, min, max, min1, max1, changed);
        }
    }
}
