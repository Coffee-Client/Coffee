/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.vertex;

import coffee.client.mixin.render.IRenderPhaseMixin;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DumpVertexProvider implements VertexConsumerProvider {
    static final Map<RenderLayer, VertexConsumer> dumps = new HashMap<>();

    public List<DumpVertexConsumer> getBuffers() {
        return new ArrayList<>(dumps.values()).stream()
                .filter(vertexConsumer -> vertexConsumer instanceof DumpVertexConsumer)
                .map(vertexConsumer -> (DumpVertexConsumer) vertexConsumer)
                .toList();
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        return dumps.computeIfAbsent(layer, renderLayer -> {
            if (((IRenderPhaseMixin) renderLayer).getName().startsWith("entity")) {
                return new DumpVertexConsumer();
            } else {
                return new DiscardingVertexConsumer();
            }
        });
    }
}
