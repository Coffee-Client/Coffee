/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.screen.base;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.FastTickable;
import coffee.client.helper.manager.ShaderManager;
import coffee.client.helper.render.MSAAFramebuffer;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public class ClientScreen extends Screen implements FastTickable {
    final int samples;
    final Screen parent;
    double expandProgress = 0;
    boolean closing = false;

    public ClientScreen(int samples, Screen parent) {
        super(Text.of(""));
        this.samples = samples;
        this.parent = parent;
    }

    public ClientScreen(Screen parent) {
        this(MSAAFramebuffer.MAX_SAMPLES, parent);
    }

    public ClientScreen() {
        this(null);
    }

    public ClientScreen(int samples) {
        this(samples, null);
    }

    public void renderInternal(MatrixStack stack, int mouseX, int mouseY, float delta) {
        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public void onFastTick() {
        double delta = parent == null ? 1 : 0.015;
        if (closing) {
            delta *= -1;
        }
        expandProgress += delta;
        expandProgress = MathHelper.clamp(expandProgress, 0, 1);
    }

    @Override
    protected void init() {
        closing = false;
        super.init();
    }

    @Override
    public void close() {
        closing = true;
    }

    protected void renderParentWithMask(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (parent == null) {
            return;
        }
        Renderer.R2D.renderQuad(matrices, new Color(20, 20, 20), 0, 0, width, height);
        float aTotal = ((float) Transitions.easeOutExpo(expandProgress)) * 2;
        //        float aTotal
        float a = MathHelper.clamp(aTotal, 0, 1);
        float a1 = MathHelper.clamp(aTotal - 1, 0, 1);
        //        float a = 0;
        //        float a1 = 1;
        matrices.push();
        double roundSx = MathHelper.lerp(a, -5, (1 - 0.9) / 2 * width);
        double roundSy = MathHelper.lerp(a, -5, (1 - .9) / 2 * height);
        double sx = Math.max(roundSx, 0);
        double sy = Math.max(roundSy, 0);
        double newWidth = width - sx * 2;
        double newHeight = height - sy * 2;
        float rel = (float) (newWidth / width);
        float relH = (float) (newHeight / height);
        matrices.translate(sx, sy, 0);
        matrices.scale(rel, relH, 1);


        ShaderManager.drawToMasking(() -> {
            if (parent instanceof ClientScreen cs) {
                cs.renderInternal(matrices, mouseX, mouseY, delta);
            } else {
                parent.render(matrices, mouseX, mouseY, delta);
            }
        });
        matrices.pop();
        ShaderManager.drawToMask(() -> {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            Renderer.R2D.renderRoundedQuadInternal(matrices.peek().getPositionMatrix(),
                    1f,
                    1f,
                    1f,
                    1f,
                    roundSx,
                    roundSy,
                    width - roundSx,
                    height - roundSy,
                    5,
                    20
            );
        });
        ShaderManager.BRUH.render(delta);
        ShaderManager.BLUR.getEffect().setUniformValue("progress", a);
        ShaderManager.BLUR.render(delta);
        matrices.translate(0, (1 - a1) * height, 0);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        CoffeeMain.client.keyboard.setRepeatEvents(true);
        renderParentWithMask(matrices, mouseX, mouseY, delta);
        if (closing && expandProgress == 0) {
            this.client.setScreen(parent);
        }
        if (samples != -1) {
            if (!MSAAFramebuffer.framebufferInUse()) {
                MSAAFramebuffer.use(samples, () -> renderInternal(matrices, mouseX, mouseY, delta));
            } else {
                renderInternal(matrices, mouseX, mouseY, delta);
            }
        } else {
            renderInternal(matrices, mouseX, mouseY, delta);
        }
    }
}
