/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.mixin;


import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.misc.AllowFormatCodes;
import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SharedConstants.class)
public class SharedConstantsMixin {

    @Inject(method = "isValidChar", at = @At("HEAD"), cancellable = true)
    private static void coffee_yesThisIsAValidCharDoNotAtMe(char chr, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleRegistry.getByClass(AllowFormatCodes.class).isEnabled() && chr == '§') {
            cir.setReturnValue(true);
        }
    }
}
