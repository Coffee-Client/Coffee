/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.mixin.screen;

import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.world.AutoSign;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignEditScreen.class)
public abstract class SignEditScreenMixin extends Screen {

    @Mutable
    @Shadow
    @Final
    private String[] text;

    protected SignEditScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract void finishEditing();

    @Inject(at = @At("HEAD"), method = "init")
    private void coffee_preInit(CallbackInfo ci) {
        if (ModuleRegistry.getByClass(AutoSign.class).isEnabled()) {
            text = ModuleRegistry.getByClass(AutoSign.class).getText();
            finishEditing();
        }
    }
}
