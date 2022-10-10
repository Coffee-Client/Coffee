/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin;

import coffee.client.CoffeeMain;
import coffee.client.helper.event.EventSystem;
import coffee.client.helper.event.impl.MouseEvent;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    public void coffee_dispatchMouseEvent(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window == CoffeeMain.client.getWindow().getHandle()) {
            MouseEvent me = new MouseEvent(button, MouseEvent.Type.of(action));
            EventSystem.manager.send(me);
            if (me.isCancelled()) {
                ci.cancel();
            }
        }
    }
}
