/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.mixin.render;

import coffee.client.CoffeeMain;
import coffee.client.helper.util.Rotations;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 2, at = @At(value = "STORE", ordinal = 0))
    public float coffee_overwriteYaw(float oldValue, LivingEntity le) {
        if (Rotations.isEnabled() && le.equals(CoffeeMain.client.player)) {
            return Rotations.getClientYaw();
        }
        return oldValue;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 3, at = @At(value = "STORE", ordinal = 0))
    public float coffee_overwriteHeadYaw(float oldValue, LivingEntity le) {
        if (le.equals(CoffeeMain.client.player) && Rotations.isEnabled()) {
            return Rotations.getClientYaw();
        }
        return oldValue;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 5, at = @At(value = "STORE", ordinal = 3))
    public float coffee_overwritePitch(float oldValue, LivingEntity le) {
        if (le.equals(CoffeeMain.client.player) && Rotations.isEnabled()) {
            return Rotations.getClientPitch();
        }
        return oldValue;
    }

}
