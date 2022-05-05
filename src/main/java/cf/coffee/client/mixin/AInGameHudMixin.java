/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import cf.coffee.client.feature.gui.notifications.NotificationRenderer;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleRegistry;
import cf.coffee.client.helper.AccurateFrameRateCounter;
import cf.coffee.client.helper.event.EventType;
import cf.coffee.client.helper.event.Events;
import cf.coffee.client.helper.event.events.base.NonCancellableEvent;
import cf.coffee.client.helper.render.MSAAFramebuffer;
import cf.coffee.client.helper.util.Utils;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class AInGameHudMixin extends DrawableHelper {
    @Inject(method = "render", at = @At("RETURN"))
    public void postRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        AccurateFrameRateCounter.globalInstance.recordFrame();
        MSAAFramebuffer.use(MSAAFramebuffer.MAX_SAMPLES, () -> {
            for (Module module : ModuleRegistry.getModules()) {
                if (module.isEnabled()) {
                    module.onHudRender();
                }
            }
            Utils.TickManager.render();
            NotificationRenderer.render();

            Events.fireEvent(EventType.HUD_RENDER, new NonCancellableEvent());
        });
        for (Module module : ModuleRegistry.getModules()) {
            if (module.isEnabled()) {
                module.onHudRenderNoMSAA();
            }
        }
    }
}
