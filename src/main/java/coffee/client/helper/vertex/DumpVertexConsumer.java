/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.vertex;

import lombok.Data;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.Stack;

public class DumpVertexConsumer implements VertexConsumer {
    final Stack<VertexData> stack = new Stack<>();

    public DumpVertexConsumer() {
        clear();
    }

    public void clear() {
        stack.clear();
        stack.push(new VertexData());
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        stack.peek().setPosition(new Vec3d(x, y, z));
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return color(red / 255f, green / 255f, blue / 255f, alpha / 255f);
    }

    @Override
    public VertexConsumer color(float red, float green, float blue, float alpha) {
        stack.peek().setColor(new float[] { red, green, blue, alpha });
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        stack.peek().setTexture(new float[] { u, v });
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        stack.peek().setOverlay(new int[] { u, v });
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        stack.peek().setLightUv(new int[] { u, v });
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        stack.peek().setNormal(new Vec3f(x, y, z));
        return this;
    }

    @Override
    public void next() {
        stack.push(new VertexData());
    }

    @Override
    public void fixedColor(int red, int green, int blue, int alpha) {

    }

    @Override
    public void unfixColor() {

    }

    public Stack<VertexData> getStack() {
        return stack;
    }

    @Data
    public static class VertexData {
        Vec3d position;
        float[] color = new float[4];
        float[] texture = new float[2];
        int[] overlay = new int[2];
        int[] lightUv = new int[2];
        Vec3f normal = new Vec3f(0, 0, 0);
    }
}
