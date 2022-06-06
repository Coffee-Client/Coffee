/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.clickgui.element.impl.config;

import coffee.client.feature.config.DoubleSetting;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.lang.reflect.Field;

public class KeybindEditor extends ConfigBase<DoubleSetting> {
    static final double h = FontRenderers.getRenderer().getFontHeight() + 2;
    boolean selecting = false;
    boolean cancelNextCharTyped = false;

    public KeybindEditor(double x, double y, double width, DoubleSetting configValue) {
        super(x, y, width, h, configValue);
    }

    @Override
    public double getHeight() {
        return h;
    }

    @Override
    public boolean clicked(double x, double y, int button) {
        if (inBounds(x, y)) {
            selecting = !selecting;
            return true;
        }
        return false;
    }

    @Override
    public boolean dragged(double x, double y, double deltaX, double deltaY, int button) {
        return false;
    }

    @Override
    public boolean released() {
        return false;
    }

    //    long lastUpdate = System.currentTimeMillis();
    @Override
    public boolean keyPressed(int keycode, int modifiers) {
        int keycode1 = keycode;
        if (selecting) {
            //            lastUpdate = System.currentTimeMillis();
            cancelNextCharTyped = true;
            if (keycode1 == GLFW.GLFW_KEY_ESCAPE) {
                selecting = false;
                return true;
            }
            if (keycode1 == GLFW.GLFW_KEY_BACKSPACE) {
                keycode1 = -1;
            }
            configValue.setValue(keycode1 + 0d);
            selecting = false;
            return true;
        }
        return false;
    }

    @Override
    public void render(MatrixStack matrices, double mouseX, double mouseY, double scrollBeingUsed) {
        String keyName;
        int keybind = getKeybind();
        if (selecting) {
            keyName = "...";
        } else if (keybind == -1) {
            keyName = "None";
        } else {
            keyName = GLFW.glfwGetKeyName(keybind, GLFW.glfwGetKeyScancode(keybind));
            if (keyName == null) {
                try {
                    for (Field declaredField : GLFW.class.getDeclaredFields()) {
                        if (declaredField.getName().startsWith("GLFW_KEY_")) {
                            int a = (int) declaredField.get(null);
                            if (a == keybind) {
                                String nb = declaredField.getName().substring("GLFW_KEY_".length());
                                keyName = nb.substring(0, 1).toUpperCase() + nb.substring(1).toLowerCase();
                            }
                        }
                    }
                } catch (Exception ignored) {
                    keyName = "unknown." + keybind;
                }
            } else {
                keyName = keyName.toUpperCase();
            }
        }
        Renderer.R2D.renderRoundedQuad(matrices, new Color(40, 40, 40), x, y, x + width, y + h, 5, 20);
        FontRenderers.getRenderer()
                .drawCenteredString(matrices,
                        keyName,
                        x + width / 2d,
                        y + h / 2d - FontRenderers.getRenderer().getMarginHeight() / 2d,
                        1f,
                        1f,
                        1f,
                        1f);
    }

    int getKeybind() {
        return (int) (configValue.getValue() + 0);
    }

    @Override
    public void tickAnim() {

    }


    @Override
    public boolean charTyped(char c, int mods) {
        // this gets triggered right after keyPressed so i cant really prevent it unless i do this cursed shit
        if (cancelNextCharTyped) {
            cancelNextCharTyped = false;
            return true;
        }
        return false;
    }
}
