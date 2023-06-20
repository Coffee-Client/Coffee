/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.gui.screen.base;

import coffee.client.helper.render.MSAAFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ClientScreen extends Screen {
    final int samples;

    public ClientScreen(int samples) {
        super(Text.of(""));
        this.samples = samples;
    }

    public ClientScreen() {
        this(MSAAFramebuffer.MAX_SAMPLES);
    }

    public void renderInternal(DrawContext stack, int mouseX, int mouseY, float delta) {
        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        if (samples != -1) {
            if (!MSAAFramebuffer.framebufferInUse()) {
                MSAAFramebuffer.use(() -> renderInternal(matrices, mouseX, mouseY, delta));
            } else {
                renderInternal(matrices, mouseX, mouseY, delta);
            }
        } else {
            renderInternal(matrices, mouseX, mouseY, delta);
        }
    }
}
