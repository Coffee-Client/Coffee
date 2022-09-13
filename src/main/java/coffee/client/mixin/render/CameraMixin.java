/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.mixin.render;

import coffee.client.feature.module.impl.render.NoLiquidFog;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {
    @Inject(method = "getSubmersionType", at = @At("RETURN"), cancellable = true)
    void coffee_pretendEverythingIsFine(CallbackInfoReturnable<CameraSubmersionType> cir) {
        if (NoLiquidFog.INSTANCE != null && NoLiquidFog.INSTANCE.isEnabled() && (cir.getReturnValue() == CameraSubmersionType.WATER || cir.getReturnValue() == CameraSubmersionType.LAVA)) {
            cir.setReturnValue(CameraSubmersionType.NONE);
        }
    }
}
