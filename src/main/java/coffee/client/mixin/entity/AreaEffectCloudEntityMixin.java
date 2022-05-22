/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.mixin.entity;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.impl.misc.AntiCrash;
import coffee.client.mixinUtil.ParticleManagerDuck;
import net.minecraft.entity.AreaEffectCloudEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AreaEffectCloudEntity.class)
public class AreaEffectCloudEntityMixin {
    @ModifyVariable(method = "tick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/math/MathHelper;ceil(F)I"), index = 4)
    int coffee_modifyIteration(int value) {
        AntiCrash ac = AntiCrash.instance();
        if (ac.isEnabled() && ac.getCapParticles().getValue()) {
            int partTotal = ((ParticleManagerDuck) CoffeeMain.client.particleManager).getTotalParticles();
            int newCount = partTotal + value;
            if (newCount >= ac.getParticleMax().getValue()) {
                int space = (int) Math.floor(ac.getParticleMax().getValue() - partTotal);
                return Math.min(value, Math.max(space, 0));
            }

        }
        return value;
    }
}
