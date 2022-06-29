/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.render.Renderer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class Test extends Module {
    static VertexBuffer vbo;

    public Test() {
        super("Test", "Testing stuff with the client, can be ignored", ModuleType.MISC);
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {
        vbo.close();
        vbo = null;
    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        if (vbo == null) {
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

            Random r = new Random();
            for (int i = 0; i < 65535; i++) {
                double x = r.nextDouble(-20, 21);
                double y = r.nextDouble(-20, 21);
                double z = r.nextDouble(-20, 21);
                buffer.vertex(x, y, z).color(1f, 1f, 1f, .3f).next();
            }

            vbo = new VertexBuffer();
            vbo.bind();
            vbo.upload(buffer.end());
            VertexBuffer.unbind();
        }

        Renderer.setupRender();
        matrices.push();
        Camera c = CoffeeMain.client.gameRenderer.getCamera();
        Vec3d camPos = c.getPos();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        vbo.bind();
        vbo.draw(
                matrices.peek().getPositionMatrix(),
                CoffeeMain.client.gameRenderer.getBasicProjectionMatrix(CoffeeMain.client.options.getFov().getValue()),
                GameRenderer.getPositionColorShader()
        );
        VertexBuffer.unbind();
        matrices.pop();
        Renderer.endRender();

    }

    @Override
    public void onHudRender() {

    }

    @Override
    public void tick() {

    }
}
