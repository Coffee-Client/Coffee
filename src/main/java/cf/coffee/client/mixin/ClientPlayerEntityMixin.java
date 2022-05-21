/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleRegistry;
import cf.coffee.client.feature.module.impl.misc.PortalGUI;
import cf.coffee.client.feature.module.impl.movement.NoPush;
import cf.coffee.client.feature.module.impl.movement.Phase;
import cf.coffee.client.feature.module.impl.render.Freecam;
import cf.coffee.client.helper.manager.ConfigManager;
import cf.coffee.client.helper.util.Utils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void preTick(CallbackInfo ci) {
        Utils.TickManager.tick();
        if (!ConfigManager.enabled) {
            ConfigManager.enableModules();
        }
        for (Module module : ModuleRegistry.getModules()) {
            if (module.isEnabled()) {
                module.tick();
            }
        }
    }

    @Redirect(method = "updateNausea", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;shouldPause()Z"))
    public boolean overwriteIsPauseScreen(Screen screen) {
        return Objects.requireNonNull(ModuleRegistry.getByClass(PortalGUI.class)).isEnabled() || screen.shouldPause();
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    public void preventPushOutFromBlocks(double x, double z, CallbackInfo ci) {
        if (Objects.requireNonNull(ModuleRegistry.getByClass(Freecam.class))
                .isEnabled() || Objects.requireNonNull(ModuleRegistry.getByClass(NoPush.class))
                .isEnabled() || Objects.requireNonNull(ModuleRegistry.getByClass(Phase.class)).isEnabled()) {
            ci.cancel();
        }
    }

}
