/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.mixin.screen;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.impl.SelfDestruct;
import coffee.client.feature.gui.screen.ProxyManagerScreen;
import coffee.client.feature.gui.widget.RoundButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
    public MultiplayerScreenMixin() {
        super(Text.of(""));
    }

    @Inject(method = "init", at = @At("RETURN"))
    void coffee_postInit(CallbackInfo ci) {
        if (SelfDestruct.shouldSelfDestruct()) {
            return;
        }
        double sourceY = 32 / 2d - 20 / 2d;
        RoundButton proxies = new RoundButton(RoundButton.STANDARD,
                5,
                sourceY,
                60,
                20,
                "Proxies",
                () -> CoffeeMain.client.setScreen(new ProxyManagerScreen(this))
        );
        addDrawableChild(proxies);
    }
}
