/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.gui.screen;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.gui.FastTickable;
import cf.coffee.client.feature.gui.notifications.Notification;
import cf.coffee.client.feature.gui.widget.RoundButton;
import cf.coffee.client.feature.gui.widget.RoundTextFieldWidget;
import cf.coffee.client.helper.NbtFormatter;
import cf.coffee.client.helper.font.FontRenderers;
import cf.coffee.client.helper.render.ClipStack;
import cf.coffee.client.helper.render.Rectangle;
import cf.coffee.client.helper.render.Renderer;
import cf.coffee.client.helper.util.Transitions;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class NbtEditorScreen extends ClientScreen implements FastTickable {
    final ItemStack stack;
    final List<String> initial = new ArrayList<>();
    final char[] suffixes = { 'b', 's', 'L', 'f', 'd' };
    final char[][] appendPairs = { { '"', '"' }, { '{', '}' }, { '\'', '\'' }, { '[', ']' } };
    int editorX = 0;
    int editorY = 0;
    double scrollX = 0;
    double smoothScrollX = 0;
    double scroll = 0;
    double smoothScroll = 0;
    RoundTextFieldWidget search;
    boolean skipAppend = false;

    public NbtEditorScreen(ItemStack stack) {
        super();
        this.stack = stack;
        NbtCompound compound = this.stack.getOrCreateNbt();
        NbtFormatter.RGBColorText formatted = new NbtFormatter("  ", 0, Lists.newArrayList()).apply(compound);
        StringBuilder current = new StringBuilder();
        for (NbtFormatter.RGBColorText.RGBEntry entry : formatted.getEntries()) {
            if (entry == NbtFormatter.RGBColorText.NEWLINE) {
                initial.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(entry.value());
            }
        }
        initial.add(current.toString());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    boolean isNumber(String t) {
        try {
            Long.parseLong(t);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    void format() {
        String nbtString = String.join("\n", initial);
        try {
            NbtCompound nc = StringNbtReader.parse(nbtString);
            NbtFormatter.RGBColorText formatted = new NbtFormatter("  ", 0, Lists.newArrayList()).apply(nc);
            StringBuilder current = new StringBuilder();
            initial.clear();
            for (NbtFormatter.RGBColorText.RGBEntry entry : formatted.getEntries()) {
                if (entry == NbtFormatter.RGBColorText.NEWLINE) {
                    initial.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(entry.value());
                }
            }
            initial.add(current.toString());
        } catch (Exception e) {
            client.setScreen(new NotificationScreen(this, "Invalid JSON!", Notification.Type.ERROR));
        }
    }

    void save() {
        String nbtString = String.join("\n", initial);
        try {
            NbtCompound nc = StringNbtReader.parse(nbtString);
            this.stack.setNbt(nc);
            this.close();
        } catch (Exception e) {
            client.setScreen(new NotificationScreen(this, "Invalid JSON!", Notification.Type.ERROR));
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (mouseX > 5 && mouseX < width - 5 && mouseY > 5 && mouseY < height - 30) {
            scroll -= deltaY;
            scrollX -= deltaX;
            mouseScrolled(0, 0, 0);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX > 5 && mouseX < width - 5 && mouseY > 5 && mouseY < height - 30) {
            double relativeX = mouseX - 5 + smoothScrollX;
            double relativeY = mouseY - 5 + smoothScroll;
            int aboutIndex = (int) Math.floor(relativeY / FontRenderers.getMono().getMarginHeight());
            if (aboutIndex >= 0 && aboutIndex < initial.size() && relativeX > 0) {
                String p = initial.get(aboutIndex);
                double wid = FontRenderers.getMono().getStringWidth(p);
                String t = FontRenderers.getMono().trimStringToWidth(p, relativeX);
                editorY = aboutIndex;
                editorX = relativeX < wid ? t.length() - 1 : t.length();
            }

        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void init() {
        RoundButton format = new RoundButton(RoundButton.STANDARD, 5, height - 30 + 5, 64, 20, "Format", this::format);
        addDrawableChild(format);
        RoundButton save = new RoundButton(RoundButton.SUCCESS, 5 + format.getWidth() + 5, height - 30 + 5, 64, 20, "Save", this::save);
        addDrawableChild(save);
        search = new RoundTextFieldWidget(width - 5 - 160, height - 30 + 5, 160, 20, "Search...");
        addDrawableChild(search);
    }

    int getColor(String total, int index, char c) {
        int amountOfSingles = 0;
        int amountOfDoubles = 0;
        for (int i = 0; i < index; i++) {
            if (total.charAt(i) == '"' && (i == 0 || total.charAt(i - 1) != '\\')) {
                amountOfDoubles++;
            }
            if (total.charAt(i) == '\'' && (i == 0 || total.charAt(i - 1) != '\\')) {
                amountOfSingles++;
            }
        }
        boolean inString = amountOfDoubles % 2 == 1;
        boolean inSingleString = amountOfSingles % 2 == 1;
        if (c == '"') {
            if (!(index > 0 && total.charAt(index - 1) == '\\')) {
                if (total.indexOf('"', index + 1) == -1 && !inString) {
                    return 0xFF5555;
                }
            }
        }
        if (c == '\'') {
            if (!(index > 0 && total.charAt(index - 1) == '\\')) {
                if (total.indexOf('\'', index + 1) == -1 && !inSingleString) {
                    return 0xFF5555;
                }
            }
        }
        if (inString || inSingleString || c == '"' || c == '\'') {
            return 0x55FF55;
        }
        // if the index of the next : from where we are right now is smaller than the index of the last , or the last , is beyond where we are now
        // and the next : from where we are right now is beyond where we are right now, mark it
        if ((total.indexOf(':', index) < total.lastIndexOf(',') || total.lastIndexOf(',') < index) && total.indexOf(' ', index) > total.indexOf(':', index) && total.indexOf(':', index) > index) {
            return 0x55FFFF;
        }
        boolean isSuffix = false;
        for (char suffix : suffixes) {
            if (suffix == c) {
                isSuffix = true;
                break;
            }
        }
        if (isSuffix && index > 0 && isNumber(total.charAt(index - 1) + "")) {
            return 0xFF5555;
        }
        return 0xFFFFFF;
    }

    @Override
    public void onFastTick() {
        this.smoothScroll = Transitions.transition(smoothScroll, scroll, 7, 0);
        this.smoothScrollX = Transitions.transition(smoothScrollX, scrollX, 7, 0);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        double contentWidth = initial.stream().map(s -> FontRenderers.getMono().getStringWidth(s)).max(Comparator.comparingDouble(value -> value)).orElse(0f);
        double windowWidth = width - 14;
        double entitledX = contentWidth - windowWidth;
        entitledX = Math.max(0, entitledX);

        double contentHeight = initial.size() * FontRenderers.getMono().getMarginHeight();
        double windowHeight = height - 37; // calc padding
        double entitledScroll = contentHeight - windowHeight;
        entitledScroll = Math.max(0, entitledScroll);

        if (InputUtil.isKeyPressed(CoffeeMain.client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            scrollX -= amount * 10;
        } else {
            scroll -= amount * 10;
        }
        scrollX = MathHelper.clamp(scrollX, 0, entitledX);
        scroll = MathHelper.clamp(scroll, 0, entitledScroll);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    double getEditorXPosition() {
        String i = initial.get(editorY);
        return 7 + FontRenderers.getMono().getStringWidth(i.substring(0, editorX)) - smoothScrollX;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (isPaste(keyCode)) {
            String clip = CoffeeMain.client.keyboard.getClipboard();
            skipAppend = true;
            for (char c : clip.toCharArray()) {
                if (c == '\n') {
                    keyPressed(GLFW.GLFW_KEY_ENTER, 0, 0);
                } else {
                    charTyped(c, 0);
                }
            }
            skipAppend = false;
        } else {
            switch (keyCode) {
                case GLFW.GLFW_KEY_RIGHT -> editorX += 1;
                case GLFW.GLFW_KEY_LEFT -> editorX -= 1;
                case GLFW.GLFW_KEY_UP -> editorY -= 1;
                case GLFW.GLFW_KEY_DOWN -> editorY += 1;
                case GLFW.GLFW_KEY_TAB -> {
                    int whitespacesStart = 0;
                    String index = initial.get(editorY);
                    for (char c : Arrays.copyOfRange(index.toCharArray(), 0, editorX)) {
                        if (c == ' ') whitespacesStart++;
                        else break;
                    }
                    whitespacesStart %= 2;
                    int missing = 2 - whitespacesStart;
                    StringBuilder sb = new StringBuilder(index);
                    sb.insert(editorX, " ".repeat(missing));
                    editorX += 2;
                    initial.set(editorY, sb.toString());
                }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    if (editorX == 0 && editorY == 0) {
                        break;
                    }
                    String index = initial.get(editorY);
                    if (editorX > 0) {
                        StringBuilder sb = new StringBuilder(index);
                        sb.deleteCharAt(editorX - 1);
                        initial.set(editorY, sb.toString());
                        editorX--;
                    } else {
                        initial.remove(editorY);
                        editorY--;
                        String b = initial.get(editorY);
                        editorX = b.length();
                        initial.set(editorY, b + index);
                    }
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    String index = initial.get(editorY);
                    if (editorY == initial.size() - 1 && editorX == index.length()) {
                        break;
                    }
                    if (editorX < index.length()) {
                        StringBuilder sb = new StringBuilder(index);
                        sb.deleteCharAt(editorX);
                        initial.set(editorY, sb.toString());
                    } else {
                        initial.remove(editorY);
                        String b = initial.get(editorY + 1);
                        initial.set(editorY, index + b);
                    }
                }
                case GLFW.GLFW_KEY_END -> {
                    String index = initial.get(editorY);
                    editorX = index.length();
                }
                case GLFW.GLFW_KEY_HOME -> editorX = 0;
                case GLFW.GLFW_KEY_ENTER -> {
                    String previous = initial.get(editorY);
                    int indent = 0;

                    for (int i = 0; i <= editorY; i++) {
                        String p = initial.get(i);
                        String trimmed = p;
                        while (trimmed.endsWith(",")) {
                            trimmed = trimmed.substring(0, trimmed.length() - 1);
                        }
                        if (p.endsWith("{") || p.endsWith("[")) {
                            indent += 2;
                        } else if (trimmed.endsWith("}") || trimmed.endsWith("]")) {
                            indent -= 2;
                        }
                    }
                    indent = Math.max(0, indent);
                    if (editorX < previous.length()) {
                        String overtake = previous.substring(editorX);
                        initial.set(editorY, previous.substring(0, editorX));
                        editorY++;
                        initial.add(editorY, " ".repeat(indent) + overtake);
                    } else {
                        char[] control = { '{', '[', ',', '.' };
                        boolean isControl = false;
                        for (char c : control) {
                            if (c == previous.charAt(previous.length() - 1)) {
                                isControl = true;
                                break;
                            }
                        }
                        if (!previous.trim().isEmpty() && !isControl) {
                            previous = previous + ",";
                        }
                        initial.set(editorY, previous);
                        editorY++;
                        initial.add(editorY, " ".repeat(indent));
                    }
                    editorX = indent;
                }
            }
        }

        editorY = MathHelper.clamp(editorY, 0, initial.size() - 1);
        String index = initial.get(editorY);
        editorX = MathHelper.clamp(editorX, 0, index.length());
        if (getEditorXPosition() < 7) {
            scrollX += getEditorXPosition() - 7;
        } else if (getEditorXPosition() > width - 14) {
            scrollX -= (width - 14) - getEditorXPosition();
        }
        double editorY = 7 + this.editorY * FontRenderers.getMono().getMarginHeight() - smoothScroll;
        if (editorY < 7) {
            scroll += editorY - 7;
        } else if (editorY > height - 37) {
            scroll -= (height - 37) - editorY;
        }
        mouseScrolled(0, 0, 0);

        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        boolean b = super.charTyped(chr, modifiers);
        if (b) {
            return true;
        }
        String index = initial.get(editorY);
        StringBuilder sb = new StringBuilder(index);
        sb.insert(editorX, chr);
        if (!skipAppend) {
            for (char[] appendPair : appendPairs) {
                if (appendPair[0] == chr) {
                    sb.insert(editorX + 1, appendPair[1]);
                }
            }
        }
        initial.set(editorY, sb.toString());
        editorX++;
        return true;
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        Renderer.R2D.renderRoundedQuad(stack, new Color(20, 20, 20, 200), 5, 5, width - 5, height - 30, 5, 20);
        ClipStack.globalInstance.addWindow(stack, new Rectangle(5, 5, width - 5, height - 30));
        //Renderer.R2D.beginScissor(stack, 5, 5, width - 5, height - 30);
        double initY = 7 - smoothScroll;

        double initX = 7 - smoothScrollX;
        double x = initX;
        double y = initY;
        for (String s : initial) {
            // only render when in bounds
            if (!(y < 7 - FontRenderers.getMono().getMarginHeight() || y > height - 30)) {
                int searchLen = search.get().length();
                if (searchLen > 0) {
                    int currentResult = s.toLowerCase().indexOf(search.get().toLowerCase());
                    if (currentResult != -1) {
                        Renderer.R2D.renderQuad(stack, new Color(0x50AB5909, true), 5, y, width - 5, y + FontRenderers.getMono().getMarginHeight());
                    }
                    while (currentResult >= 0) {
                        double paddingX = FontRenderers.getMono().getStringWidth(s.substring(0, currentResult)) + 7 - smoothScrollX;
                        double markedTextWidth = FontRenderers.getMono().getStringWidth(s.substring(currentResult, currentResult + searchLen)) + 2;
                        Renderer.R2D.renderRoundedQuad(stack, new Color(0xAB5907), paddingX, y, paddingX + markedTextWidth, y + FontRenderers.getMono().getMarginHeight(), 2, 10);
                        currentResult = s.toLowerCase().indexOf(search.get().toLowerCase(), currentResult + 1);
                    }
                }
                for (int i = 0; i < s.toCharArray().length; i++) {
                    //                    boolean isInSearch = indexOfSearch != -1 && i >= indexOfSearch && i < indexOfSearch+searchLen;
                    char c = s.charAt(i);
                    double cw = FontRenderers.getMono().getStringWidth(c + "");
                    //                    double cw = 8;
                    if (x > 5 - cw && x < width - 5) {
                        int color = getColor(s, i, c);
                        //                        int color = 0xFFFFFF;
                        FontRenderers.getMono().drawString(stack, c + "", x, y, color);
                    } else if (x > width - 5) {
                        break;
                    }
                    x += cw;
                }
            }
            x = initX;
            y += FontRenderers.getMono().getMarginHeight();
        }
        editorY = MathHelper.clamp(editorY, 0, initial.size() - 1);
        String index = initial.get(editorY);
        editorX = MathHelper.clamp(editorX, 0, index.length());
        String before = index.substring(0, editorX);
        double cx = FontRenderers.getMono().getStringWidth(before) + initX + 0.5;
        double cy = FontRenderers.getMono().getMarginHeight() * editorY + initY;
        //Renderer.R2D.endScissor();
        ClipStack.globalInstance.popWindow();
        Renderer.R2D.renderQuad(stack, Color.WHITE, cx, cy, cx + 1, cy + FontRenderers.getMono().getMarginHeight());
        super.renderInternal(stack, mouseX, mouseY, delta);
    }
}
