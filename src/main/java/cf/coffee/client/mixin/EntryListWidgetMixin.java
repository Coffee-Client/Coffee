/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntryListWidget.class)
public class EntryListWidgetMixin {
    @Shadow
    protected int left;

    @Shadow
    protected int top;

    @Shadow
    protected int right;

    @Shadow
    protected int bottom;

    @Shadow
    protected int height;

    @Shadow
    protected int width;

    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/widget/EntryListWidget;renderHorizontalShadows:Z", opcode = Opcodes.GETFIELD))
    boolean r(EntryListWidget<?> instance, MatrixStack stack) {
        //        ClipStack.globalInstance.addWindow(stack,new Rectangle(left,top,width-right,height-bottom));
        return false;
    }

    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/widget/EntryListWidget;renderBackground:Z", opcode = Opcodes.GETFIELD))
    boolean r1(EntryListWidget<?> instance) {

        return false;
    }
}
