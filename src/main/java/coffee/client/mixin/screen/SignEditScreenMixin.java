/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.screen;

import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.world.AutoSign;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(AbstractSignEditScreen.class)
public abstract class SignEditScreenMixin extends Screen {

    @Mutable
    @Shadow
    private SignText text;

    protected SignEditScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract void finishEditing();

    @Inject(at = @At("HEAD"), method = "init")
    private void coffee_preInit(CallbackInfo ci) {
        if (ModuleRegistry.getByClass(AutoSign.class).isEnabled()) {
            Text[] array = Arrays.stream(ModuleRegistry.getByClass(AutoSign.class).getText()).map(Text::of).toArray(Text[]::new);
            text = new SignText(array, array, DyeColor.BLACK, false);
            finishEditing();
        }
    }
}
