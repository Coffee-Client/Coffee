/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin;

import coffee.client.helper.event.EventSystem;
import coffee.client.helper.event.impl.ShouldSneakQuery;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z", ordinal = 5))
    boolean coffee_modifySneakPressed(KeyBinding instance) {
        ShouldSneakQuery ssq = new ShouldSneakQuery(instance.isPressed());
        EventSystem.manager.send(ssq);
        return ssq.isShouldSneak();
    }
}
