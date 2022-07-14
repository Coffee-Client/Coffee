/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.mixin.screen;

import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(DebugHud.class)
public interface IDebugHudMixin {
    @Invoker("getLeftText")
    List<String> callGetLeftText();

    @Invoker("getRightText")
    List<String> callGetRightText();
}
