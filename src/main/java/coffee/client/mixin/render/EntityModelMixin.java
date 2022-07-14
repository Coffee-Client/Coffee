/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.mixin.render;

import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.render.ESP;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnimalModel.class)
public class EntityModelMixin {
    @Inject(method = "render", at = @At("HEAD"))
    void coffee_preRender(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
        // shut up retard
        //noinspection ConstantConditions
        ModuleRegistry.getByClass(ESP.class).recording = ModuleRegistry.getByClass(ESP.class)
                .isEnabled() && ModuleRegistry.getByClass(ESP.class).outlineMode.getValue() == ESP.Mode.Model && ((((Object) this) instanceof PlayerEntityModel && ModuleRegistry.getByClass(
                ESP.class).players.getValue()) || ModuleRegistry.getByClass(ESP.class).entities.getValue());
    }

    @Inject(method = "render", at = @At("TAIL"))
    void coffee_postRender(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
        ModuleRegistry.getByClass(ESP.class).recording = false;
    }
}
