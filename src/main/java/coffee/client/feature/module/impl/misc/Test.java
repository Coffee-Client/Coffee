/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;

public class Test extends Module {

    public Test() {
        super("Test", "Testing stuff with the client, can be ignored", ModuleType.MISC);
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

    @Override
    public void onWorldRender(MatrixStack matrices) {
        //Entity target = null;
        //Vec3d t = client.player.getPos();
        //Optional<Entity> e = StreamSupport.stream(client.world.getEntities().spliterator(), false).filter(entity -> !entity.equals(client.player)).min(Comparator.comparingDouble(value -> value.distanceTo(client.player)));
        //if (e.isEmpty()) return;
        //Entity target = e.get();
        //EntityRenderer<? super Entity> renderer = client.getEntityRenderDispatcher().getRenderer(target);
        //DumpVertexConsumer consumer = new DumpVertexConsumer();
        //DumpVertexProvider provider = new DumpVertexProvider(consumer, client.getBufferBuilders()
        //        .getEntityVertexConsumers());
        //renderer.render(target, (float) (Math.random()*360f),client.getTickDelta(),Renderer.R3D.getEmptyMatrixStack(),provider,0);
        //Vec3d entitySrc = target.getPos();

        //float red = color.getRed() / 255f;
        //float green = color.getGreen() / 255f;
        //float blue = color.getBlue() / 255f;
        //float alpha = color.getAlpha() / 255f;
        //Camera c = CoffeeMain.client.gameRenderer.getCamera();
        //Vec3d camPos = c.getPos();
        //Vec3d start1 = start.subtract(camPos);
        //Vec3d end1 = end.subtract(camPos);
        //Matrix4f matrix = matrices.peek().getPositionMatrix();
        //float x1 = (float) start1.x;
        //float y1 = (float) start1.y;
        //float z1 = (float) start1.z;
        //float x2 = (float) end1.x;
        //float y2 = (float) end1.y;
        //float z2 = (float) end1.z;
        //BufferBuilder buffer = Tessellator.getInstance().getBuffer();


        //Renderer.setupRender();
        //RenderSystem.enableDepthTest();
        //GL11.glDepthFunc(GL11.GL_ALWAYS);
        //RenderSystem.setShader(GameRenderer::getPositionColorShader);
        //buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        //buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).next();
        //buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).next();
        //for (DumpVertexConsumer.VertexData vertexData : consumer.getStack()) {
        //    Vec3d pos = vertexData.getPosition();
        //    if (pos == null) continue;
        //    //Vec3d translated = entitySrc.add(pos).subtract(camPos);
        //    //float[] color = vertexData.getColor();
        //    //buffer.vertex(matrix, (float) translated.x, (float) translated.y, (float) translated.z).color(color[0], color[1], color[2], color[3]).next();
        //    Vec3d translated = entitySrc.add(pos);
        //    Vec3d screenSpace = Renderer.R2D.getScreenSpaceCoordinate(translated, matrices);
        //    if (Renderer.R2D.isOnScreen(screenSpace)) {
        //        Utils.TickManager.runOnNextRender(() -> {
        //            MatrixStack stack = Renderer.R3D.getEmptyMatrixStack();
        //            stack.translate(screenSpace.x,screenSpace.y,0);
        //            stack.scale(0.4f,0.4f,1);
        //            FontRenderers.getMono().drawString(stack,pos.toString(),0,0,0xFFFFFF);
        //        });
        //    }
        //}

        //buffer.end();

        //BufferRenderer.draw(buffer);
        //GL11.glDepthFunc(GL11.GL_LEQUAL);
        //Renderer.endRender();


    }

    @Override
    public void onHudRender() {

    }

    @Override
    public void tick() {

    }
}
