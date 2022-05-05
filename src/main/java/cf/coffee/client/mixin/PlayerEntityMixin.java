/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.module.ModuleRegistry;
import cf.coffee.client.feature.module.impl.movement.LongJump;
import cf.coffee.client.feature.module.impl.movement.VanillaSpeed;
import cf.coffee.client.helper.event.EventType;
import cf.coffee.client.helper.event.Events;
import cf.coffee.client.helper.event.events.PlayerNoClipQueryEvent;
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
    void tickNoClip(PlayerEntity playerEntity, boolean value) {
        PlayerNoClipQueryEvent q = new PlayerNoClipQueryEvent(playerEntity);
        Events.fireEvent(EventType.NOCLIP_QUERY, q);
        playerEntity.noClip = q.getNoClip();
    }

    @Inject(method = "getMovementSpeed", at = @At("RETURN"), cancellable = true)
    void a(CallbackInfoReturnable<Float> cir) {
        VanillaSpeed hs = ModuleRegistry.getByClass(VanillaSpeed.class);
        if (!hs.isEnabled() || !equals(CoffeeMain.client.player)) {
            return;
        }
        cir.setReturnValue((float) (cir.getReturnValue() * hs.speed.getValue()));
    }

    @Inject(method = "jump", at = @At("RETURN"))
    void applyLongJump(CallbackInfo ci) {
        if (!this.equals(CoffeeMain.client.player)) {
            return;
        }
        if (ModuleRegistry.getByClass(LongJump.class).isEnabled()) {
            ModuleRegistry.getByClass(LongJump.class).applyLongJumpVelocity();
        }
    }
}
