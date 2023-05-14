/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

public class MoreChatHistory extends Module {
    final DoubleSetting size = this.config.create(new DoubleSetting.Builder(300).name("Size")
        .description("How big the new chat history should be allowed to get (vanilla is 100)")
        .min(10)
        .max(1000)
        .precision(0)
        .get());

    public MoreChatHistory() {
        super("MoreChatHistory", "Allows you to change the size of the chat history", ModuleType.MISC);
    }

    public int getHistSize() {
        return (int) (size.getValue() + 0);
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
}
