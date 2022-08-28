/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.config;

import coffee.client.feature.config.SettingBase;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

@Getter
public class ConfigInputFile {
    File file;
    String name = "Parsing error";
    int version = -1;
    private byte[] configContents = new byte[0];

    public ConfigInputFile(File file) {
        this.file = file;
        this.parse();
    }

    protected void parse() {
        try (FileInputStream fis = new FileInputStream(file); DataInputStream data = new ConfigInputStream(fis)) {
            this.version = data.readInt();
            this.name = data.readUTF();
            this.configContents = data.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void apply() {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(this.configContents);
            DataInputStream data = new ConfigInputStream(bis)) {
            int amount = data.readInt();
            for (int i = 0; i < amount; i++) {
                String name = data.readUTF();
                Module meant = ModuleRegistry.getByName(name);
                if (meant == null) {
                    continue;
                }
                boolean enabled = data.readBoolean();
                System.out.printf("%s is enabled: %s, should be enabled: %s%n", name, meant.isEnabled(), enabled);
                if (meant.isEnabled() != enabled) {
                    meant.setEnabled(enabled);
                }
                int configs = data.readInt();
                for (int i1 = 0; i1 < configs; i1++) {
                    String configName = data.readUTF();
                    SettingBase<?> settingBase = meant.config.get(configName);
                    if (settingBase == null) {
                        continue;
                    }
                    String configSave = settingBase.getConfigSave();
                    settingBase.deserialize(data);
                    System.out.printf("%s: %s: %s -> %s%n", name, configName, configSave, settingBase.getConfigSave());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
