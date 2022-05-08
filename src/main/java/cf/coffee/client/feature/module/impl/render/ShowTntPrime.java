/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.render;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.helper.event.EventType;
import cf.coffee.client.helper.event.Events;
import cf.coffee.client.helper.event.events.PacketEvent;
import cf.coffee.client.helper.font.FontRenderers;
import cf.coffee.client.helper.render.Renderer;
import cf.coffee.client.helper.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class ShowTntPrime extends Module {
    final Int2IntArrayMap i2iamp = new Int2IntArrayMap();

    public ShowTntPrime() {
        super("ShowTntPrime", "Shows how much time is left for a piece of tnt to explode", ModuleType.RENDER);
        Events.registerEventHandler(EventType.PACKET_RECEIVE, event -> {
            if (!this.isEnabled()) return;
            PacketEvent pe = (PacketEvent) event;
            if (pe.getPacket() instanceof EntityTrackerUpdateS2CPacket p) {
                Entity e = CoffeeMain.client.world.getEntityById(p.id());
                if (e == null) return;
                if (e instanceof TntEntity) {
                    if (i2iamp.size() > 200) {
                        return;
                    }
                    if (p.getTrackedValues() == null || p.getTrackedValues().size() == 0) return;
                    //                    System.out.println(p.getTrackedValues().getExtern(0).getExtern());
                    if (!i2iamp.containsKey(p.id()))
                        i2iamp.put(p.id(), Integer.parseInt(p.getTrackedValues().get(0).get() + ""));
                }
            }
        });
    }

    static void semicircle(MatrixStack stack, Color c, double x, double y, double rad, double width, double segments, double toRad) {
        double toRad1 = MathHelper.clamp(toRad, 0, 360);
        stack.push();
        stack.translate(x, y, 0);
        //        stack.multiply(new Quaternion(0, 0, (System.currentTimeMillis() % 2000) / 2000f * 360f, true));
        double segments1 = MathHelper.clamp(segments, 2, 90);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        int color = c.getRGB();

        Matrix4f matrix = stack.peek().getPositionMatrix();
        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        //            RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (double r = 0; r < toRad1; r += Math.min(360 / segments1, (toRad1 - r))) {
            double rad1 = Math.toRadians(r);
            double sin = Math.sin(rad1);
            double cos = Math.cos(rad1);
            double offX = sin * rad;
            double offY = cos * rad;
            bufferBuilder.vertex(matrix, (float) offX, (float) offY, 0).color(g, h, k, f).next();
            bufferBuilder.vertex(matrix, (float) (offX + sin * width), (float) (offY + cos * width), 0).color(g, h, k, f).next();

        }
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        stack.pop();
    }

    @Override
    public void tick() {
        //        System.out.println(i2iamp);
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return (i2iamp.size() + "");
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        //        System.out.println(i2iamp.keySet().size());
        for (Integer integer : i2iamp.keySet()) {
            Entity e = CoffeeMain.client.world.getEntityById(integer);
            if (e == null) i2iamp.remove((int) integer);
            else {
                int fuseStart = i2iamp.get((int) integer);
                int fuseNow = ((TntEntity) e).getFuse();
                double prog = (double) fuseNow / fuseStart;
                //                System.out.println(prog);
                Vec3d ePos = Utils.getInterpolatedEntityPosition(e).add(0, e.getHeight(), 0);
                Vec3d screenSpace = Renderer.R2D.getScreenSpaceCoordinate(ePos, matrices);
                if (Renderer.R2D.isOnScreen(screenSpace)) {
                    Utils.TickManager.runOnNextRender(() -> {
                        //                        Renderer.R2D.renderQuad(Renderer.Util.lerp(Color.RED,Color.GREEN,prog),screenSpace.x,screenSpace.y,screenSpace.x+30,screenSpace.y+30);

                        drawSingleEntity(screenSpace, (TntEntity) e, prog);
                    });
                }
            }
        }
    }

    void drawSingleEntity(Vec3d screenSpacePos, TntEntity entity, double progress) {
        double cWidth = 30;
        double cHeight = 30;
        MatrixStack nothing = Renderer.R3D.getEmptyMatrixStack();
        Vec2f root = Renderer.R2D.renderTooltip(nothing, screenSpacePos.x, screenSpacePos.y, 30, 30, new Color(20, 20, 20), true);
        String txt = Utils.Math.roundToDecimal(entity.getFuse() / 20d, 1) + "";
        FontRenderers.getRenderer().drawString(nothing, txt, root.x + cWidth / 2d - (FontRenderers.getRenderer().getStringWidth(txt)) / 2d, root.y + cHeight / 2d - FontRenderers.getRenderer().getMarginHeight() / 2d, 0xFFFFFF);
        //        semicircle(nothing,new Color(50,50,50),root.x+cWidth/2d,root.y+cHeight/2d,cWidth/2d-4,2,40,360);
        semicircle(nothing, Renderer.Util.lerp(new Color(50, 255, 50), new Color(255, 50, 50), progress), root.x + cWidth / 2d, root.y + cHeight / 2d, cWidth / 2d - 4, 2, 90, 360 * progress);

    }

    @Override
    public void onHudRender() {

    }
}
