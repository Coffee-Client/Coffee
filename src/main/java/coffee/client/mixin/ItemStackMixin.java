/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.mixin;

import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.LoreQueryEvent;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    void coffee_dispatchTooltipRender(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        List<Text> cval = cir.getReturnValue();
        LoreQueryEvent event = new LoreQueryEvent((ItemStack) (Object) this, cval);
        Events.fireEvent(EventType.LORE_QUERY, event);
        cir.setReturnValue(event.getLore());
    }
}
