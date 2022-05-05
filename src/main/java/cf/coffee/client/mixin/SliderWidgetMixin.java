/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import net.minecraft.client.gui.widget.SliderWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SliderWidget.class)
public class SliderWidgetMixin {
    // Makes the mouse click more accurate by replacing the expected indicator width (8px) with the actual, new indicator width (1.5 px, rounded to 2)
    @ModifyConstant(method = "setValueFromMouse", constant = @Constant(intValue = 4))
    int real(int constant) {
        return 1;
    }

    @ModifyConstant(method = "setValueFromMouse", constant = @Constant(intValue = 8))
    int real1(int constant) {
        return 2;
    }
}
