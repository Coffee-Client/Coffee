/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.mixin;

import coffee.client.CoffeeMain;
import coffee.client.feature.command.impl.SelfDestruct;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.world.FastUse;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.base.NonCancellableEvent;
import coffee.client.helper.manager.ConfigManager;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    private int itemUseCooldown;

    @Inject(method = "stop", at = @At("HEAD"))
    void coffee_dispatchExit(CallbackInfo ci) {
        ConfigManager.saveState();
        Events.fireEvent(EventType.GAME_EXIT, new NonCancellableEvent());
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    void coffee_postWindowInit(RunArgs args, CallbackInfo ci) {
        CoffeeMain.INSTANCE.postWindowInit();
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    void coffee_setScreenChange(Screen screen, CallbackInfo ci) {
        CoffeeMain.lastScreenChange = System.currentTimeMillis();
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/MinecraftClient;itemUseCooldown:I"))
    public int coffee_replaceItemUseCooldown(MinecraftClient minecraftClient) {
        if (Objects.requireNonNull(ModuleRegistry.getByClass(FastUse.class)).isEnabled()) {
            return 0;
        } else {
            return this.itemUseCooldown;
        }
    }

    @Inject(method = "getGameVersion", at = @At("HEAD"), cancellable = true)
    void coffee_replaceGameVersion(CallbackInfoReturnable<String> cir) {
        if (SelfDestruct.shouldSelfDestruct()) {
            cir.setReturnValue(SharedConstants.getGameVersion().getName());
        }
    }

    @Inject(method = "getVersionType", at = @At("HEAD"), cancellable = true)
    void coffee_replaceVersionType(CallbackInfoReturnable<String> cir) {
        if (SelfDestruct.shouldSelfDestruct()) {
            cir.setReturnValue("release");
        }
    }

}
