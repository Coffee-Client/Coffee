/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.feature.module.NoNotificationDefault;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

@NoNotificationDefault
public class ClickGUI extends Module {
    public BooleanSetting skipAnimation = this.config.create(new BooleanSetting.Builder(false).name("Skip animation")
            .description("Skips the animation sequence for the clickgui")
            .get());
    int t = 2;

    public ClickGUI() {
        super("ClickGUI", "A visual manager for all modules", ModuleType.RENDER);
        this.keybind.accept(GLFW.GLFW_KEY_RIGHT_SHIFT + "");
    }

    @Override
    public void tick() {
        t--;
        if (t == 0) {
            CoffeeMain.client.setScreen(coffee.client.feature.gui.clickgui.ClickGUI.instance());
            setEnabled(false);
        }
    }

    @Override
    public void enable() {
        t = 2;
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
}
