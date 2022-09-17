/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.config.annotation;

import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.ColorSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.config.ListSetting;
import coffee.client.feature.config.RangeSetting;
import coffee.client.feature.config.SettingBase;
import coffee.client.feature.config.StringSetting;
import coffee.client.helper.util.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.lang.reflect.Field;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public enum SettingType {
    STRING(String.class, (setting, inputField, defaultValue) -> new StringSetting.Builder((String) inputField.get(defaultValue))),
    DOUBLE(double.class, (setting, inputField, defaultValue) -> {
        Utils.throwIfAnyEquals("Min, max and precision need to be defined", -1, setting.min(), setting.max(), setting.precision());
        return new DoubleSetting.Builder(inputField.getDouble(defaultValue)).min(setting.min()).max(setting.max()).precision(setting.precision());
    }),
    INT(int.class, (setting, inputField, defaultValue) -> {
        Utils.throwIfAnyEquals("Min and max need to be defined", -1, setting.min(), setting.max());
        return new DoubleSetting.Builder(inputField.getDouble(defaultValue)).min(setting.min()).max(setting.max()).precision(0);
    }),
    BOOLEAN(boolean.class, (setting, inputField, defaultValue) -> new BooleanSetting.Builder(inputField.getBoolean(defaultValue))),
    @SuppressWarnings("rawtypes") ENUM(Enum.class, (setting, inputField, defaultValue) -> new EnumSetting.Builder((Enum<?>) inputField.get(defaultValue))),
    COLOR(Color.class, (setting, inputField, declaringClass) -> new ColorSetting.Builder((Color) inputField.get(declaringClass))),
    RANGE(RangeSetting.Range.class, (setting, inputField, declaringClass) -> {
        Utils.throwIfAnyEquals("Min, max and precision need to be defined", -1, setting.min(), setting.max(), setting.precision());
        double minA = setting.min();
        double maxA = setting.max();
        double minB = setting.upperMin();
        double maxB = setting.upperMax();
        if (minB == -1) {
            minB = minA;
        }
        if (maxB == -1) {
            maxB = maxA;
        }
        return new RangeSetting.Builder((RangeSetting.Range) inputField.get(declaringClass)).lowerMin(minA)
            .lowerMax(maxA)
            .upperMin(minB)
            .upperMax(maxB)
            .precision(setting.precision());
    }),
    MULTIVALUE(ListSetting.FlagSet.class, (setting, inputField, declaringClass) -> new ListSetting.Builder<>(((ListSetting.FlagSet<?>) inputField.get(declaringClass))));
    @Getter
    final Class<?> acceptedType;
    @Getter
    final SettingProvider<?> provider;

    interface SettingProvider<T extends SettingBase.Builder<?, ?, ?>> {
        default T getExtern(Setting setting, Field inputField, Object declaringClass) throws Exception {
            return get(setting, inputField, declaringClass);
        }

        T get(Setting setting, Field inputField, Object declaringClass) throws Exception;
    }
}
