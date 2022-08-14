/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.render;

import coffee.client.CoffeeMain;
import coffee.client.mixin.render.IFramebufferMixin;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.EXTPackedDepthStencil;
import org.lwjgl.opengl.GL11;

public enum Stencil {
    INSTANCE;

    public void write() {
        //        checkSetupFBO();
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_STENCIL_TEST);

        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFFFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GlStateManager._colorMask(false, false, false, false);
    }

    public void erase() {
        GL11.glStencilFunc(GL11.GL_NOTEQUAL, 1, 0xFFFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GlStateManager._colorMask(true, true, true, true);
    }

    public void dispose() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    public void checkSetupFBO() {
        Framebuffer fbo = MinecraftClient.getInstance().getFramebuffer();
        if (fbo != null) {
            if (fbo.getDepthAttachment() > -1) {
                setupFBO(fbo);
                IFramebufferMixin ifbo = (IFramebufferMixin) fbo;
                ifbo.setDepthAttachment(-1);
            }
        }
    }

    public void setupFBO(Framebuffer fbo) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.getDepthAttachment());
        int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
        EXTFramebufferObject.glRenderbufferStorageEXT(
                EXTFramebufferObject.GL_RENDERBUFFER_EXT,
                EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT,
                CoffeeMain.client.getWindow().getWidth(),
                CoffeeMain.client.getWindow().getHeight()
        );
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
                EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT,
                EXTFramebufferObject.GL_RENDERBUFFER_EXT,
                stencil_depth_buffer_ID
        );
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
                EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT,
                EXTFramebufferObject.GL_RENDERBUFFER_EXT,
                stencil_depth_buffer_ID
        );
    }
}
