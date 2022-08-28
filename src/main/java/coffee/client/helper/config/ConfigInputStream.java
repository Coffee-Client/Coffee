/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.config;

import coffee.client.feature.config.SettingBase;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

class ConfigInputStream extends DataInputStream {
    /**
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public ConfigInputStream(@NotNull InputStream in) {
        super(in);
    }

    public String getName() throws IOException {
        return readUTF();
    }

    public int getVersion() throws IOException {
        return readInt();
    }

    public void parseConfig() throws IllegalStateException, IOException {
        int amount = readInt();
        for (int i = 0; i < amount; i++) {
            String name = readUTF();
            Module meant = ModuleRegistry.getByName(name);
            if (meant == null) {
                continue;
            }
            boolean enabled = readBoolean();
            System.out.printf("%s is enabled: %s, should be enabled: %s%n", name, meant.isEnabled(), enabled);
            if (meant.isEnabled() != enabled) {
                meant.setEnabled(enabled);
            }
            int configs = readInt();
            for (int i1 = 0; i1 < configs; i1++) {
                String configName = readUTF();
                SettingBase<?> settingBase = meant.config.get(configName);
                if (settingBase == null) {
                    continue;
                }
                String configSave = settingBase.getConfigSave();
                settingBase.deserialize(this);
                System.out.printf("%s: %s: %s -> %s%n", name, configName, configSave, settingBase.getConfigSave());
            }
        }
    }
}
