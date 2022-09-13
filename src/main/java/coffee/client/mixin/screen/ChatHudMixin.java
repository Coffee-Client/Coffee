/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.mixin.screen;

import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.misc.MoreChatHistory;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Debug(export = true)
@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @ModifyConstant(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", constant = @Constant(intValue = 100))
    int coffee_increaseHistorySize(int constant) {
        MoreChatHistory hist = ModuleRegistry.getByClass(MoreChatHistory.class);
        if (hist.isEnabled()) {
            return hist.getHistSize();
        } else {
            return 100;
        }
    }
}
