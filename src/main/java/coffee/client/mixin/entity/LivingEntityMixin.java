/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.entity;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.movement.Jesus;
import coffee.client.feature.module.impl.movement.NoLevitation;
import coffee.client.feature.module.impl.movement.NoPush;
import coffee.client.feature.module.impl.render.FreeLook;
import coffee.client.helper.manager.AttackManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(value = LivingEntity.class, priority = 990)
public class LivingEntityMixin {
    @Inject(method = "onAttacking", at = @At("HEAD"))
    public void coffee_setLastAttacked(Entity target, CallbackInfo ci) {
        if (this.equals(CoffeeMain.client.player) && target instanceof LivingEntity entity) {
            AttackManager.registerLastAttacked(entity);
        }
    }

    @Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
    public void coffee_overwriteFluidWalk(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        if (CoffeeMain.client.player == null) {
            return;
        }
        // shut up monkey these are mixins you fucking idiot
        if (this.equals(CoffeeMain.client.player)) {
            Jesus jesus = ModuleRegistry.getByClass(Jesus.class);
            if (jesus.isEnabled() && jesus.mode.getValue() == Jesus.Mode.Solid) {
                cir.setReturnValue(true);
            }
        }
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "net/minecraft/entity/LivingEntity.hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"), require = 0)
    boolean coffee_stopLevitationEffect(LivingEntity instance, StatusEffect effect) {
        if (instance.equals(CoffeeMain.client.player) && ModuleRegistry.getByClass(NoLevitation.class).isEnabled() && effect == StatusEffects.LEVITATION) {
            return false;
        } else {
            return instance.hasStatusEffect(effect);
        }
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true, require = 0)
    public void coffee_cancelCollision(Entity entity, CallbackInfo ci) {
        if (this.equals(CoffeeMain.client.player)) {
            if (Objects.requireNonNull(ModuleRegistry.getByClass(NoPush.class)).isEnabled()) {
                ci.cancel();
            }
        }
    }

    // INCREDIBLE baritone hack, never fucking do this
    // also fuck you leijurv for doing the same redirect as me
    @SuppressWarnings("all") // ALSO never do this but in this case its ok because the mcdev plugin will not stop screaming
    @ModifyVariable(method = "jump", at = @At(value = "STORE"), ordinal = 0)
    private float coffee_replaceYaw(float f) {
        if (equals(CoffeeMain.client.player) && ModuleRegistry.getByClass(FreeLook.class).isEnabled() && !((boolean) FreeLook.instance().getEnableAA().getValue())) {
            return (float) Math.toDegrees(ModuleRegistry.getByClass(FreeLook.class).newyaw);
        }
        return f;
    }
}
