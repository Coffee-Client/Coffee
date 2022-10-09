/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.render;

import coffee.client.helper.event.EventSystem;
import coffee.client.helper.event.impl.RenderEvent;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {
    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    void coffee_dispatchRenderEvent(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, CallbackInfo ci) {
        RenderEvent re = new RenderEvent.Block(matrices, pos, state);
        EventSystem.manager.send(re);
        if (re.isCancelled()) {
            ci.cancel();
        }
        //        BlockRenderEvent be = new BlockRenderEvent(matrices, pos, state);
        //        if (Events.fireEvent(EventType.BLOCK_RENDER, be)) {
        //            ci.cancel();
        //        }
    }
}
