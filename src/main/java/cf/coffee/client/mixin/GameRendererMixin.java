/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.gui.DoesMSAA;
import cf.coffee.client.feature.gui.notifications.hudNotif.HudNotificationRenderer;
import cf.coffee.client.feature.gui.screen.ClientScreen;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleRegistry;
import cf.coffee.client.feature.module.impl.render.FreeLook;
import cf.coffee.client.feature.module.impl.render.Zoom;
import cf.coffee.client.helper.Rotations;
import cf.coffee.client.helper.event.EventType;
import cf.coffee.client.helper.event.Events;
import cf.coffee.client.helper.event.events.WorldRenderEvent;
import cf.coffee.client.helper.render.MSAAFramebuffer;
import cf.coffee.client.helper.render.Renderer;
import cf.coffee.client.helper.util.Utils;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    private boolean vb;
    private boolean dis;

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    void dispatchWorldRender(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        if (vb) {
            CoffeeMain.client.options.bobView = true;
            vb = false;
        }
        MSAAFramebuffer.use(MSAAFramebuffer.MAX_SAMPLES, () -> {
            for (Module module : ModuleRegistry.getModules()) {
                if (module.isEnabled()) {
                    module.onWorldRender(matrix);
                }
            }
            Events.fireEvent(EventType.WORLD_RENDER, new WorldRenderEvent(matrix));
            Renderer.R3D.renderFadingBlocks(matrix);
        });
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V"))
    void coffee_msaaScreenRender(Screen instance, MatrixStack matrices, int mouseX, int mouseY, float delta) {
        boolean shouldMsaa = false;
        for (Element child : instance.children()) {
            if (child instanceof DoesMSAA) {
                shouldMsaa = true;
                break;
            }
        }
        if (shouldMsaa && !(instance instanceof ClientScreen)) { // only do msaa if we dont already do it and need it
            MSAAFramebuffer.use(MSAAFramebuffer.MAX_SAMPLES, () -> instance.render(matrices, mouseX, mouseY, delta));
        } else {
            instance.render(matrices, mouseX, mouseY, delta);
        }
    }

    @Inject(at = @At("HEAD"), method = "renderWorld")
    private void preRenderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        dis = true;
    }

    @Inject(at = @At("HEAD"), method = "bobView", cancellable = true)
    private void stopCursorBob(MatrixStack matrices, float f, CallbackInfo ci) {
        if (CoffeeMain.client.options.bobView && dis) {
            vb = true;
            CoffeeMain.client.options.bobView = false;
            dis = false;
            ci.cancel();
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;"), method = "updateTargetedEntity", require = 0)
    HitResult replaceFreelookHitResult(Entity instance, double maxDistance, float tickDelta, boolean includeFluids) {
        if (ModuleRegistry.getByClass(FreeLook.class).isEnabled()) {
            Vec3d vec3d = instance.getCameraPosVec(tickDelta);
            Vec3d vec3d2 = Utils.Math.getRotationVector(Rotations.getClientPitch(), Rotations.getClientYaw());
            Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
            return instance.world.raycast(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, instance));
        } else {
            return instance.raycast(maxDistance, tickDelta, includeFluids);
        }
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    public void overwriteFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        double zv = ModuleRegistry.getByClass(Zoom.class).getZoomValue(cir.getReturnValue());
        cir.setReturnValue(zv);
    }

    @Inject(method = "render", at = @At("RETURN"))
    void a(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        HudNotificationRenderer.instance.render(Renderer.R3D.getEmptyMatrixStack());
    }
}
