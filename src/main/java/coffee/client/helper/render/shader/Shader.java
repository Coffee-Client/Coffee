/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.render.shader;

import coffee.client.CoffeeMain;
import coffee.client.mixinUtil.ShaderEffectDuck;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Shader {
    @Getter
    final PostEffectProcessor shader;
    int previousWidth, previousHeight;

    @SneakyThrows
    private Shader(Identifier ident, Consumer<Shader> init) {
        this.shader = new PostEffectProcessor(CoffeeMain.client.getTextureManager(), CoffeeMain.client.getResourceManager(), CoffeeMain.client.getFramebuffer(), ident);
        checkUpdateDimensions();
        init.accept(this);
    }

    public static Shader create(String progName, Consumer<Shader> callback) {
        return new Shader(new Identifier("coffee", String.format("shaders/post/%s.json", progName)), callback);
    }

    void checkUpdateDimensions() {
        int currentWidth = CoffeeMain.client.getWindow().getFramebufferWidth();
        int currentHeight = CoffeeMain.client.getWindow().getFramebufferHeight();
        if (previousWidth != currentWidth || previousHeight != currentHeight) {
            this.shader.setupDimensions(currentWidth, currentHeight);
            previousWidth = currentWidth;
            previousHeight = currentHeight;
        }
    }

    public void setUniformf(String name, float value) {
        List<PostEffectPass> passes = ((ShaderEffectDuck) shader).coffee_getPasses();
        passes.stream().map(postEffectPass -> postEffectPass.getProgram().getUniformByName(name)).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(value));
    }

    public void render(float delta) {
        checkUpdateDimensions();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.resetTextureMatrix();
        shader.render(delta);
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        RenderSystem.disableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // restore blending
        RenderSystem.enableDepthTest();

    }
}
