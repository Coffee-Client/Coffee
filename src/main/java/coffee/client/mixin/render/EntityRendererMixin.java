/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.mixin.render;

import coffee.client.feature.module.impl.misc.AntiCrash;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @ModifyVariable(method = "renderLabelIfPresent", at = @At("HEAD"), index = 2, argsOnly = true)
    Text coffee_capText(Text text) {
        Text text1 = text;
        AntiCrash ac = AntiCrash.instance();
        if (ac.isEnabled() && ac.getCapNames().getValue()) {
            String t = text1.getString();
            int maxlen = (int) Math.floor(ac.getNameMax().getValue());
            int len = t.length();
            if (len > maxlen) {
                t = t.substring(0, maxlen) + "§r...";
            }
            text1 = Text.of(t);
        }
        return text1;
    }
}
