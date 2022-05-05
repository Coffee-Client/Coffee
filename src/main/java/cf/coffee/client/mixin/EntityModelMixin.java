/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import cf.coffee.client.feature.module.ModuleRegistry;
import cf.coffee.client.feature.module.impl.render.ESP;
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
    void preRender(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
        // shut up retard
        //noinspection ConstantConditions
        ModuleRegistry.getByClass(ESP.class).recording = ModuleRegistry.getByClass(ESP.class).isEnabled() && ModuleRegistry.getByClass(ESP.class).outlineMode.getValue() == ESP.Mode.Model && ((((Object) this) instanceof PlayerEntityModel && ModuleRegistry.getByClass(ESP.class).players.getValue()) || ModuleRegistry.getByClass(ESP.class).entities.getValue());
        //        Utils.Logging.message(ModuleRegistry.getByClass(ESP.class).recording+"");
    }

    @Inject(method = "render", at = @At("TAIL"))
    void postRender(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
        ModuleRegistry.getByClass(ESP.class).recording = false;
    }
}
