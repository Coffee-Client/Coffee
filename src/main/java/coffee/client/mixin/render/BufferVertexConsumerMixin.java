/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.mixin.render;

import coffee.client.helper.render.AlphaOverride;
import net.minecraft.client.render.BufferVertexConsumer;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Debug(export = true)
@Mixin(BufferVertexConsumer.class)
public interface BufferVertexConsumerMixin {
    @ModifyVariable(method = "color", at = @At("HEAD"), argsOnly = true, index = 4)
    default int real(int value) {
        return (int) AlphaOverride.compute(value);
    }
}
