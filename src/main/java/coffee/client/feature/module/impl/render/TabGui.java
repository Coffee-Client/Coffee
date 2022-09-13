/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.feature.gui.theme.ThemeManager;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.KeyboardEvent;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import lombok.Data;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.function.BooleanSupplier;

public class TabGui extends Module {
    @Getter
    final Stack<TabPane> tabStack = new Stack<>();

    public TabGui() {
        super("TabGui", "Renders a small module manager top left", ModuleType.RENDER);
        Events.registerEventHandler(EventType.KEYBOARD, event -> {
            if (!this.isEnabled()) {
                return;
            }
            KeyboardEvent me = (KeyboardEvent) event;
            handleMouse(me);
        }, 0);
    }

    @Override
    public void onFastTick() {
        for (TabPane tabPane : tabStack) {
            tabPane.smoothCursor = Transitions.transition(tabPane.smoothCursor, tabPane.cursor, 7, 0);
        }
    }

    @Override
    public void postInit() {
        if (tabStack.isEmpty()) {
            TabPane tbp = new TabPane();
            for (ModuleType value : Arrays.stream(ModuleType.values()).filter(moduleType -> moduleType != ModuleType.HIDDEN).toList()) {
                GuiEntry ge = new GuiEntry(value.getName(), () -> false, () -> {
                    TabPane modules = new TabPane();
                    for (Module module : ModuleRegistry.getModules()) {
                        if (module.getModuleType() != value) {
                            continue;
                        }
                        GuiEntry ge1 = new GuiEntry(module.getName(),
                            module::isEnabled,
                            module::toggle,
                            tabStack::pop,
                            FontRenderers.getRenderer().getStringWidth(module.getName()),
                            FontRenderers.getRenderer().getMarginHeight());
                        modules.entries.add(ge1);
                    }
                    if (modules.entries.isEmpty()) {
                        return;
                    }
                    tabStack.add(modules);
                }, () -> {
                }, FontRenderers.getRenderer().getStringWidth(value.getName()), FontRenderers.getRenderer().getMarginHeight());
                tbp.entries.add(ge);
            }
            tabStack.add(tbp);
        }
    }

    int makeSureInBounds(int index, int size) {
        int index1 = index;
        index1 %= size;
        if (index1 < 0) {
            index1 = size + index1;
        }
        return index1;
    }

    void handleMouse(KeyboardEvent me) {
        if (me.getType() == 0) {
            return;
        }
        if (tabStack.isEmpty()) {
            return;
        }
        TabPane tbp = tabStack.peek();
        switch (me.getKeycode()) {
            case GLFW.GLFW_KEY_DOWN -> tbp.cursor++;
            case GLFW.GLFW_KEY_UP -> tbp.cursor--;
            case GLFW.GLFW_KEY_RIGHT -> tbp.entries.get(tbp.cursor).onClick().run();
            case GLFW.GLFW_KEY_LEFT -> tbp.entries.get(tbp.cursor).onClose().run();
        }
        tbp.cursor = makeSureInBounds(tbp.cursor, tbp.entries.size());
    }

    @Override
    public void onHudRender() {
    }

    public void render(MatrixStack stack) {
        if (!this.isEnabled()) {
            return;
        }
        for (TabPane tabPane : tabStack) {
            GuiEntry widest = tabPane.entries.stream().max(Comparator.comparingDouble(value -> value.width)).orElseThrow();
            double padOuter = 2;
            double scrollerWidth = 1.5;
            double yOffset = padOuter;
            double oneHeight = FontRenderers.getRenderer().getMarginHeight();
            double scrollerYOffset = tabPane.smoothCursor * oneHeight;
            double scrollerYEnd = (tabPane.smoothCursor + 1) * oneHeight;
            double height = tabPane.entries.size() * oneHeight + padOuter * 2;

            double width = padOuter + scrollerWidth + 2 + Math.ceil(widest.width + 1) + 3;
            Renderer.R2D.renderRoundedQuadWithShadow(stack, ThemeManager.getMainTheme().getConfig(), 0, 0, width, height, 3, 20);
            Renderer.R2D.renderRoundedQuad(stack, new Color(60, 60, 60), padOuter, yOffset + scrollerYOffset, width - padOuter, yOffset + scrollerYEnd, 2, 10);

            double lastEnabledStackHeight = 0;
            double lastEnabledStackY = 0;
            double yOff = yOffset;
            for (int i = 0; i <= tabPane.entries.size(); i++) {
                GuiEntry ge = i >= tabPane.entries.size() ? null : tabPane.entries.get(i);
                if (ge != null && ge.isEnabled.getAsBoolean()) {
                    if (lastEnabledStackHeight == 0) {
                        lastEnabledStackY = yOff;
                    }
                    lastEnabledStackHeight += oneHeight;
                } else {
                    if (lastEnabledStackHeight != 0) {
                        Renderer.R2D.renderRoundedQuad(stack,
                            ThemeManager.getMainTheme().getAccent(),
                            scrollerWidth + padOuter,
                            lastEnabledStackY + 1,
                            scrollerWidth + 1.5 + padOuter,
                            lastEnabledStackY + lastEnabledStackHeight - 1,
                            0.75,
                            5);
                    }
                    lastEnabledStackHeight = 0;
                    lastEnabledStackY = 0;
                }
                yOff += oneHeight;
            }
            for (GuiEntry entry : tabPane.entries) {
                FontRenderers.getRenderer()
                    .drawString(stack,
                        entry.text,
                        scrollerWidth + padOuter + 2 + (entry.isEnabled.getAsBoolean() ? scrollerWidth : 0),
                        yOffset,
                        entry.isEnabled.getAsBoolean() ? 0xFFFFFF : 0xBBBBBB);
                yOffset += oneHeight;
            }
            stack.translate(width + 5, 0, 0); // x offset
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

    @Data
    public static class TabPane {
        int cursor = 0;
        double smoothCursor = 0;
        List<GuiEntry> entries = new ArrayList<>();
    }

    record GuiEntry(String text, BooleanSupplier isEnabled, Runnable onClick, Runnable onClose, double width, double height) {
    }


}
