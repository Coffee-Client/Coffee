/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.config;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.SettingBase;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import lombok.SneakyThrows;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

// Format: <amount><<name><enabled><config length><<config name><config value>>>
public class ConfigOutputStream extends DataOutputStream {
    String name;

    public ConfigOutputStream(OutputStream stream, String name) {
        super(stream);
        this.name = name;
    }

    @SneakyThrows
    public void writeConfigValue(SettingBase<?> base) {
        writeUTF(base.name);
        base.serialize(this);
    }

    @SneakyThrows
    public void writeModule(Module module) {
        writeUTF(module.getName());
        writeBoolean(module.isEnabled());
        List<SettingBase<?>> settings = new ArrayList<>(module.config.getSettings()).stream().filter(settingBase -> !settingBase.getValue().equals(settingBase.getDefaultValue())).toList();
        int size = settings.size();
        writeInt(size);
        for (SettingBase<?> setting : settings) {
            writeConfigValue(setting);
        }
    }

    @SneakyThrows
    public void writeModules(List<Module> modules) {
        writeInt(modules.size());
        for (Module module : modules) {
            writeModule(module);
        }
    }

    public boolean isModuleChanged(Module m) {
        if (m.isEnabled()) {
            return true; // is enabled - different from default
        }
        for (SettingBase<?> setting : m.config.getSettings()) {
            if (!setting.getValue().equals(setting.getDefaultValue())) {
                return true; // value different
            }
        }
        return false;
    }

    @SneakyThrows
    public void writeHeader() {
        writeInt(CoffeeMain.getClientVersion());
        writeUTF(name);
    }

    public void write() {
        writeHeader();
        writeModules(ModuleRegistry.getModules());
    }
}
