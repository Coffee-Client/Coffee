/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.clickgui;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.FastTickable;
import coffee.client.feature.gui.clickgui.element.Element;
import coffee.client.feature.gui.clickgui.element.impl.CategoryDisplay;
import coffee.client.feature.gui.clickgui.element.impl.ModuleDisplay;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.ConfigContainer;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.font.adapter.FontAdapter;
import coffee.client.helper.manager.ShaderManager;
import coffee.client.helper.render.MSAAFramebuffer;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClickGUI extends Screen implements FastTickable {
    //    public static final Theme theme = new SipoverV1();

    static final Color tooltipColor = new Color(20, 20, 30, 255);
    static final ConfigContainer configContainer = new ConfigContainer(new File(CoffeeMain.BASE, "clickGui.sip"), "amongUs");
    private static ClickGUI instance;
    final List<Element> elements = new ArrayList<>();
    final ParticleRenderer real = new ParticleRenderer(100);
    final double scroll = 0;
    public String searchTerm = "";
    String desc = null;
    double descX, descY;
    double trackedScroll = 0;
    double introAnimation = 0;
    boolean closing = false;

    private ClickGUI() {
        super(Text.of(""));
        initElements();
        Events.registerEventHandler(EventType.HUD_RENDER, event -> {
            if (this.real.particles.isEmpty() || !closing) {
                return;
            }
            this.real.render(Renderer.R3D.getEmptyMatrixStack());
        });
        Events.registerEventHandler(EventType.CONFIG_SAVE, event -> saveConfig());
        loadConfig();
    }

    public static ClickGUI instance() {
        if (instance == null) {
            instance = new ClickGUI();
        }
        return instance;
    }

    public static void reInit() {
        if (instance != null) {
            instance.initElements();
        }
    }

    void loadConfig() {
        configContainer.reload();
        ClickguiConfigContainer cc = configContainer.get(ClickguiConfigContainer.class);
        if (cc == null || cc.entries == null) {
            return;
        }
        Map<String, CategoryDisplay> displays = new HashMap<>();
        for (Element element : elements) {
            if (element instanceof CategoryDisplay dd) {
                displays.put(dd.getMt().getName(), dd);
            }
        }
        for (ClickguiConfigContainer.CategoryEntry entry : cc.entries) {
            String n = entry.name;
            if (displays.containsKey(n)) {
                CategoryDisplay disp = displays.get(n);
                disp.setX(entry.posX);
                disp.setY(entry.posY);
                disp.setOpen(entry.expanded);
                List<ModuleDisplay> mdList = disp.getMd();
                for (ClickguiConfigContainer.ModuleEntry moduleEntry : entry.entries) {
                    ModuleDisplay mde = mdList.stream()
                            .filter(moduleDisplay -> moduleDisplay.getModule().getName().equals(moduleEntry.name))
                            .findFirst()
                            .orElse(null);
                    if (mde == null) {
                        continue;
                    }
                    mde.setExtended(moduleEntry.expanded);
                }
            }
        }
    }

    void saveConfig() {
        ClickguiConfigContainer cc = new ClickguiConfigContainer();
        List<ClickguiConfigContainer.CategoryEntry> e = new ArrayList<>();
        for (Element element : elements) {
            if (element instanceof CategoryDisplay ce) {
                List<ModuleDisplay> mods = ce.getMd();
                ModuleType type = ce.getMt();
                ClickguiConfigContainer.CategoryEntry cm = new ClickguiConfigContainer.CategoryEntry();
                cm.expanded = ce.isOpen();
                cm.posX = ce.getX();
                cm.posY = ce.getY();
                cm.name = type.getName();
                List<ClickguiConfigContainer.ModuleEntry> me = new ArrayList<>();
                for (ModuleDisplay mod : mods) {
                    ClickguiConfigContainer.ModuleEntry moduleEntry = new ClickguiConfigContainer.ModuleEntry();
                    moduleEntry.expanded = mod.isExtended();
                    moduleEntry.name = mod.getModule().getName();
                    me.add(moduleEntry);
                }
                cm.entries = me.toArray(ClickguiConfigContainer.ModuleEntry[]::new);
                e.add(cm);
            }
        }
        cc.entries = e.toArray(ClickguiConfigContainer.CategoryEntry[]::new);
        configContainer.set(cc);
        configContainer.save();
    }

    @Override
    protected void init() {
        //        ShaderManager.blurProgressGoal = 1;
        closing = false;
        introAnimation = 0;
        //        this.real.particles.clear();
        this.real.shouldAdd = true;
    }

    @Override
    public void close() {
        //        ShaderManager.blurProgressGoal = 0;
        closing = true;
        this.real.shouldAdd = false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        for (Element element : elements) {
            if (element.scroll(mouseX, mouseY, amount)) {
                break;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    public void renderDescription(double x, double y, String text) {
        desc = text;
        descX = x;
        descY = y;
    }

    void initElements() {
        elements.clear();
        double width = CoffeeMain.client.getWindow().getScaledWidth();
        double x = 5;
        double y = 5;
        double tallestInTheRoom = 0;
        for (ModuleType value : Arrays.stream(ModuleType.values())
                .sorted(Comparator.comparingLong(value -> -ModuleRegistry.getModules().stream().filter(module -> module.getModuleType() == value).count()))
                .toList()) {
            CategoryDisplay cd = new CategoryDisplay(x, y, value);
            tallestInTheRoom = Math.max(tallestInTheRoom, cd.getHeight());
            x += cd.getWidth() + 5;
            if (x >= width) {
                y += tallestInTheRoom + 5;
                tallestInTheRoom = 0;
                x = 5;
            }
            elements.add(cd);
        }
    }

    double easeInOutQuint(double x) {
        return x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2;

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (closing && introAnimation == 0) {
            Objects.requireNonNull(client).setScreen(null);
            return;
        }
        double intp = easeInOutQuint(introAnimation);
        ShaderManager.BLUR.getEffect().setUniformValue("progress", (float) intp);
        ShaderManager.BLUR.render(delta);
        MSAAFramebuffer.use(MSAAFramebuffer.MAX_SAMPLES, () -> renderIntern(matrices, mouseX, mouseY, delta));
    }

    void renderIntern(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        double wid = width / 2d;
        double hei = height / 2d;
        FontAdapter bigAssFr = FontRenderers.getCustomSize(70);
        double tx = wid - bigAssFr.getStringWidth(searchTerm) / 2d;
        double ty = hei - bigAssFr.getMarginHeight() / 2d;
        bigAssFr.drawString(matrices, searchTerm, (float) tx, (float) ty, 0x50FFFFFF, false);

        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        matrices.push();
        matrices.translate(0, 0, -20);
        if (!closing) {
            real.render(matrices);
        }
        matrices.pop();
        matrices.push();
        double intp = easeInOutQuint(introAnimation);
        matrices.translate((1 - intp) * width / 2, (1 - intp) * height / 2, 0);
        matrices.scale((float) intp, (float) intp, 1);
        matrices.translate(0, -trackedScroll, 0);
        //mouseY1 += trackedScroll;
        List<Element> rev = new ArrayList<>(elements);
        Collections.reverse(rev);
        for (Element element : rev) {
            element.render(matrices, mouseX, mouseY, trackedScroll);
            //matrices.pop();
        }
        matrices.pop();
        super.render(matrices, mouseX, mouseY, delta);
        if (desc != null) {

            //            double width = FontRenderers.getNormal().getStringWidth(desc);
            double width = 0;
            List<String> text = Arrays.stream(desc.split("\n")).map(String::trim).toList();
            for (String s : text) {
                width = Math.max(width, FontRenderers.getRenderer().getStringWidth(s));
            }
            Vec2f root = Renderer.R2D.renderTooltip(matrices, descX, descY, width + 4, FontRenderers.getRenderer().getMarginHeight() + 4, tooltipColor);
            float yOffset = 2;
            for (String s : text) {
                FontRenderers.getRenderer().drawString(matrices, s, root.x + 1, root.y + yOffset, 0xFFFFFF, false);
                yOffset += FontRenderers.getRenderer().getMarginHeight();
            }

            desc = null;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double mouseY1 = mouseY;
        mouseY1 += trackedScroll;
        for (Element element : new ArrayList<>(elements)) {
            if (element.clicked(mouseX, mouseY1, button)) {
                elements.remove(element);
                elements.add(0, element); // put to front when clicked
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY1, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double mouseY1 = mouseY;
        mouseY1 += trackedScroll;
        for (Element element : elements) {
            element.released();
        }
        return super.mouseReleased(mouseX, mouseY1, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        double mouseY1 = mouseY;
        mouseY1 += trackedScroll;
        for (Element element : elements) {
            if (element.dragged(mouseX, mouseY1, deltaX, deltaY, button)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY1, button, deltaX, deltaY);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Element element : elements) {
            if (element.keyPressed(keyCode, modifiers)) {
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && searchTerm.length() > 0) {
            searchTerm = searchTerm.substring(0, searchTerm.length() - 1);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE && !searchTerm.isEmpty()) {
            searchTerm = "";
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void onFastTick() {
        double d = 0.03;
        if (closing) {
            d *= -1;
        }
        introAnimation += d;
        introAnimation = MathHelper.clamp(introAnimation, 0, 1);
        trackedScroll = Transitions.transition(trackedScroll, scroll, 7, 0);
        for (Element element : elements) {
            element.tickAnim();
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (Element element : elements) {
            if (element.charTyped(chr, modifiers)) {
                return true;
            }
        }
        searchTerm += chr;
        return false;
    }

    static class ClickguiConfigContainer {
        CategoryEntry[] entries;

        static class CategoryEntry {
            String name;
            double posX, posY;
            boolean expanded;
            ModuleEntry[] entries;
        }

        static class ModuleEntry {
            String name;
            boolean expanded;
        }
    }
}
