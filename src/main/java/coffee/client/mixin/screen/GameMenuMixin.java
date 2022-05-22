/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.mixin.screen;

import coffee.client.feature.gui.screen.AddonManagerScreen;
import coffee.client.feature.gui.screen.HudEditorScreen;
import coffee.client.feature.gui.widget.RoundButton;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class GameMenuMixin extends Screen {
    protected GameMenuMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("RETURN"))
    void coffee_addClientButtons(CallbackInfo ci) {
        addDrawableChild(new RoundButton(RoundButton.STANDARD, 5, 5, 60, 20, "Addons", () -> {
            assert client != null;
            client.setScreen(new AddonManagerScreen());
        }));
        addDrawableChild(new RoundButton(RoundButton.STANDARD, 5, 30, 60, 20, "Edit HUD", () -> client.setScreen(new HudEditorScreen())));
    }
}
