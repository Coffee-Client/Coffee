/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.screen;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.screen.NbtEditorScreen;
import coffee.client.feature.gui.widget.RoundButton;
import coffee.client.helper.util.Utils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryScreen.class)
public class CreativeInventoryScreenMixin extends Screen {

    protected CreativeInventoryScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    void coffee_postInit(CallbackInfo ci) {
        RoundButton nbtEdit = new RoundButton(RoundButton.STANDARD, 5, 5, 64, 20, "NBT editor", () -> {
            if (CoffeeMain.client.player.getInventory().getMainHandStack().isEmpty()) {
                Utils.Logging.error("You need to hold an item!");
            } else {
                CoffeeMain.client.setScreen(new NbtEditorScreen(CoffeeMain.client.player.getInventory().getMainHandStack()));
            }
        });
        addDrawableChild(nbtEdit);
    }
}
