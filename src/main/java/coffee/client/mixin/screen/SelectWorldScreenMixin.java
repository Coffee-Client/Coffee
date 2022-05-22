/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.mixin.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.screen.world.SelectWorldScreen.class)
public class SelectWorldScreenMixin extends Screen {
    public SelectWorldScreenMixin() {
        super(Text.of(""));
    }

    @Inject(method = "render", at = @At("HEAD"))
    void coffee_preRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        renderBackground(matrices);
    }
}
