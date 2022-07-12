package coffee.client.feature.gui.clickgui;

import coffee.client.feature.gui.clickgui.element.CategoryDisplay;
import coffee.client.feature.gui.screen.base.AAScreen;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.font.FontRenderers;
import coffee.client.helper.manager.ShaderManager;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import coffee.client.helper.util.Utils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;

public class ClickGUI extends AAScreen {
    private static ClickGUI instance;
    boolean initialized = false;
    boolean closing = false;
    double progress = 0;

    double tooltipX, tooltipY;
    String tooltipContent;

    public static void reInit() {
        if (instance != null) {
            instance.initWidgets();
        }
    }

    public static ClickGUI instance() {
        if (instance == null) {
            instance = new ClickGUI();
        }
        return instance;
    }

    public void setTooltip(String content) {
        tooltipX = Utils.Mouse.getMouseX();
        tooltipY = Utils.Mouse.getMouseY() + 10;
        tooltipContent = content;
    }

    @Override
    protected void init() {
        initInternal(); // do not clear the elements on the way in
    }

    @Override
    protected void initInternal() {
        closing = false;
        if (initialized) {
            return;
        }
        initialized = true;
        initWidgets();
    }

    public void initWidgets() {
        clearWidgets();
        double x = 5;
        for (ModuleType value : ModuleType.values()) {
            CategoryDisplay gd = new CategoryDisplay(value, x, 5, 100);
            x += gd.getWidth() + 5;
            addChild(gd);
        }
    }

    @Override
    public void close() {
        closing = true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onFastTick() {
        double delta = 0.04;
        if (closing) {
            delta *= -1;
        }
        progress += delta;
        progress = MathHelper.clamp(progress, 0, 1);

        super.onFastTick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean b = iterateOverChildren(element -> element.keyPressed(keyCode, modifiers));
        if (b) {
            return true;
        }
        if (keyCode == 256) {
            closing = true;
            return true;
        }
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (closing && progress == 0) {
            super.close();
        }
        double interpolated = Transitions.easeOutExpo(progress);
        ShaderManager.BLUR.getEffect().setUniformValue("progress", (float) interpolated);
        ShaderManager.BLUR.render(delta);
        matrices.push();
        matrices.scale((float) interpolated, (float) interpolated, 1);
        super.render(matrices, mouseX, mouseY, delta);
        matrices.pop();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        super.renderInternal(stack, mouseX, mouseY, delta);
        if (tooltipContent != null) {
            String[] split = tooltipContent.split("\n");
            double height = FontRenderers.getRenderer().getFontHeight() * split.length + 2;
            double width = Arrays.stream(split)
                    .map(s -> FontRenderers.getRenderer().getStringWidth(s))
                    .max(Comparator.comparingDouble(value -> value))
                    .orElse(0f) + 4f;

            Renderer.R2D.renderRoundedQuadWithShadow(stack, new Color(30, 30, 30), tooltipX, tooltipY, tooltipX + width, tooltipY + height, 2, 6);
            double y = 0;
            for (String s : split) {
                FontRenderers.getRenderer().drawString(stack, s, tooltipX + 2, tooltipY + 1 + y, 0xFFFFFF);
                y += FontRenderers.getRenderer().getFontHeight();
            }

            tooltipContent = null;
        }
    }
}
