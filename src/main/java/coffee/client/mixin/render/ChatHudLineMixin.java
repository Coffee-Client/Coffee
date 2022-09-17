/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.render;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.render.NoMessageIndicators;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatHudLine.Visible.class)
public class ChatHudLineMixin {
    private static final Module noChatIndicators = ModuleRegistry.getByClass(NoMessageIndicators.class);

    @Inject(method = "indicator", at = @At("HEAD"), cancellable = true)
    void coffee_shutTheFuckUpMojang(CallbackInfoReturnable<MessageIndicator> cir) {
        if (noChatIndicators.isEnabled()) {
            cir.setReturnValue(null);
        }
    }
}
