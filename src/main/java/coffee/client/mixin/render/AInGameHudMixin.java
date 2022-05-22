/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.mixin.render;

import coffee.client.feature.gui.notifications.NotificationRenderer;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.helper.AccurateFrameRateCounter;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.Events;
import coffee.client.helper.event.events.base.NonCancellableEvent;
import coffee.client.helper.render.MSAAFramebuffer;
import coffee.client.helper.util.Utils;
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
    public void coffee_runRenderers(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        AccurateFrameRateCounter.globalInstance.recordFrame();
        MSAAFramebuffer.use(MSAAFramebuffer.MAX_SAMPLES, () -> {
            Utils.TickManager.render();
            for (Module module : ModuleRegistry.getModules()) {
                if (module.isEnabled()) {
                    module.onHudRender();
                }
            }

            NotificationRenderer.render();

            Events.fireEvent(EventType.HUD_RENDER, new NonCancellableEvent());
        });
    }
}
