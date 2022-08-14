/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.render;

import coffee.client.CoffeeMain;
import coffee.client.helper.math.Matrix4x4;
import coffee.client.helper.math.Vector3D;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vector4f;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Renderer {
    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void endRender() {
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static class R3D {

        static final MatrixStack empty = new MatrixStack();
        static final List<FadingBlock> fades = new CopyOnWriteArrayList<>();

        public static void renderFadingBlock(Color outlineColor, Color fillColor, Vec3d start, Vec3d dimensions, long lifeTimeMs) {
            FadingBlock fb = new FadingBlock(outlineColor, fillColor, start, dimensions, System.currentTimeMillis(), lifeTimeMs);

            fades.removeIf(fadingBlock -> fadingBlock.start.equals(start) && fadingBlock.dimensions.equals(dimensions));
            fades.add(fb);
        }

        public static void renderFadingBlocks(MatrixStack stack) {
            fades.removeIf(FadingBlock::isDead);
            Camera camera = CoffeeMain.client.getEntityRenderDispatcher().camera;
            Vec3d camPos = camera.getPos();
            fades.sort(Comparator.comparingDouble(value -> {
                Vec3d pos = value.start.add(value.dimensions.multiply(.5));
                return -pos.distanceTo(camPos);
            }));
            for (FadingBlock fade : fades) {
                if (fade == null) {
                    continue;
                }
                long lifetimeLeft = fade.getLifeTimeLeft();
                double progress = lifetimeLeft / (double) fade.lifeTime;
                progress = MathHelper.clamp(progress, 0, 1);
                double ip = 1 - progress;
                stack.push();
                Color out = Util.modify(fade.outline, -1, -1, -1, (int) (fade.outline.getAlpha() * progress));
                Color fill = Util.modify(fade.fill, -1, -1, -1, (int) (fade.fill.getAlpha() * progress));
                Renderer.R3D.renderEdged(stack, fade.start.add(new Vec3d(0.2, 0.2, 0.2).multiply(ip)),
                        fade.dimensions.subtract(new Vec3d(.4, .4, .4).multiply(ip)), fill, out);
                stack.pop();
            }
        }

        static Matrix4f setupStack(MatrixStack stack) {
            Camera camera = CoffeeMain.client.gameRenderer.getCamera();
            Vec3d camPos = camera.getPos();
            stack.translate(-camPos.x, -camPos.y, -camPos.z);
            return stack.peek().getPositionMatrix();
        }

        static float[] getColor(Color c) {
            return new float[] { c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f };
        }

        private static void useBuffer(VertexFormat.DrawMode mode, VertexFormat format, Supplier<Shader> shader, Consumer<BufferBuilder> runner) {
            Tessellator t = Tessellator.getInstance();
            BufferBuilder bb = t.getBuffer();

            bb.begin(mode, format);

            runner.accept(bb);

            setupRender();
            RenderSystem.setShader(shader);
            BufferRenderer.drawWithShader(bb.end());
            endRender();
        }

        public static void renderCircleOutline(MatrixStack stack, Color c, Vec3d start, double rad, double width, double segments) {
            stack.push();
            setupStack(stack);
            stack.translate(start.x, start.y, start.z);
            double segments1 = MathHelper.clamp(segments, 2, 90);

            Matrix4f matrix = stack.peek().getPositionMatrix();
            float[] col = getColor(c);

            useBuffer(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR, GameRenderer::getPositionColorShader,
                    bufferBuilder1 -> {
                        for (double r = 0; r < 360; r += (360 / segments1)) {
                            double rad1 = Math.toRadians(r);
                            double sin = Math.sin(rad1);
                            double cos = Math.cos(rad1);
                            double offX = sin * rad;
                            double offY = cos * rad;
                            bufferBuilder1.vertex(matrix, (float) offX, 0, (float) offY).color(col[0], col[1], col[2], col[3]).next();
                            bufferBuilder1.vertex(matrix, (float) (offX + sin * width), 0, (float) (offY + cos * width))
                                    .color(col[0], col[1], col[2], col[3])
                                    .next();
                        }
                    });

            stack.pop();
        }

        public static void renderOutline(Vec3d start, Vec3d dimensions, Color color, MatrixStack stack) {
            genericAABBRender(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR, GameRenderer::getPositionColorShader, stack,
                    start, dimensions, color, (buffer, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, matrix) -> {
                        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();

                        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();

                        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();

                        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();

                        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();

                        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();
                    });
        }

        public static MatrixStack getEmptyMatrixStack() {
            empty.loadIdentity(); // essentially clear the stack
            return empty;
        }

        public static void renderEdged(MatrixStack stack, Vec3d start, Vec3d dimensions, Color colorFill, Color colorOutline) {
            float[] fill = getColor(colorFill);
            float[] outline = getColor(colorOutline);

            stack.push();
            Matrix4f matrix = setupStack(stack);
            Vec3d end = start.add(dimensions);
            float x1 = (float) start.x;
            float y1 = (float) start.y;
            float z1 = (float) start.z;
            float x2 = (float) end.x;
            float y2 = (float) end.y;
            float z2 = (float) end.z;

            useBuffer(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR, GameRenderer::getPositionColorShader, buffer -> {
                float red = fill[0];
                float green = fill[1];
                float blue = fill[2];
                float alpha = fill[3];
                buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();

                buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();

                buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();

                buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();

                buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();

                buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();
            });

            useBuffer(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR, GameRenderer::getPositionColorShader, buffer -> {
                float red = outline[0];
                float green = outline[1];
                float blue = outline[2];
                float alpha = outline[3];
                buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();

                buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();

                buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();

                buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();

                buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();

                buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();
                buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();
            });
            stack.pop();
        }

        private static void genericAABBRender(VertexFormat.DrawMode mode, VertexFormat format, Supplier<Shader> shader, MatrixStack stack, Vec3d start, Vec3d dimensions, Color color, RenderAction action) {
            float red = color.getRed() / 255f;
            float green = color.getGreen() / 255f;
            float blue = color.getBlue() / 255f;
            float alpha = color.getAlpha() / 255f;
            stack.push();
            Matrix4f matrix = setupStack(stack);
            Vec3d end = start.add(dimensions);
            float x1 = (float) start.x;
            float y1 = (float) start.y;
            float z1 = (float) start.z;
            float x2 = (float) end.x;
            float y2 = (float) end.y;
            float z2 = (float) end.z;
            useBuffer(mode, format, shader,
                    bufferBuilder -> action.run(bufferBuilder, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, matrix));
            stack.pop();
        }

        public static void renderFilled(Vec3d start, Vec3d dimensions, Color color, MatrixStack stack) {
            genericAABBRender(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR, GameRenderer::getPositionColorShader, stack, start,
                    dimensions, color, (buffer, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, matrix) -> {
                        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();

                        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();

                        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();

                        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();

                        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).next();

                        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).next();
                    });
        }

        public static void renderLine(Vec3d start, Vec3d end, Color color, MatrixStack matrices) {
            genericAABBRender(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR, GameRenderer::getPositionColorShader,
                    matrices, start, end.subtract(start), color, (buffer, x, y, z, x1, y1, z1, red, green, blue, alpha, matrix) -> {
                        buffer.vertex(matrix, x, y, z).color(red, green, blue, alpha).next();
                        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
                    });
        }

        public static Vec3d getCrosshairVector() {
            Camera camera = CoffeeMain.client.gameRenderer.getCamera();

            float pi = (float) Math.PI;
            float yawRad = (float) Math.toRadians(-camera.getYaw());
            float pitchRad = (float) Math.toRadians(-camera.getPitch());
            float f1 = MathHelper.cos(yawRad - pi);
            float f2 = MathHelper.sin(yawRad - pi);
            float f3 = -MathHelper.cos(pitchRad);
            float f4 = MathHelper.sin(pitchRad);

            return new Vec3d(f2 * f3, f4, f1 * f3).add(camera.getPos());
        }

        interface RenderAction {
            void run(BufferBuilder buffer, float x, float y, float z, float x1, float y1, float z1, float red, float green, float blue, float alpha, Matrix4f matrix);
        }

        record FadingBlock(Color outline, Color fill, Vec3d start, Vec3d dimensions, long created, long lifeTime) {
            long getLifeTimeLeft() {
                return Math.max(0, (created - System.currentTimeMillis()) + lifeTime);
            }

            boolean isDead() {
                return getLifeTimeLeft() == 0;
            }
        }

    }

    public static class R2D {

        /**
         * Renders an arrow tooltip
         *
         * @param stack  The transformation stack
         * @param arrowX the x position of the arrow
         * @param arrowY the y position of the arrow
         * @param width  the width of the tooltip
         * @param height the height of the tooltip
         * @param color  the color of the tooltip
         * @return the start position (0,0) of the tooltip content, after considering where to place it
         */
        public static Vec2f renderTooltip(MatrixStack stack, double arrowX, double arrowY, double width, double height, Color color, boolean renderUpsideDown) {
            double centerX = CoffeeMain.client.getWindow().getScaledWidth() / 2d;
            /*
            left:
            *           /\
            * --------------
            * |            |
            * |            |
            * --------------
            right:
            *   /\
            * --------------
            * |            |
            * |            |
            * --------------
            * */
            boolean placeLeft = centerX < arrowX;
            /*
            top:
            *   /\
            * --------------
            * |            |
            * |            |
            * --------------
            bottom:
            * --------------
            * |            |
            * |            |
            * --------------
            *   V
            * */
            double arrowDimX = 10;
            double arrowDimY = 5;
            double roundStartX = placeLeft ? arrowX + arrowDimX / 2d + 10 - width : arrowX - arrowDimX / 2d - 10;
            double roundStartY = renderUpsideDown ? arrowY - arrowDimY - height : arrowY + arrowDimY;
            Matrix4f mat = stack.peek().getPositionMatrix();

            setupRender();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            renderRoundedQuadInternal(mat, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f,
                    roundStartX, roundStartY, roundStartX + width, roundStartY + height, 5, 5, 5, 5, 20);
            Tessellator t = Tessellator.getInstance();
            BufferBuilder bb = t.getBuffer();
            bb.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            if (renderUpsideDown) {
                bb.vertex(mat, (float) arrowX, (float) arrowY - .5f, 0)
                        .color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f)
                        .next();
                bb.vertex(mat, (float) (arrowX - arrowDimX / 2f), (float) (arrowY - arrowDimY - .5), 0)
                        .color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f)
                        .next();
                bb.vertex(mat, (float) (arrowX + arrowDimX / 2f), (float) (arrowY - arrowDimY - .5), 0)
                        .color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f)
                        .next();
            } else {
                bb.vertex(mat, (float) arrowX, (float) arrowY + .5f, 0)
                        .color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f)
                        .next();
                bb.vertex(mat, (float) (arrowX - arrowDimX / 2f), (float) (arrowY + arrowDimY + .5), 0)
                        .color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f)
                        .next();
                bb.vertex(mat, (float) (arrowX + arrowDimX / 2f), (float) (arrowY + arrowDimY + .5), 0)
                        .color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f)
                        .next();
            }
            t.draw();
            endRender();
            return new Vec2f((float) roundStartX, (float) roundStartY);
        }

        public static void beginScissor(double x, double y, double endX, double endY) {
            double width = endX - x;
            double height = endY - y;
            width = Math.max(0, width);
            height = Math.max(0, height);
            float mulScale = (float) CoffeeMain.client.getWindow().getScaleFactor();
            int invertedY = (int) ((CoffeeMain.client.getWindow().getScaledHeight() - (y + height)) * mulScale);
            RenderSystem.enableScissor((int) (x * mulScale), invertedY, (int) (width * mulScale), (int) (height * mulScale));
        }

        public static void endScissor() {
            RenderSystem.disableScissor();
        }

        public static void renderTexture(MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
            double x1 = x0 + width;
            double y1 = y0 + height;
            double z = 0;
            renderTexturedQuad(matrices.peek().getPositionMatrix(), x0, x1, y0, y1, z, (u + 0.0F) / (float) textureWidth,
                    (u + (float) regionWidth) / (float) textureWidth, (v + 0.0F) / (float) textureHeight,
                    (v + (float) regionHeight) / (float) textureHeight);
        }

        public static void renderRoundedShadowInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double rad, double samples, double wid) {
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

            double toX1 = toX - rad;
            double toY1 = toY - rad;
            double fromX1 = fromX + rad;
            double fromY1 = fromY + rad;
            double[][] map = new double[][] {
                    new double[] { toX1, toY1 },
                    new double[] { toX1, fromY1 },
                    new double[] { fromX1, fromY1 },
                    new double[] { fromX1, toY1 }
            };
            for (int i = 0; i < map.length; i++) {
                double[] current = map[i];
                for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
                    float rad1 = (float) Math.toRadians(r);
                    float sin = (float) (Math.sin(rad1) * rad);
                    float cos = (float) (Math.cos(rad1) * rad);
                    bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
                    float sin1 = (float) (sin + Math.sin(rad1) * wid);
                    float cos1 = (float) (cos + Math.cos(rad1) * wid);
                    bufferBuilder.vertex(matrix, (float) current[0] + sin1, (float) current[1] + cos1, 0.0F).color(cr, cg, cb, 0f).next();
                }
            }
            {
                double[] current = map[0];
                float rad1 = (float) Math.toRadians(0);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
                float sin1 = (float) (sin + Math.sin(rad1) * wid);
                float cos1 = (float) (cos + Math.cos(rad1) * wid);
                bufferBuilder.vertex(matrix, (float) current[0] + sin1, (float) current[1] + cos1, 0.0F).color(cr, cg, cb, 0f).next();
            }
            BufferRenderer.drawWithShader(bufferBuilder.end());
        }

        public static void renderRoundedShadow(MatrixStack matrices, Color innerColor, double fromX, double fromY, double toX, double toY, double rad, double samples, double shadowWidth) {
            int color = innerColor.getRGB();
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float f = (float) (color >> 24 & 255) / 255.0F;
            float g = (float) (color >> 16 & 255) / 255.0F;
            float h = (float) (color >> 8 & 255) / 255.0F;
            float k = (float) (color & 255) / 255.0F;
            setupRender();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            renderRoundedShadowInternal(matrix, g, h, k, f, fromX, fromY, toX, toY, rad, samples, shadowWidth);
            endRender();
        }

        public static void renderLoadingSpinner(MatrixStack stack, float alpha, double x, double y, double rad, double width, double segments) {
            stack.push();
            stack.translate(x, y, 0);
            float rot = (System.currentTimeMillis() % 2000) / 2000f;
            stack.multiply(new Quaternion(0, 0, rot * 360f, true));
            double segments1 = MathHelper.clamp(segments, 2, 90);

            Matrix4f matrix = stack.peek().getPositionMatrix();
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

            setupRender();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (double r = 0; r < 90; r += (90 / segments1)) {
                double rad1 = Math.toRadians(r);
                double sin = Math.sin(rad1);
                double cos = Math.cos(rad1);
                double offX = sin * rad;
                double offY = cos * rad;
                float prog = (float) r / 360f;
                prog -= rot;
                prog %= 1;
                Color hsb = Color.getHSBColor(prog, .6f, 1f);
                float g = hsb.getRed() / 255f;
                float h = hsb.getGreen() / 255f;
                float k = hsb.getBlue() / 255f;
                bufferBuilder.vertex(matrix, (float) offX, (float) offY, 0).color(g, h, k, alpha).next();
                bufferBuilder.vertex(matrix, (float) (offX + sin * width), (float) (offY + cos * width), 0).color(g, h, k, alpha).next();

            }
            BufferRenderer.drawWithShader(bufferBuilder.end());
            stack.pop();
            endRender();
        }

        private static void renderTexturedQuad(Matrix4f matrix, double x0, double x1, double y0, double y1, double z, float u0, float u1, float v0, float v1) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            bufferBuilder.vertex(matrix, (float) x0, (float) y1, (float) z).texture(u0, v1).next();
            bufferBuilder.vertex(matrix, (float) x1, (float) y1, (float) z).texture(u1, v1).next();
            bufferBuilder.vertex(matrix, (float) x1, (float) y0, (float) z).texture(u1, v0).next();
            bufferBuilder.vertex(matrix, (float) x0, (float) y0, (float) z).texture(u0, v0).next();
            BufferRenderer.drawWithShader(bufferBuilder.end());
        }

        public static void renderCircle(MatrixStack matrices, Color c, double originX, double originY, double rad, int segments) {
            int segments1 = MathHelper.clamp(segments, 4, 360);
            int color = c.getRGB();

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float f = (float) (color >> 24 & 255) / 255.0F;
            float g = (float) (color >> 16 & 255) / 255.0F;
            float h = (float) (color >> 8 & 255) / 255.0F;
            float k = (float) (color & 255) / 255.0F;
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            setupRender();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            for (int i = 0; i < 360; i += Math.min((360 / segments1), 360 - i)) {
                double radians = Math.toRadians(i);
                double sin = Math.sin(radians) * rad;
                double cos = Math.cos(radians) * rad;
                bufferBuilder.vertex(matrix, (float) (originX + sin), (float) (originY + cos), 0).color(g, h, k, f).next();
            }
            BufferRenderer.drawWithShader(bufferBuilder.end());
        }

        public static boolean isOnScreen(Vec3d pos) {
            return pos != null && (pos.z > -1 && pos.z < 1);
        }

        public static Vec3d getScreenSpaceCoordinate(Vec3d pos, MatrixStack stack) {
            Camera camera = CoffeeMain.client.getEntityRenderDispatcher().camera;
            Matrix4f matrix = stack.peek().getPositionMatrix();
            double x = pos.x - camera.getPos().x;
            double y = pos.y - camera.getPos().y;
            double z = pos.z - camera.getPos().z;
            Vector4f vector4f = new Vector4f((float) x, (float) y, (float) z, 1.f);
            vector4f.transform(matrix);
            int displayHeight = CoffeeMain.client.getWindow().getHeight();
            Vector3D screenCoords = new Vector3D();
            int[] viewport = new int[4];
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
            Matrix4x4 matrix4x4Proj = Matrix4x4.copyFromColumnMajor(RenderSystem.getProjectionMatrix());//no more joml :)
            Matrix4x4 matrix4x4Model = Matrix4x4.copyFromColumnMajor(
                    RenderSystem.getModelViewMatrix());//but I do the math myself now :( (heck math)
            matrix4x4Proj.mul(matrix4x4Model).project(vector4f.getX(), vector4f.getY(), vector4f.getZ(), viewport, screenCoords);
            return new Vec3d(screenCoords.x / CoffeeMain.client.getWindow().getScaleFactor(),
                    (displayHeight - screenCoords.y) / CoffeeMain.client.getWindow().getScaleFactor(), screenCoords.z);
        }

        public static void renderQuad(MatrixStack matrices, Color c, double x1, double y1, double x2, double y2) {
            double x11 = x1;
            double x21 = x2;
            double y11 = y1;
            double y21 = y2;
            int color = c.getRGB();
            double j;
            if (x11 < x21) {
                j = x11;
                x11 = x21;
                x21 = j;
            }

            if (y11 < y21) {
                j = y11;
                y11 = y21;
                y21 = j;
            }
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float f = (float) (color >> 24 & 255) / 255.0F;
            float g = (float) (color >> 16 & 255) / 255.0F;
            float h = (float) (color >> 8 & 255) / 255.0F;
            float k = (float) (color & 255) / 255.0F;
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            setupRender();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix, (float) x11, (float) y21, 0.0F).color(g, h, k, f).next();
            bufferBuilder.vertex(matrix, (float) x21, (float) y21, 0.0F).color(g, h, k, f).next();
            bufferBuilder.vertex(matrix, (float) x21, (float) y11, 0.0F).color(g, h, k, f).next();
            bufferBuilder.vertex(matrix, (float) x11, (float) y11, 0.0F).color(g, h, k, f).next();
            BufferRenderer.drawWithShader(bufferBuilder.end());
            endRender();
        }

        public static void renderQuadGradient(MatrixStack matrices, Color c2, Color c1, double x1, double y1, double x2, double y2, boolean vertical) {
            double x11 = x1;
            double x21 = x2;
            double y11 = y1;
            double y21 = y2;
            float r1 = c1.getRed() / 255f;
            float g1 = c1.getGreen() / 255f;
            float b1 = c1.getBlue() / 255f;
            float a1 = c1.getAlpha() / 255f;
            float r2 = c2.getRed() / 255f;
            float g2 = c2.getGreen() / 255f;
            float b2 = c2.getBlue() / 255f;
            float a2 = c2.getAlpha() / 255f;

            double j;

            if (x11 < x21) {
                j = x11;
                x11 = x21;
                x21 = j;
            }

            if (y11 < y21) {
                j = y11;
                y11 = y21;
                y21 = j;
            }
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            setupRender();

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            if (vertical) {
                bufferBuilder.vertex(matrix, (float) x11, (float) y11, 0.0F).color(r1, g1, b1, a1).next();
                bufferBuilder.vertex(matrix, (float) x11, (float) y21, 0.0F).color(r2, g2, b2, a2).next();
                bufferBuilder.vertex(matrix, (float) x21, (float) y21, 0.0F).color(r2, g2, b2, a2).next();
                bufferBuilder.vertex(matrix, (float) x21, (float) y11, 0.0F).color(r1, g1, b1, a1).next();
            } else {
                bufferBuilder.vertex(matrix, (float) x11, (float) y11, 0.0F).color(r1, g1, b1, a1).next();
                bufferBuilder.vertex(matrix, (float) x11, (float) y21, 0.0F).color(r1, g1, b1, a1).next();
                bufferBuilder.vertex(matrix, (float) x21, (float) y21, 0.0F).color(r2, g2, b2, a2).next();
                bufferBuilder.vertex(matrix, (float) x21, (float) y11, 0.0F).color(r2, g2, b2, a2).next();
            }

            BufferRenderer.drawWithShader(bufferBuilder.end());
            endRender();
        }

        public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double rad, double samples) {
            renderRoundedQuadInternal(matrix, cr, cg, cb, ca, fromX, fromY, toX, toY, rad, rad, rad, rad, samples);
        }

        public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radC1, double radC2, double radC3, double radC4, double samples) {
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

            double[][] map = new double[][] {
                    new double[] { toX - radC4, toY - radC4, radC4 },
                    new double[] { toX - radC2, fromY + radC2, radC2 },
                    new double[] { fromX + radC1, fromY + radC1, radC1 },
                    new double[] { fromX + radC3, toY - radC3, radC3 }
            };
            for (int i = 0; i < 4; i++) {
                double[] current = map[i];
                double rad = current[2];
                for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
                    float rad1 = (float) Math.toRadians(r);
                    float sin = (float) (Math.sin(rad1) * rad);
                    float cos = (float) (Math.cos(rad1) * rad);
                    bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
                }
                float rad1 = (float) Math.toRadians((360 / 4d + i * 90d));
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
            }
            BufferRenderer.drawWithShader(bufferBuilder.end());
        }

        public static void renderRoundedQuadWithShadow(MatrixStack matrices, Color c, double fromX, double fromY, double toX, double toY, double rad, double samples) {
            int color = c.getRGB();
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float f = (float) (color >> 24 & 255) / 255.0F;
            float g = (float) (color >> 16 & 255) / 255.0F;
            float h = (float) (color >> 8 & 255) / 255.0F;
            float k = (float) (color & 255) / 255.0F;
            setupRender();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            renderRoundedQuadInternal(matrix, g, h, k, f, fromX, fromY, toX, toY, rad, rad, rad, rad, samples);

            renderRoundedShadow(matrices, new Color(10, 10, 10, 100), fromX, fromY, toX, toY, rad, samples, 3);
            endRender();
        }

        public static void renderRoundedQuad(MatrixStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radC1, double radC2, double radC3, double radC4, double samples) {
            int color = c.getRGB();
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float f = (float) (color >> 24 & 255) / 255.0F;
            float g = (float) (color >> 16 & 255) / 255.0F;
            float h = (float) (color >> 8 & 255) / 255.0F;
            float k = (float) (color & 255) / 255.0F;
            setupRender();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            renderRoundedQuadInternal(matrix, g, h, k, f, fromX, fromY, toX, toY, radC1, radC2, radC3, radC4, samples);
            endRender();
        }

        public static void renderRoundedQuad(MatrixStack stack, Color c, double x, double y, double x1, double y1, double rad, double samples) {
            renderRoundedQuad(stack, c, x, y, x1, y1, rad, rad, rad, rad, samples);
        }

        public static void renderRoundedOutlineInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radC1, double radC2, double radC3, double radC4, double width, double samples) {
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

            double[][] map = new double[][] {
                    new double[] { toX - radC4, toY - radC4, radC4 },
                    new double[] { toX - radC2, fromY + radC2, radC2 },
                    new double[] { fromX + radC1, fromY + radC1, radC1 },
                    new double[] { fromX + radC3, toY - radC3, radC3 }
            };
            for (int i = 0; i < 4; i++) {
                double[] current = map[i];
                double rad = current[2];
                for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
                    float rad1 = (float) Math.toRadians(r);
                    double sin1 = Math.sin(rad1);
                    float sin = (float) (sin1 * rad);
                    double cos1 = Math.cos(rad1);
                    float cos = (float) (cos1 * rad);
                    bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
                    bufferBuilder.vertex(matrix, (float) (current[0] + sin + sin1 * width), (float) (current[1] + cos + cos1 * width), 0.0F)
                            .color(cr, cg, cb, ca)
                            .next();
                }
                float rad1 = (float) Math.toRadians((360 / 4d + i * 90d));
                double sin1 = Math.sin(rad1);
                float sin = (float) (sin1 * rad);
                double cos1 = Math.cos(rad1);
                float cos = (float) (cos1 * rad);
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
                bufferBuilder.vertex(matrix, (float) (current[0] + sin + sin1 * width), (float) (current[1] + cos + cos1 * width), 0.0F)
                        .color(cr, cg, cb, ca)
                        .next();
            }
            int i = 0;
            double[] current = map[i];
            double rad = current[2];
            float cos = (float) (rad);
            bufferBuilder.vertex(matrix, (float) current[0], (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
            bufferBuilder.vertex(matrix, (float) (current[0]), (float) (current[1] + cos + width), 0.0F).color(cr, cg, cb, ca).next();
            BufferRenderer.drawWithShader(bufferBuilder.end());
        }

        public static void renderRoundedOutline(MatrixStack matrices, Color c, double fromX, double fromY, double toX, double toY, double rad1, double rad2, double rad3, double rad4, double width, double samples) {
            int color = c.getRGB();
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float f = (float) (color >> 24 & 255) / 255.0F;
            float g = (float) (color >> 16 & 255) / 255.0F;
            float h = (float) (color >> 8 & 255) / 255.0F;
            float k = (float) (color & 255) / 255.0F;
            setupRender();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            renderRoundedOutlineInternal(matrix, g, h, k, f, fromX, fromY, toX, toY, rad1, rad2, rad3, rad4, width, samples);
            endRender();
        }

        public static void renderLine(MatrixStack stack, Color c, double x, double y, double x1, double y1) {
            float g = c.getRed() / 255f;
            float h = c.getGreen() / 255f;
            float k = c.getBlue() / 255f;
            float f = c.getAlpha() / 255f;
            Matrix4f m = stack.peek().getPositionMatrix();
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            setupRender();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(m, (float) x, (float) y, 0f).color(g, h, k, f).next();
            bufferBuilder.vertex(m, (float) x1, (float) y1, 0f).color(g, h, k, f).next();
            BufferRenderer.drawWithShader(bufferBuilder.end());
            endRender();
        }

    }

    public static class Util {

        public static int lerp(int o, int i, double p) {
            return (int) Math.floor(i + (o - i) * MathHelper.clamp(p, 0, 1));
        }

        public static double lerp(double i, double o, double p) {
            return (i + (o - i) * MathHelper.clamp(p, 0, 1));
        }

        public static Color lerp(Color a, Color b, double c) {
            return new Color(lerp(a.getRed(), b.getRed(), c), lerp(a.getGreen(), b.getGreen(), c), lerp(a.getBlue(), b.getBlue(), c),
                    lerp(a.getAlpha(), b.getAlpha(), c));
        }

        /**
         * @param original       the original color
         * @param redOverwrite   the new red (or -1 for original)
         * @param greenOverwrite the new green (or -1 for original)
         * @param blueOverwrite  the new blue (or -1 for original)
         * @param alphaOverwrite the new alpha (or -1 for original)
         * @return the modified color
         */
        public static Color modify(Color original, int redOverwrite, int greenOverwrite, int blueOverwrite, int alphaOverwrite) {
            return new Color(redOverwrite == -1 ? original.getRed() : redOverwrite,
                    greenOverwrite == -1 ? original.getGreen() : greenOverwrite, blueOverwrite == -1 ? original.getBlue() : blueOverwrite,
                    alphaOverwrite == -1 ? original.getAlpha() : alphaOverwrite);
        }

    }

}
