/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.mixin.render;

import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.BlockRenderEvent;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {
    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    void coffee_dispatchRenderEvent(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, CallbackInfoReturnable<Boolean> cir) {
        BlockRenderEvent be = new BlockRenderEvent(matrices, pos, state);
        if (Events.fireEvent(EventType.BLOCK_RENDER, be)) {
            cir.setReturnValue(false);
        }
    }
}
