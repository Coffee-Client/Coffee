/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.config;

import java.util.ArrayList;
import java.util.List;

public class ModuleConfig {
    final List<SettingBase<?>> settings = new ArrayList<>();
    final List<SettingsGroup> groups = new ArrayList<>();

    public <S extends SettingBase<?>> S create(S in) { // used as a proxy to make a one liner
        settings.add(in);
        return in;
    }

    public SettingsGroup create(SettingsGroup in) {
        groups.add(in);
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

    public List<SettingsGroup> getGroups() {
        return groups;
    }
}
