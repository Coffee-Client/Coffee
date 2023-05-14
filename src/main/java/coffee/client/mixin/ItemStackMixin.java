/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin;

import coffee.client.feature.module.impl.misc.AntiCrash;
import coffee.client.helper.event.EventSystem;
import coffee.client.helper.event.impl.LoreQueryEvent;
import com.google.common.collect.Multimap;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    private static int omitted = 0;

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    void coffee_dispatchTooltipRender(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        List<Text> cval = cir.getReturnValue();
        LoreQueryEvent event = new LoreQueryEvent((ItemStack) (Object) this, cval);
        EventSystem.manager.send(event);
        cir.setReturnValue(event.getExistingLore());
    }

    // these 2 look incredibly fucked but they work
    @Redirect(method = "getTooltip", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;entries()Ljava/util/Collection;"))
    Collection<Map.Entry<EntityAttribute, EntityAttributeModifier>> coffee_limitCollectionLength(Multimap<EntityAttribute, EntityAttributeModifier> instance) {
        Collection<Map.Entry<EntityAttribute, EntityAttributeModifier>> normalEntries = instance.entries();
        if (AntiCrash.instance().isEnabled() && AntiCrash.instance().getCapAttributes().getValue()) {
            int size = instance.size();
            int maxSize = (int) (AntiCrash.instance().getCapAttributesAmount().getValue() + 0);
            if (size > maxSize) {
                omitted = size - maxSize;
                return normalEntries.stream().limit(maxSize).toList();
            }
        }
        return normalEntries;
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;entries()Ljava/util/Collection;", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    void coffee_addOmittedTooltip(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        if (omitted != 0) {
            list.add(Text.literal("(Coffee: Omitted " + omitted + " entries)").formatted(Formatting.GRAY));
            omitted = 0;
        }
    }
}
