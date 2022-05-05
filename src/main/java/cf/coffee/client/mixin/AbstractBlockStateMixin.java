/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import cf.coffee.client.feature.module.ModuleRegistry;
import cf.coffee.client.feature.module.impl.world.XRAY;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {

    @Inject(method = "getLuminance", at = @At("HEAD"), cancellable = true)
    public void shadow_luminateBlock(CallbackInfoReturnable<Integer> cir) {
        if (Objects.requireNonNull(ModuleRegistry.getByClass(XRAY.class)).isEnabled()) {
            cir.setReturnValue(15);
        }
    }
}
