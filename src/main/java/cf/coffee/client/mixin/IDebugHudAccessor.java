/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(DebugHud.class)
public interface IDebugHudAccessor {
    @Invoker("getLeftText")
    List<String> callGetLeftText();

    @Invoker("getRightText")
    List<String> callGetRightText();
}
