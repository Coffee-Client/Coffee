/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.mixin;

import coffee.client.CoffeeMain;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.MouseEvent;
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
            if (Events.fireEvent(EventType.MOUSE_EVENT, new MouseEvent(button, action))) {
                ci.cancel();
            }
        }
    }
}
