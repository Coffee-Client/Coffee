/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.impl.SelfDestruct;
import coffee.client.helper.event.EventSystem;
import coffee.client.helper.event.impl.KeyboardEvent;
import coffee.client.helper.manager.KeybindingManager;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onKey", at = @At("RETURN"))
    void coffee_postKeyPressed(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (window == this.client.getWindow()
            .getHandle() && CoffeeMain.client.currentScreen == null && System.currentTimeMillis() - CoffeeMain.lastScreenChange > 10 &&
            !SelfDestruct.shouldSelfDestruct()) { // make sure we are in game and the screen has been there for at least 10 ms
            if (CoffeeMain.client.player == null || CoffeeMain.client.world == null) {
                return; // again, make sure we are in game and exist
            }
            KeybindingManager.updateSingle(key, action);
            KeyboardEvent ke = new KeyboardEvent(key, KeyboardEvent.Type.of(action));
            EventSystem.manager.send(ke);
        }
    }
}
