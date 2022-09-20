/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin;

import coffee.client.feature.module.impl.misc.AntiCrash;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public class StyleMixin {
    @Inject(method = "isObfuscated", at = @At("HEAD"), cancellable = true)
    void coffee_replaceObfuscatedFlag(CallbackInfoReturnable<Boolean> cir) {
        if (AntiCrash.instance().isEnabled() && AntiCrash.instance().getDisableObfText().getValue()) {
            cir.setReturnValue(false);
        }
    }
}
