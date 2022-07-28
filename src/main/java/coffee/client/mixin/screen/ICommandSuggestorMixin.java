/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.mixin.screen;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatInputSuggestor.class)
public interface ICommandSuggestorMixin {
    @Invoker("provideRenderText")
    OrderedText invokeProvideRenderText(String original, int index);
}
