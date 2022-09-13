/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.vertex;

import net.minecraft.client.render.VertexConsumer;

public class DiscardingVertexConsumer implements VertexConsumer {

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return this;
    }

    @Override
    public void next() {

    }

    @Override
    public void fixedColor(int red, int green, int blue, int alpha) {

    }

    @Override
    public void unfixColor() {

    }
}
