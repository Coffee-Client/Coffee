/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.gui.clickgui;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.clickgui.theme.Theme;
import coffee.client.feature.gui.clickgui.theme.ThemeManager;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Transitions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ParticleRenderer {
    static final Color DYING = new Color(255, 255, 255, 0); // it goes gradient so you can still see the white
    public final List<Particle> particles = new ArrayList<>();
    final int pc;
    public boolean shouldAdd = true;
    long lastTick = System.currentTimeMillis();

    public ParticleRenderer(int pc) {
        this.pc = pc;
        for (int i = 0; i < pc; i++) {
            addParticle();
        }
    }

    void addParticle() {
        if (!shouldAdd) {
            return;
        }
        Particle n = new Particle();
        n.x = CoffeeMain.client.getWindow().getScaledWidth() * Math.random();
        n.y = CoffeeMain.client.getWindow().getScaledHeight() * Math.random();
        //        n.velY = (Math.random() - .5) / 4;
        n.velX = (Math.random() - .5) / 4;
        n.circleRad = Math.random() * 2;
        particles.add(n);
    }

    private void tick() {
        lastTick = System.currentTimeMillis();
        for (Particle particle : particles) {
            particle.move();
        }
        particles.removeIf(Particle::isDead);
    }

    public void render(MatrixStack stack) {
        long timeDiffSinceLastTick = System.currentTimeMillis() - lastTick;
        int iter = (int) Math.floor(timeDiffSinceLastTick / 20d);
        for (int i = 0; i < iter; i++) {
            tick();
        }
        if (particles.size() < this.pc) {
            addParticle();
        }
        for (Particle particle : particles) {
            particle.render(particles, stack);
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    static class Particle {
        final long origLife = (long) (20 * MathHelper.lerp(Math.random(), 4, 6));
        double velX = 0;
        double x = 0;
        double y = 0;
        double velY = 0;
        double accelX = 0;
        double accelY = -0.1;
        long life = origLife;
        double circleRad = 1.5;

        public static BufferBuilder renderPrepare(Color color) {
            float red = color.getRed() / 255f;
            float green = color.getGreen() / 255f;
            float blue = color.getBlue() / 255f;
            float alpha = color.getAlpha() / 255f;
            RenderSystem.setShader(GameRenderer::getPositionShader);
            GL11.glDepthFunc(GL11.GL_ALWAYS);
            RenderSystem.setShaderColor(red, green, blue, alpha);

            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);
            return buffer;
        }

        public static void renderOutline(Vec3d start, Vec3d dimensions, Color color, MatrixStack stack) {
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableBlend();
            BufferBuilder buffer = renderPrepare(color);

            renderOutlineIntern(start, dimensions, stack, buffer);

            BufferRenderer.drawWithShader(buffer.end());
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            RenderSystem.disableBlend();
        }

        static void renderOutlineIntern(Vec3d start, Vec3d dimensions, MatrixStack stack, BufferBuilder buffer) {
            Vec3d end = start.add(dimensions);
            Matrix4f matrix = stack.peek().getPositionMatrix();
            float x1 = (float) start.x;
            float y1 = (float) start.y;
            float z1 = (float) start.z;
            float x2 = (float) end.x;
            float y2 = (float) end.y;
            float z2 = (float) end.z;

            buffer.vertex(matrix, x1, y1, z1).next();
            buffer.vertex(matrix, x1, y1, z2).next();
            buffer.vertex(matrix, x1, y1, z2).next();
            buffer.vertex(matrix, x2, y1, z2).next();
            buffer.vertex(matrix, x2, y1, z2).next();
            buffer.vertex(matrix, x2, y1, z1).next();
            buffer.vertex(matrix, x2, y1, z1).next();
            buffer.vertex(matrix, x1, y1, z1).next();

            buffer.vertex(matrix, x1, y2, z1).next();
            buffer.vertex(matrix, x1, y2, z2).next();
            buffer.vertex(matrix, x1, y2, z2).next();
            buffer.vertex(matrix, x2, y2, z2).next();
            buffer.vertex(matrix, x2, y2, z2).next();
            buffer.vertex(matrix, x2, y2, z1).next();
            buffer.vertex(matrix, x2, y2, z1).next();
            buffer.vertex(matrix, x1, y2, z1).next();

            buffer.vertex(matrix, x1, y1, z1).next();
            buffer.vertex(matrix, x1, y2, z1).next();

            buffer.vertex(matrix, x2, y1, z1).next();
            buffer.vertex(matrix, x2, y2, z1).next();

            buffer.vertex(matrix, x2, y1, z2).next();
            buffer.vertex(matrix, x2, y2, z2).next();

            buffer.vertex(matrix, x1, y1, z2).next();
            buffer.vertex(matrix, x1, y2, z2).next();
        }

        void move() {
            life--;
            life = Math.max(0, life);
            accelX /= 1.1;
            accelY /= 1.1;
            velX += accelX;
            velY += accelY;
            x += velX;
            y += velY;
            double h = CoffeeMain.client.getWindow().getScaledHeight();
            double w = CoffeeMain.client.getWindow().getScaledWidth();
            if (x > w || x < 0) {
                velX *= -1;
            }
            if (y > h) {
                velY *= -1;
            }
            x = MathHelper.clamp(x, 0, w);
            y = Math.min(y, h);
        }

        void render(List<Particle> others, MatrixStack stack) {
            long fadeTime = 40;
            long startDelta = Math.min(origLife - life, fadeTime);
            long endDelta = Math.min(life, fadeTime);
            long deltaOverall = Math.min(startDelta, endDelta);
            double pk = (deltaOverall / (double) fadeTime);

            //            ShadowMain.client.textRenderer.draw(stack,pk+"",(float)x,(float)y,0xFFFFFF);
            pk = Transitions.easeOutExpo(pk);
            Theme theme = ThemeManager.getMainTheme();
            stack.push();
            double radToUse = pk * circleRad;
            Renderer.R2D.renderCircle(stack,
                    Renderer.Util.lerp(theme.getAccent(), DYING, pk),
                    x - radToUse / 2d,
                    y - radToUse / 2d,
                    radToUse,
                    30);
            stack.pop();
        }

        boolean isDead() {
            return life == 0 || y < 0;
        }
    }
}
