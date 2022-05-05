/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.gui.screen.ProxyManagerScreen;
import cf.coffee.client.feature.gui.widget.RoundButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
    public MultiplayerScreenMixin() {
        super(Text.of(""));
    }

    @Inject(method = "init", at = @At("RETURN"))
    void init(CallbackInfo ci) {
        double sourceY = 32 / 2d - 20 / 2d;
        RoundButton proxies = new RoundButton(new Color(40, 40, 40), 5, sourceY, 60, 20, "Proxies", () -> CoffeeMain.client.setScreen(new ProxyManagerScreen(this)));
        addDrawableChild(proxies);
    }
}
