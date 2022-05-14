/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.render;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.config.BooleanSetting;
import cf.coffee.client.feature.config.DoubleSetting;
import cf.coffee.client.feature.config.EnumSetting;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.helper.render.Renderer;
import cf.coffee.client.helper.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ESP extends Module {
    public final EnumSetting<Mode> outlineMode = this.config.create(new EnumSetting.Builder<>(Mode.Filled).name("Outline mode").description("How to render the outline").get());
    public final BooleanSetting entities = this.config.create(new BooleanSetting.Builder(false).name("Show entities").description("Render entities").get());
    public final BooleanSetting players = this.config.create(new BooleanSetting.Builder(true).name("Show players").description("Render players").get());
    public final List<double[]> vertexDumps = new ArrayList<>();
    final DoubleSetting range = this.config.create(new DoubleSetting.Builder(64).name("Range").description("How far to render the entities").min(32).max(128).precision(1).get());
    public boolean recording = false;

    public ESP() {
        super("ESP", "Shows where entities are", ModuleType.RENDER);
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return null;
    }

    public boolean shouldRenderEntity(Entity e) {
        return ((e instanceof PlayerEntity && players.getValue()) || entities.getValue());
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        if (outlineMode.getValue() == Mode.Model) {
            float alpha = 1f;
            List<double[]> vertBuffer = new ArrayList<>();
            List<double[][]> verts = new ArrayList<>();
            for (double[] vertexDump : vertexDumps) {
                if (vertexDump.length == 0) {
                    verts.add(vertBuffer.toArray(double[][]::new));
                    vertBuffer.clear();
                } else {
                    vertBuffer.add(vertexDump);
                }
            }
            verts.add(vertBuffer.toArray(double[][]::new));
            vertBuffer.clear();
            vertexDumps.clear();

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            double p;
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            for (double[][] vert : verts) {


                for (double[] vertexDump : vert) {
                    p = (((/*vertexDump[0]+vertexDump[1]+*/vertexDump[2]) % 10) / 10 + (System.currentTimeMillis() % 2000) / 2000d) % 1;
                    int col = Color.HSBtoRGB((float) p, .6f, 1f);
                    float red = (col >> 16 & 0xFF) / 255f;
                    float green = (col >> 8 & 0xFF) / 255f;
                    float blue = (col & 0xFF) / 255f;
                    buffer.vertex(vertexDump[0], vertexDump[1], vertexDump[2]).color(red, green, blue, alpha).next();
                }

            }
            buffer.end();
            BufferRenderer.draw(buffer);

            GL11.glDepthFunc(GL11.GL_LEQUAL);
            RenderSystem.disableBlend();
            return;
        }
        if (CoffeeMain.client.world == null || CoffeeMain.client.player == null) {
            return;
        }

        for (Entity entity : CoffeeMain.client.world.getEntities()) {
            if (entity.squaredDistanceTo(CoffeeMain.client.player) > Math.pow(range.getValue(), 2)) {
                continue;
            }
            if (entity.getUuid().equals(CoffeeMain.client.player.getUuid())) {
                continue;
            }
            if (shouldRenderEntity(entity)) {
                Color c = Utils.getCurrentRGB();
                Vec3d eSource = Utils.getInterpolatedEntityPosition(entity);
                switch (outlineMode.getValue()) {
                    case Filled ->
                            Renderer.R3D.renderFilled(eSource.subtract(new Vec3d(entity.getWidth(), 0, entity.getWidth()).multiply(0.5)), new Vec3d(entity.getWidth(), entity.getHeight(), entity.getWidth()), Renderer.Util.modify(c, -1, -1, -1, 130), matrices);
                    case Rect -> renderOutline(entity, c, matrices);
                    case Outline ->
                            Renderer.R3D.renderOutline(eSource.subtract(new Vec3d(entity.getWidth(), 0, entity.getWidth()).multiply(0.5)), new Vec3d(entity.getWidth(), entity.getHeight(), entity.getWidth()), Renderer.Util.modify(c, -1, -1, -1, 130), matrices);
                    case Shader -> renderShaderOutline(entity, matrices);
                }
            }
        }
    }

    void renderShaderOutline(Entity e, MatrixStack stack) {
        Vec3d origin = Utils.getInterpolatedEntityPosition(e);
        float w = e.getWidth();
        float h = e.getHeight();

        Vec3d[] boxPoints = new Vec3d[] { origin.add(-w / 2d, 0, -w / 2d), origin.add(-w / 2d, 0, w / 2d), origin.add(w / 2d, 0, w / 2d), origin.add(w / 2d, 0, -w / 2d), origin.add(-w / 2d, h, -w / 2d), origin.add(-w / 2d, h, w / 2d), origin.add(w / 2d, h, w / 2d), origin.add(w / 2d, h, -w / 2d), };
        Vec3d[] screenSpace = new Vec3d[boxPoints.length];
        for (int i = 0; i < boxPoints.length; i++) {
            Vec3d boxP = boxPoints[i];
            screenSpace[i] = Renderer.R2D.getScreenSpaceCoordinate(boxP, stack);
        }
        Vec3d leastX = screenSpace[0];
        Vec3d mostX = screenSpace[0];
        Vec3d leastY = screenSpace[0];
        Vec3d mostY = screenSpace[0];
        for (Vec3d vec3d : screenSpace) {
            if (!Renderer.R2D.isOnScreen(vec3d)) return;
            if (vec3d.x < leastX.x) leastX = vec3d;
            if (vec3d.x > mostX.x) mostX = vec3d;
            if (vec3d.y < leastY.y) leastY = vec3d;
            if (vec3d.y > mostY.y) mostY = vec3d;
        }
        Vec3d finalLeastX = leastX;
        Vec3d finalLeastY = leastY;
        Vec3d finalMostX = mostX;
        Vec3d finalMostY = mostY;
        Utils.TickManager.runOnNextRender(() -> {
            //Renderer.R2D.renderRoundedQuad(Renderer.R3D.getEmptyMatrixStack(),new Color(255,255,255,100),finalLeastX.x,finalLeastY.y,finalMostX.x,finalMostY.y,5,20);
            float x1 = (float) finalLeastX.x;
            float y1 = (float) finalLeastY.y;
            float x2 = (float) finalMostX.x;
            float y2 = (float) finalMostY.y;
            Matrix4f matrix = stack.peek().getPositionMatrix();
            float r = 1f;
            float g = 1f;
            float b = 1f;
            float a = 1f;
            float desiredHeight = (float) ((finalMostY.y - finalLeastY.y) / 3f);
            float desiredWidth = (float) ((finalMostX.x - finalLeastX.x) / 3f);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            Renderer.setupRender();

            renderCorner(bufferBuilder, matrix, r, g, b, a, x1, y1, desiredHeight, desiredWidth, 1, 1);
            renderCorner(bufferBuilder, matrix, r, g, b, a, x2, y1, desiredHeight, desiredWidth, -1, 1);
            renderCorner(bufferBuilder, matrix, r, g, b, a, x2, y2, desiredHeight, desiredWidth, -1, -1);
            renderCorner(bufferBuilder, matrix, r, g, b, a, x1, y2, desiredHeight, desiredWidth, 1, -1);


            Renderer.endRender();

        });
    }

    void renderCorner(BufferBuilder bb, Matrix4f matrix, float r, float g, float b, float a, float x, float y, float height, float topWidth, float xMul, float yMul) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bb.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        float width = 1;
        //float height = 50;
        //float topWidth = 30;
        /*
        4---------5
        |         |
        |    1----6
        |    |
        |    |
        3----2
        */
        //matrix.multiply(new Quaternion(0,0,(float) rotation,true));
        float[][] verts = new float[][] { new float[] { 0, 0 }, new float[] { 0, height }, new float[] { -width, height }, new float[] { -width, -width }, new float[] { topWidth, -width }, new float[] { topWidth, 0 }, new float[] { 0, 0 } };
        //double rad = Math.toRadians(rotation);
        //double sin = Math.sin(rad);
        //double cos = Math.cos(rad);
        //double sin = .5;
        //double cos = 1;
        for (float[] vert : verts) {

            bb.vertex(matrix, x + vert[0] * xMul, y + vert[1] * yMul, 0f).color(r, g, b, a).next();
        }
        //bb.vertex(matrix,x,y,0f).color(r,g,b,a).next();
        //bb.vertex(matrix,x,y+height,0).color(r,g,b,a).next();
        //bb.vertex(matrix,x-width,y+height,0).color(r,g,b,a).next();
        //bb.vertex(matrix,x-width,y-width,0).color(r,g,b,a).next();
        //bb.vertex(matrix,x+topWidth,y-width,0).color(r,g,b,a).next();
        //bb.vertex(matrix,x+topWidth,y,0).color(r,g,b,a).next();
        //bb.vertex(matrix,x,y,0f).color(r,g,b,a).next();
        bb.end();
        BufferRenderer.draw(bb);
    }

    @Override
    public void onHudRender() {

    }

    void renderOutline(Entity e, Color color, MatrixStack stack) {
        Vec3d eSource = new Vec3d(MathHelper.lerp(CoffeeMain.client.getTickDelta(), e.prevX, e.getX()), MathHelper.lerp(CoffeeMain.client.getTickDelta(), e.prevY, e.getY()), MathHelper.lerp(CoffeeMain.client.getTickDelta(), e.prevZ, e.getZ()));
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;
        Camera c = CoffeeMain.client.gameRenderer.getCamera();
        Vec3d camPos = c.getPos();
        Vec3d start = eSource.subtract(camPos);
        float x = (float) start.x;
        float y = (float) start.y;
        float z = (float) start.z;

        double r = Math.toRadians(-c.getYaw() + 90);
        float sin = (float) (Math.sin(r) * (e.getWidth() / 1.7));
        float cos = (float) (Math.cos(r) * (e.getWidth() / 1.7));
        stack.push();

        Matrix4f matrix = stack.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix, x + sin, y, z + cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin, y, z - cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin, y, z - cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin, y + e.getHeight(), z - cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin, y + e.getHeight(), z - cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin, y + e.getHeight(), z + cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin, y + e.getHeight(), z + cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin, y, z + cos).color(red, green, blue, alpha).next();

        buffer.end();

        BufferRenderer.draw(buffer);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
        stack.pop();
    }

    public enum Mode {
        Filled, Rect, Outline, Model, Shader
    }
}
