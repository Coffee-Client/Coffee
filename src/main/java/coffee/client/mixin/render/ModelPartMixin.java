/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.mixin.render;

import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.render.ESP;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public class ModelPartMixin {
    @Inject(method = "renderCuboids", at = @At("HEAD"))
    void coffee_renderCuboids(MatrixStack.Entry entry, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
        if (ModuleRegistry.getByClass(ESP.class).recording) {
            ModuleRegistry.getByClass(ESP.class).vertexDumps.add(new double[0]);
        }
    }
}
