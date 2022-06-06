/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.screen;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.Command;
import coffee.client.feature.command.CommandRegistry;
import coffee.client.feature.gui.DoesMSAA;
import coffee.client.feature.gui.FastTickable;
import coffee.client.feature.gui.screen.base.ClientScreen;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.helper.GameTexture;
import coffee.client.helper.Texture;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.render.ClipStack;
import coffee.client.helper.render.Rectangle;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpotlightScreen extends ClientScreen implements FastTickable {
    CommandTextField command;
    List<SuggestionsEntry> entries = new ArrayList<>();
    double anim = 0;
    double scroll = 0;
    double smoothScroll = 0;
    Rectangle suggestionsField = new Rectangle(0, 0, 0, 0);
    double suggestionsHeight = 0;
    boolean closing = false;
    int selectingIndex = 0;
    boolean useSelectingIndex = false;

    @Override
    protected void init() {
        closing = false;
        double thingWidth = 400;
        int thingFontHeight = 30;
        command = new CommandTextField(FontRenderers.getCustomSize(thingFontHeight), (width - thingWidth) / 2d, 100, thingWidth, thingFontHeight + 5, "Enter command");
        addDrawableChild(command);
        command.setFocused(true);
        setInitialFocus(command);

        super.init();
    }

    void updateActions() {
        String action = command.get();
        ArrayList<SuggestionsEntry> entries = new ArrayList<>(this.entries);
        entries.clear();
        if (!action.isEmpty()) {
            String[] cmdArgs = action.split(" +");
            String firstPart = cmdArgs[0].toLowerCase();
            for (Module module : ModuleRegistry.getModules()) {
                if (module.getName().toLowerCase().startsWith(firstPart)) {
                    entries.add(new SuggestionsEntry(module.getName(), GameTexture.ACTION_TOGGLEMODULE.getWhere(), "Toggle module", () -> {
                        module.toggle();
                        close();
                    }, 0, 0, 0, () -> {
                        command.set(module.getName());
                        command.setCursorMax();
                    }));
                }
            }
            for (Command command1 : CommandRegistry.getCommands()) {
                for (String alias : command1.getAliases()) {
                    if (alias.toLowerCase().startsWith(firstPart)) {
                        entries.add(new SuggestionsEntry(alias + " " + String.join(" ", Arrays.copyOfRange(cmdArgs, 1, cmdArgs.length)), GameTexture.ACTION_RUNCOMMAND.getWhere(), "Run command", () -> {
                            CommandRegistry.execute(alias + " " + String.join(" ", Arrays.copyOfRange(cmdArgs, 1, cmdArgs.length)));
                            close();
                        }, 0, 0, 0, () -> {
                            command.set(alias + " " + String.join(" ", Arrays.copyOfRange(cmdArgs, 1, cmdArgs.length)));
                            command.setCursorMax();
                        }));
                    }
                }
            }
        }
        this.entries = entries;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        double anim = ease(this.anim);
        if (anim == 0 && closing) {
            client.setScreen(null);
        }
        stack.translate(width / 2d * (1 - anim), (command.y + command.height / 2d) * (1 - anim), 0);
        stack.scale((float) anim, (float) anim, 1f);
        command.opacity = 1f;


        double suggestionsHeight = entries.stream()
                .map(suggestionsEntry -> suggestionsEntry.height() + 2)
                .reduce(Double::sum)
                .orElse(0d);
        this.suggestionsHeight = suggestionsHeight;
        suggestionsHeight = Math.min(suggestionsHeight, 200);

        double totalHeight = command.height;
        double pad = 2;
        if (suggestionsHeight > 0) {
            totalHeight += suggestionsHeight + pad;
            this.suggestionsField = new Rectangle(command.x, command.y + command.height + pad, command.x + command.width, command.y + command.height + pad + suggestionsHeight);
        } else {
            this.suggestionsField = new Rectangle(0, 0, 0, 0);
        }
        Renderer.R2D.renderRoundedQuad(stack, new Color(20, 20, 20), command.x, command.y, command.x + command.width, command.y + totalHeight, 5, 20);
        Renderer.R2D.renderRoundedQuad(stack, new Color(30, 30, 30), command.x, command.y, command.x + command.width, command.y + command.height, 5, 20);

        double yOffset = 0;
        stack.push();
        ClipStack.globalInstance.addWindow(stack, suggestionsField);
        stack.translate(0, -smoothScroll, 0);
        int index = 0;
        for (SuggestionsEntry suggestionsEntry : entries) {

            suggestionsEntry.x = command.x + 2;
            suggestionsEntry.y = command.y + command.height + yOffset + pad;
            suggestionsEntry.wid = command.width - 4;
            if (!useSelectingIndex)
                suggestionsEntry.selected = mouseX >= suggestionsEntry.x && mouseX <= suggestionsEntry.x + suggestionsEntry.wid && mouseY + smoothScroll >= suggestionsEntry.y && mouseY + smoothScroll <= suggestionsEntry.y + suggestionsEntry.height();
            else suggestionsEntry.selected = index == selectingIndex;
            suggestionsEntry.render(stack);
            yOffset += suggestionsEntry.height() + 2;
            index++;
        }
        ClipStack.globalInstance.popWindow();
        stack.pop();
        super.renderInternal(stack, mouseX, mouseY, delta);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        selectingIndex = makeSureInBounds(selectingIndex, entries.size());
        useSelectingIndex = true;
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            selectingIndex++;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_UP) {
            selectingIndex--;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
            if (!entries.isEmpty()) entries.get(selectingIndex).onCl.run();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_TAB) {
            for (SuggestionsEntry entry : entries) {
                if (entry.selected) entry.tabcomplete.run();
            }
            return true;
        } else return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseX >= suggestionsField.getX() && mouseX <= suggestionsField.getX1() && mouseY >= suggestionsField.getY() && mouseY <= suggestionsField.getY1()) {
            scroll -= amount * 10;
            double suggestionsWindowHeight = suggestionsField.getY1() - suggestionsField.getY();
            double entitledScroll = suggestionsHeight - suggestionsWindowHeight;
            scroll = MathHelper.clamp(scroll, 0, entitledScroll);
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (mouseX >= suggestionsField.getX() && mouseX <= suggestionsField.getX1() && mouseY >= suggestionsField.getY() && mouseY <= suggestionsField.getY1()) {
            for (SuggestionsEntry suggestionsEntry : entries) {
                suggestionsEntry.clicked(mouseX, mouseY + smoothScroll);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    int makeSureInBounds(int index, int size) {
        int indexCpy = index;
        if (size == 0) {
            return 0;
        }
        indexCpy %= size;
        if (indexCpy < 0) {
            indexCpy = size + indexCpy;
        }
        return indexCpy;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (mouseX >= suggestionsField.getX() && mouseX <= suggestionsField.getX1() && mouseY >= suggestionsField.getY() && mouseY <= suggestionsField.getY1()) {
            useSelectingIndex = false;
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void onFastTick() {
        updateActions();
        selectingIndex = makeSureInBounds(selectingIndex, entries.size());
        smoothScroll = Transitions.transition(smoothScroll, scroll, 7, 0);
        double delta = 0.07;
        if (closing) delta *= -1;
        anim += delta;
        anim = MathHelper.clamp(anim, 0, 1);
    }

    double ease(double x) {
        return x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2;

    }

    @Override
    public void close() {
        closing = true;
    }

    public static class CommandTextField implements Element, Drawable, Selectable, DoesMSAA {
        public final Runnable changeListener = () -> {
        };
        protected final String suggestion;
        final FontAdapter fa;
        public float opacity = 0f;
        protected String text = "";
        protected boolean focused;
        protected int cursor;
        protected double textStart;
        protected int selectionStart, selectionEnd;
        boolean mouseOver = false;
        double x, y, width, height;

        public CommandTextField(FontAdapter fa, double x, double y, double width, double height, String text) {
            this.fa = fa;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.suggestion = text;
        }

        protected double maxTextWidth() {
            return width - pad() * 2 - 1;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            int preSelectionCursor = 0;
            if (selectionStart < preSelectionCursor && preSelectionCursor == selectionEnd) {
                cursor = selectionStart;
            } else if (selectionEnd > preSelectionCursor && preSelectionCursor == selectionStart) {
                cursor = selectionEnd;
            }

            return false;
        }

        @Override
        public boolean keyPressed(int key, int scanCode, int mods) {
            if (!focused) {
                return false;
            }

            boolean control = MinecraftClient.IS_SYSTEM_MAC ? mods == GLFW.GLFW_MOD_SUPER : mods == GLFW.GLFW_MOD_CONTROL;

            if (control && key == GLFW.GLFW_KEY_C) {
                if (cursor != selectionStart || cursor != selectionEnd) {
                    CoffeeMain.client.keyboard.setClipboard(text.substring(selectionStart, selectionEnd));
                }
                return true;
            } else if (control && key == GLFW.GLFW_KEY_X) {
                if (cursor != selectionStart || cursor != selectionEnd) {
                    CoffeeMain.client.keyboard.setClipboard(text.substring(selectionStart, selectionEnd));
                    clearSelection();
                }

                return true;
            } else if (control && key == GLFW.GLFW_KEY_A) {
                cursor = text.length();
                selectionStart = 0;
                selectionEnd = cursor;
            } else if (mods == ((MinecraftClient.IS_SYSTEM_MAC ? GLFW.GLFW_MOD_SUPER : GLFW.GLFW_MOD_CONTROL) | GLFW.GLFW_MOD_SHIFT) && key == GLFW.GLFW_KEY_A) {
                resetSelection();
            } else if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                setFocused(false);
                return true;
            }

            return onKeyRepeated(key, mods);
        }

        public boolean onKeyRepeated(int key, int mods) {
            if (!focused) {
                return false;
            }

            boolean control = MinecraftClient.IS_SYSTEM_MAC ? mods == GLFW.GLFW_MOD_SUPER : mods == GLFW.GLFW_MOD_CONTROL;
            boolean shift = mods == GLFW.GLFW_MOD_SHIFT;
            int isCtrlPressed = SystemUtils.IS_OS_WINDOWS ? GLFW.GLFW_MOD_ALT : MinecraftClient.IS_SYSTEM_MAC ? GLFW.GLFW_MOD_SUPER : GLFW.GLFW_MOD_CONTROL;
            boolean controlShift = mods == (isCtrlPressed | GLFW.GLFW_MOD_SHIFT);
            boolean altShift = mods == ((SystemUtils.IS_OS_WINDOWS ? GLFW.GLFW_MOD_CONTROL : GLFW.GLFW_MOD_ALT) | GLFW.GLFW_MOD_SHIFT);

            if (control && key == GLFW.GLFW_KEY_V) {
                clearSelection();

                String preText = text;
                String clipboard = CoffeeMain.client.keyboard.getClipboard();
                int addedChars = 0;

                StringBuilder sb = new StringBuilder(text.length() + clipboard.length());
                sb.append(text, 0, cursor);

                for (int i = 0; i < clipboard.length(); i++) {
                    char c = clipboard.charAt(i);
                    sb.append(c);
                    addedChars++;
                }

                sb.append(text, cursor, text.length());

                text = sb.toString();
                cursor += addedChars;
                resetSelection();

                if (!text.equals(preText)) {
                    runAction();
                }
                return true;
            } else if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (cursor > 0 && cursor == selectionStart && cursor == selectionEnd) {
                    String preText = text;

                    int count = (mods == isCtrlPressed) ? cursor : (mods == (SystemUtils.IS_OS_WINDOWS ? GLFW.GLFW_MOD_CONTROL : GLFW.GLFW_MOD_ALT)) ? countToNextSpace(true) : 1;

                    text = text.substring(0, cursor - count) + text.substring(cursor);
                    cursor -= count;
                    resetSelection();

                    if (!text.equals(preText)) {
                        runAction();
                    }
                } else if (cursor != selectionStart || cursor != selectionEnd) {
                    clearSelection();
                }

                return true;
            } else {
                boolean ctrl = mods == isCtrlPressed;
                if (key == GLFW.GLFW_KEY_DELETE) {
                    if (cursor < text.length()) {
                        if (cursor == selectionStart && cursor == selectionEnd) {
                            String preText = text;

                            int count = ctrl ? text.length() - cursor : (mods == (SystemUtils.IS_OS_WINDOWS ? GLFW.GLFW_MOD_CONTROL : GLFW.GLFW_MOD_ALT)) ? countToNextSpace(false) : 1;

                            text = text.substring(0, cursor) + text.substring(cursor + count);

                            if (!text.equals(preText)) {
                                runAction();
                            }
                        } else {
                            clearSelection();
                        }
                    }

                    return true;
                } else if (key == GLFW.GLFW_KEY_LEFT) {
                    if (cursor > 0) {
                        if (mods == (SystemUtils.IS_OS_WINDOWS ? GLFW.GLFW_MOD_CONTROL : GLFW.GLFW_MOD_ALT)) {
                            cursor -= countToNextSpace(true);
                            resetSelection();
                        } else if (ctrl) {
                            cursor = 0;
                            resetSelection();
                        } else if (altShift) {
                            if (cursor == selectionEnd && cursor != selectionStart) {
                                cursor -= countToNextSpace(true);
                                selectionEnd = cursor;
                            } else {
                                cursor -= countToNextSpace(true);
                                selectionStart = cursor;
                            }
                        } else if (controlShift) {
                            if (cursor == selectionEnd && cursor != selectionStart) {
                                selectionEnd = selectionStart;
                            }
                            selectionStart = 0;

                            cursor = 0;
                        } else if (shift) {
                            if (cursor == selectionEnd && cursor != selectionStart) {
                                selectionEnd = cursor - 1;
                            } else {
                                selectionStart = cursor - 1;
                            }

                            cursor--;
                        } else {
                            if (cursor == selectionEnd && cursor != selectionStart) {
                                cursor = selectionStart;
                            } else {
                                cursor--;
                            }

                            resetSelection();
                        }

                        cursorChanged();
                    } else if (selectionStart != selectionEnd && selectionStart == 0 && mods == 0) {
                        cursor = 0;
                        resetSelection();
                        cursorChanged();
                    }

                    return true;
                } else if (key == GLFW.GLFW_KEY_RIGHT) {
                    if (cursor < text.length()) {
                        if (mods == (SystemUtils.IS_OS_WINDOWS ? GLFW.GLFW_MOD_CONTROL : GLFW.GLFW_MOD_ALT)) {
                            cursor += countToNextSpace(false);
                            resetSelection();
                        } else if (ctrl) {
                            cursor = text.length();
                            resetSelection();
                        } else if (altShift) {
                            if (cursor == selectionStart && cursor != selectionEnd) {
                                cursor += countToNextSpace(false);
                                selectionStart = cursor;
                            } else {
                                cursor += countToNextSpace(false);
                                selectionEnd = cursor;
                            }
                        } else if (controlShift) {
                            if (cursor == selectionStart && cursor != selectionEnd) {
                                selectionStart = selectionEnd;
                            }
                            cursor = text.length();
                            selectionEnd = cursor;
                        } else if (shift) {
                            if (cursor == selectionStart && cursor != selectionEnd) {
                                selectionStart = cursor + 1;
                            } else {
                                selectionEnd = cursor + 1;
                            }

                            cursor++;
                        } else {
                            if (cursor == selectionStart && cursor != selectionEnd) {
                                cursor = selectionEnd;
                            } else {
                                cursor++;
                            }

                            resetSelection();
                        }

                        cursorChanged();
                    } else if (selectionStart != selectionEnd && selectionEnd == text.length() && mods == 0) {
                        cursor = text.length();
                        resetSelection();
                        cursorChanged();
                    }

                    return true;
                }
            }

            return false;
        }


        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            clearSelection();
            this.text = text;
            cursor = this.text.length();
            resetSelection();
            runAction();
        }

        @Override
        public boolean charTyped(char c, int modifiers) {
            if (!focused) {
                return false;
            }

            clearSelection();

            text = text.substring(0, cursor) + c + text.substring(cursor);

            cursor++;
            resetSelection();

            runAction();
            return true;
        }

        boolean inBounds(double cx, double cy) {
            return cx >= x && cx < x + width && cy >= y && cy < y + height;
        }

        @Override
        public void render(MatrixStack stack, int mouseX, int mouseY, float delta) {
            mouseOver = inBounds(mouseX, mouseY);
            double pad = pad();
            double overflowWidth = getOverflowWidthForRender();
            double innerHeight = fa.getFontHeight();
            double centerY = y + height / 2d - innerHeight / 2d;

            ClipStack.globalInstance.addWindow(stack, new Rectangle(x + pad, y, x + width - pad, y + height));
            // Text content
            if (!text.isEmpty()) {
                fa.drawString(stack, text, (float) (x + pad - overflowWidth), (float) (centerY), 1f, 1f, 1f, opacity, false);
            } else {
                fa.drawString(stack, suggestion, (float) (x + pad - overflowWidth), (float) (centerY), 1f, 1f, 1f, opacity, false);
            }

            // Text highlighting
            if (focused && (cursor != selectionStart || cursor != selectionEnd)) {
                double selStart = x + pad + getTextWidth(selectionStart) - overflowWidth;
                double selEnd = x + pad + getTextWidth(selectionEnd) - overflowWidth;
                Renderer.R2D.renderQuad(stack, new Color(50, 50, 255, (int) (100 * opacity)), selStart, centerY, selEnd, centerY + fa.getMarginHeight());
            }
            ClipStack.globalInstance.popWindow();
            boolean renderCursor = (System.currentTimeMillis() % 1000) / 500d > 1;
            if (focused && renderCursor) {
                Renderer.R2D.renderQuad(stack, new Color(1f, 1f, 1f, opacity), x + pad + getTextWidth(cursor) - overflowWidth, centerY, x + pad + getTextWidth(cursor) - overflowWidth + 1, centerY + fa.getMarginHeight());
            }

        }

        private void clearSelection() {
            if (selectionStart == selectionEnd) {
                return;
            }

            String preText = text;

            text = text.substring(0, selectionStart) + text.substring(selectionEnd);

            cursor = selectionStart;
            selectionEnd = cursor;

            if (!text.equals(preText)) {
                runAction();
            }
        }

        private void resetSelection() {
            selectionStart = cursor;
            selectionEnd = cursor;
        }

        private int countToNextSpace(boolean toLeft) {
            int count = 0;
            boolean hadNonSpace = false;

            for (int i = cursor; toLeft ? i >= 0 : i < text.length(); i += toLeft ? -1 : 1) {
                int j = i;
                if (toLeft) {
                    j--;
                }

                if (j >= text.length()) {
                    continue;
                }
                if (j < 0) {
                    break;
                }

                if (hadNonSpace && Character.isWhitespace(text.charAt(j))) {
                    break;
                } else if (!Character.isWhitespace(text.charAt(j))) {
                    hadNonSpace = true;
                }

                count++;
            }

            return count;
        }

        private void runAction() {
            cursorChanged();
            changeListener.run();
        }

        private double textWidth() {
            return fa.getStringWidth(text);
        }

        private void cursorChanged() {
            double cursor = getCursorTextWidth(-2);
            if (cursor < textStart) {
                textStart -= textStart - cursor;
            }

            cursor = getCursorTextWidth(2);
            if (cursor > textStart + maxTextWidth()) {
                textStart += cursor - (textStart + maxTextWidth());
            }

            textStart = MathHelper.clamp(textStart, 0, Math.max(textWidth() - maxTextWidth(), 0));
        }

        protected double getTextWidth(int pos) {
            if (pos < 0) {
                return 0;
            }
            int pos1 = Math.min(text.length(), pos);
            return fa.getStringWidth(text.substring(0, pos1)) + 1;
        }

        protected double getCursorTextWidth(int offset) {
            return getTextWidth(cursor + offset);
        }

        protected double getOverflowWidthForRender() {
            return textStart;
        }

        public String get() {
            return text;
        }

        public void set(String text) {
            this.text = text;

            cursor = MathHelper.clamp(cursor, 0, text.length());
            selectionStart = cursor;
            selectionEnd = cursor;

            cursorChanged();
        }

        public boolean isFocused() {
            return focused;
        }

        public void setFocused(boolean focused) {
            this.focused = focused;

            resetSelection();
        }

        public void setCursorMax() {
            cursor = text.length();
        }

        double pad() {
            return 4;
        }

        @Override
        public SelectionType getType() {
            return mouseOver ? SelectionType.HOVERED : SelectionType.NONE;
        }

        @Override
        public void appendNarrations(NarrationMessageBuilder builder) {

        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseOver) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    if (!text.isEmpty()) {
                        text = "";
                        cursor = 0;
                        selectionStart = 0;
                        selectionEnd = 0;

                        runAction();
                    }
                }

                setFocused(true);
                return true;
            }

            if (focused) {
                setFocused(false);
            }

            return false;
        }
    }

    static class SuggestionsEntry {
        final String text;
        final String title;
        final Texture icon;
        final Runnable onCl;
        final Runnable tabcomplete;
        final double padUpDown = 3;
        public boolean selected = false;
        double x, y, wid;

        public SuggestionsEntry(String text, Texture icon, String title, Runnable onClick, double x, double y, double width, Runnable onTab) {
            this.text = text;
            this.title = title;
            this.icon = icon;
            this.onCl = onClick;
            this.x = x;
            this.y = y;
            this.wid = width;
            this.tabcomplete = onTab;
        }

        public void render(MatrixStack stack) {
            double yCenter = y + height() / 2d;
            double contentSize = height() - padUpDown * 2d;
            if (selected) {
                Renderer.R2D.renderRoundedQuad(stack, new Color(40, 40, 40), x, y, x + wid, y + height(), 5, 20);
            }
            RenderSystem.setShaderTexture(0, icon);
            Renderer.R2D.renderTexture(stack, x + padUpDown, yCenter - contentSize / 2d, contentSize, contentSize, 0, 0, contentSize, contentSize, contentSize, contentSize);
            FontRenderers.getRenderer()
                    .drawString(stack, title, x + padUpDown + contentSize + padUpDown, yCenter - contentSize / 2d, 0xAAAAAA);
            FontRenderers.getRenderer()
                    .drawString(stack, text, x + padUpDown + contentSize + padUpDown, yCenter - contentSize / 2d + FontRenderers.getRenderer()
                            .getMarginHeight(), 0xFFFFFF);
        }

        public double height() {
            return FontRenderers.getRenderer().getFontHeight() * 2 + padUpDown * 2;
        }

        public void clicked(double mx, double my) {
            if (mx >= x && mx <= x + wid && my >= y && my <= y + height()) {
                onCl.run();
            }
        }
    }
}
