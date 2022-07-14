/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.config;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of settings
 */
public class SettingsGroup {
    final String name;
    final String description;
    final List<SettingBase<?>> settings;

    SettingsGroup(String name, String description, List<SettingBase<?>> settings) {
        this.name = name;
        this.description = description;
        this.settings = settings;
    }

    public List<SettingBase<?>> getSettings() {
        return settings;
    }

    public static class Builder {
        final List<SettingBase<?>> s = new ArrayList<>();
        String name = "none", description = "";

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String desc) {
            this.description = desc;
            return this;
        }

        public Builder settings(SettingBase<?>... settings) {
            s.addAll(List.of(settings));
            return this;
        }

        public SettingsGroup get() {
            return new SettingsGroup(name, description, s);
        }
    }
}
