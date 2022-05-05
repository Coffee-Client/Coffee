/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.gui.clickgui.theme.impl;

import cf.coffee.client.feature.module.ModuleRegistry;
import cf.coffee.client.feature.module.impl.render.Theme;

import java.awt.Color;

public class Custom implements cf.coffee.client.feature.gui.clickgui.theme.Theme {
    final Theme theme = ModuleRegistry.getByClass(Theme.class);

    @Override
    public String getName() {
        return "Custom";
    }

    @Override
    public Color getAccent() {
        return theme.accent.getValue();
    }

    @Override
    public Color getHeader() {
        return theme.header.getValue();
    }

    @Override
    public Color getModule() {
        return theme.module.getValue();
    }

    @Override
    public Color getConfig() {
        return theme.configC.getValue();
    }

    @Override
    public Color getActive() {
        return theme.active.getValue();
    }

    @Override
    public Color getInactive() {
        return theme.inactive.getValue();
    }
}
