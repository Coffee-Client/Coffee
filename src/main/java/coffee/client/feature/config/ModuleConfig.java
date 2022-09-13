/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.config;

import coffee.client.feature.config.annotation.AnnotationParser;

import java.util.ArrayList;
import java.util.List;

public class ModuleConfig {
    final List<SettingBase<?>> settings = new ArrayList<>();

    public <S extends SettingBase<?>> S create(S in) { // used as a proxy to make a one liner
        settings.add(in);
        return in;
    }

    public SettingBase<?> get(String name) {
        for (SettingBase<?> setting : getSettings()) {
            if (setting.getName().equals(name)) {
                return setting;
            }
        }
        return null;
    }

    public List<SettingBase<?>> getSettings() {
        return settings;
    }

    public void addSettingsFromAnnotations(Object moduleInstance) {
        try {
            new AnnotationParser(moduleInstance, this).runParse();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
