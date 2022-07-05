package coffee.client.helper.manager;

import coffee.client.CoffeeMain;
import coffee.client.helper.render.shader.Shader;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.lwjgl.opengl.GL11;

public class ShaderManager {
    public static final Shader BLUR = Shader.create("blur", managedShaderEffect -> managedShaderEffect.setUniformValue("radius", 5f));
    public static final Shader FROSTED_GLASS_BLUR = Shader.create("frosted_glass", managedShaderEffect -> managedShaderEffect.setUniformValue("Range", 24f));
    public static SimpleFramebuffer maskBuffer, maskingBuffer;
    public static final Shader BRUH = Shader.create("masking_test", managedShaderEffect -> {
        managedShaderEffect.setSamplerUniform("Mask", getMaskBuffer());
        managedShaderEffect.setSamplerUniform("Masking", getMaskingBuffer());
    });

    public static SimpleFramebuffer getMaskingBuffer() {
        if (maskingBuffer == null) {
            Framebuffer mainBuffer = CoffeeMain.client.getFramebuffer();
            maskingBuffer = new SimpleFramebuffer(mainBuffer.textureWidth, mainBuffer.textureHeight, true, true);
        }
        return maskingBuffer;
    }

    public static SimpleFramebuffer getMaskBuffer() {
        if (maskBuffer == null) {
            Framebuffer mainBuffer = CoffeeMain.client.getFramebuffer();
            maskBuffer = new SimpleFramebuffer(mainBuffer.textureWidth, mainBuffer.textureHeight, true, true);

        }
        return maskBuffer;
    }

    public static void drawToMasking(Runnable drawAction) {
        RenderSystem.assertOnRenderThreadOrInit();
        Framebuffer mainBuffer = CoffeeMain.client.getFramebuffer();
        getMaskingBuffer(); // ensure buffer is there
        maskingBuffer.resize(mainBuffer.textureWidth, mainBuffer.textureHeight, true);
        maskingBuffer.clear(true);

        maskingBuffer.beginWrite(true);
        maskingBuffer.setTexFilter(GL11.GL_LINEAR);
        drawAction.run();
        maskingBuffer.endWrite();

        mainBuffer.beginWrite(false);
    }

    public static void drawToMask(Runnable drawAction) {
        RenderSystem.assertOnRenderThreadOrInit();
        Framebuffer mainBuffer = CoffeeMain.client.getFramebuffer();
        getMaskBuffer(); // ensure buffer is there
        maskBuffer.resize(mainBuffer.textureWidth, mainBuffer.textureHeight, true);
        maskBuffer.clear(true);


        maskBuffer.beginWrite(true);
        maskBuffer.setTexFilter(GL11.GL_LINEAR);
        drawAction.run();
        maskBuffer.endWrite();

        mainBuffer.beginWrite(false);
    }
}
