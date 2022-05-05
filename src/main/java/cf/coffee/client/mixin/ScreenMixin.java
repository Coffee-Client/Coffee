/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import cf.coffee.client.feature.gui.HasSpecialCursor;
import cf.coffee.client.helper.render.Cursor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenMixin extends AbstractParentElement {
    private static final Color c = new Color(10, 10, 10);
    @Shadow
    public int height;

    @Shadow
    public int width;

    @Shadow
    @Final
    private List<Element> children;

    @Shadow
    protected abstract void insertText(String text, boolean override);

    @Inject(method = "renderBackgroundTexture", at = @At("HEAD"), cancellable = true)
    void real(int vOffset, CallbackInfo ci) {
        float r = c.getRed() / 255f;
        float g = c.getGreen() / 255f;
        float b = c.getBlue() / 255f;
        ci.cancel();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(0.0D, this.height, 0.0D).color(r, g, b, 1f).next();
        bufferBuilder.vertex(this.width, this.height, 0.0D).color(r, g, b, 1f).next();
        bufferBuilder.vertex(this.width, 0.0D, 0.0D).color(r, g, b, 1f).next();
        bufferBuilder.vertex(0.0D, 0.0D, 0.0D).color(r, g, b, 1f).next();
        tessellator.draw();
    }

    @Override
    public List<? extends Element> children() { // have to do this because java will shit itself when i dont overwrite this
        return this.children;
    }

    void shadow_handleCursor(double x, double y) {
        long c = Cursor.STANDARD;
        for (Element child : this.children) {
            if (child instanceof HasSpecialCursor specialCursor) {
                if (specialCursor.shouldApplyCustomCursor()) c = specialCursor.getCursor();
            }
        }
        Cursor.setGlfwCursor(c);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        shadow_handleCursor(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }
}
