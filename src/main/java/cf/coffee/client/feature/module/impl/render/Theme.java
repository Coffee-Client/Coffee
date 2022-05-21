/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.render;

import cf.coffee.client.feature.config.ColorSetting;
import cf.coffee.client.feature.config.EnumSetting;
import cf.coffee.client.feature.config.SettingBase;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class Theme extends Module {
    public final ColorSetting accent = this.config.create(new ColorSetting.Builder(new Color(0x3AD99D)).name("Accent")
            .description("The accent color")
            .get());
    public final ColorSetting header = this.config.create(new ColorSetting.Builder(new Color(0xFF1D2525, true)).name("Header")
            .description("The header color")
            .get());
    public final ColorSetting module = this.config.create(new ColorSetting.Builder(new Color(0xFF171E1F, true)).name("Module")
            .description("The module color")
            .get());
    public final ColorSetting configC = this.config.create(new ColorSetting.Builder(new Color(0xFF111A1A, true)).name("Config")
            .description("The config section color")
            .get());
    public final ColorSetting active = this.config.create(new ColorSetting.Builder(new Color(21, 157, 204, 255)).name("Active")
            .description("The active color")
            .get());
    public final ColorSetting inactive = this.config.create(new ColorSetting.Builder(new Color(66, 66, 66, 255)).name("Inactive")
            .description("The inactive color")
            .get());
    public final EnumSetting<Mode> modeSetting = this.config.create(new EnumSetting.Builder<>(Mode.Ocean).name("Theme")
            .description("Which preset theme to use")
            .get());

    public Theme() {
        super("Theme", "Allows you to edit the client's appearance", ModuleType.RENDER);
        for (SettingBase<?> settingBase : new SettingBase<?>[] { accent, header, module, configC, active, inactive }) {
            settingBase.showIf(() -> modeSetting.getValue() == Mode.Custom);
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }

    public enum Mode {
        Custom, Midnight, Ocean
    }
}
