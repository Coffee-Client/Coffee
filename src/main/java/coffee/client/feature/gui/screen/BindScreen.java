/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.gui.screen;

import coffee.client.feature.gui.screen.base.ClientScreen;
import coffee.client.feature.module.Module;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public class BindScreen extends ClientScreen {
    final Module a;
    final FontAdapter cfr = FontRenderers.getCustomSize(30);
    final FontAdapter smaller = FontRenderers.getCustomSize(20);
    long closeAt = -1;

    public BindScreen(Module toBind) {
        this.a = toBind;
    }

    @Override
    public void renderInternal(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        cfr.drawCenteredString(matrices, "Press any key", width / 2d, height / 2d - cfr.getMarginHeight(), 0xAAAAAA);
        String kn = a.keybind.getValue() > 0 ? GLFW.glfwGetKeyName((int) (a.keybind.getValue() + 0),
                GLFW.glfwGetKeyScancode((int) (a.keybind.getValue() + 0))
        ) : "None";
        if (kn == null) {
            try {
                for (Field declaredField : GLFW.class.getDeclaredFields()) {
                    if (declaredField.getName().startsWith("GLFW_KEY_")) {
                        int a = (int) declaredField.get(null);
                        if (a == this.a.keybind.getValue()) {
                            String nb = declaredField.getName().substring("GLFW_KEY_".length());
                            kn = nb.substring(0, 1).toUpperCase() + nb.substring(1).toLowerCase();
                        }
                    }
                }
            } catch (Exception ignored) {
                kn = "unknown." + (int) (a.keybind.getValue() + 0);
            }
        }
        smaller.drawCenteredString(matrices, "Current bind: " + kn, width / 2d, height / 2d, 0xBBBBBB);
        super.renderInternal(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int keyCodeCpy = keyCode;
        if (closeAt != -1) {
            return false;
        }
        if (keyCodeCpy == GLFW.GLFW_KEY_ESCAPE) {
            keyCodeCpy = -1;
        }
        a.keybind.setValue((double) keyCodeCpy);
        closeAt = System.currentTimeMillis() + 500;
        return true;
    }

    @Override
    public void tick() {
        if (closeAt != -1 && closeAt < System.currentTimeMillis()) {
            close();
        }
        super.tick();
    }
}
