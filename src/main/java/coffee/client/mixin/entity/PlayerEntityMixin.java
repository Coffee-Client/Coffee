/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.mixin.entity;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.movement.LongJump;
import coffee.client.feature.module.impl.movement.VanillaSpeed;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.PlayerNoClipQueryEvent;
import net.minecraft.entity.player.PlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;noClip:Z", opcode = Opcodes.PUTFIELD))
    void coffee_overwriteNoClip(PlayerEntity playerEntity, boolean value) {
        PlayerNoClipQueryEvent q = new PlayerNoClipQueryEvent(playerEntity);
        Events.fireEvent(EventType.NOCLIP_QUERY, q);
        playerEntity.noClip = q.getNoClip();
    }

    @Inject(method = "getMovementSpeed", at = @At("RETURN"), cancellable = true)
    void coffee_overwriteMovementSpeed(CallbackInfoReturnable<Float> cir) {
        VanillaSpeed hs = ModuleRegistry.getByClass(VanillaSpeed.class);
        if (!hs.isEnabled() || !equals(CoffeeMain.client.player)) {
            return;
        }
        cir.setReturnValue((float) (cir.getReturnValue() * hs.speed.getValue()));
    }

    @Inject(method = "jump", at = @At("RETURN"))
    void coffee_applyLongJump(CallbackInfo ci) {
        if (!this.equals(CoffeeMain.client.player)) {
            return;
        }
        if (ModuleRegistry.getByClass(LongJump.class).isEnabled()) {
            ModuleRegistry.getByClass(LongJump.class).applyLongJumpVelocity();
        }
    }
}
