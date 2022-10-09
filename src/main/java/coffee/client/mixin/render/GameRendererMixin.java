/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.DoesMSAA;
import coffee.client.feature.gui.notifications.NotificationRenderer;
import coffee.client.feature.gui.notifications.hudNotif.HudNotificationRenderer;
import coffee.client.feature.gui.screen.base.ClientScreen;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.render.FreeLook;
import coffee.client.feature.module.impl.render.LSD;
import coffee.client.feature.module.impl.render.Zoom;
import coffee.client.helper.event.EventSystem;
import coffee.client.helper.event.impl.RenderEvent;
import coffee.client.helper.render.MSAAFramebuffer;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.AccurateFrameRateCounter;
import coffee.client.helper.util.Rotations;
import coffee.client.helper.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.RaycastContext;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameRenderer.class, priority = 990)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private Camera camera;

    @Shadow
    protected abstract double getFov(Camera camera, float tickDelta, boolean changingFov);

    @Shadow
    public abstract void tick();

    @Shadow
    public abstract Matrix4f getBasicProjectionMatrix(double fov);

    @Shadow
    protected abstract void bobViewWhenHurt(MatrixStack matrices, float tickDelta);

    @Shadow
    public abstract void loadProjectionMatrix(Matrix4f projectionMatrix);

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    void coffee_dispatchWorldRender(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        RenderSystem.backupProjectionMatrix();
        clearViewBobbing(tickDelta);
        MatrixStack ms = Renderer.R3D.getEmptyMatrixStack();
        ms.push();
        ms.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
        ms.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));
        MSAAFramebuffer.use(MSAAFramebuffer.MAX_SAMPLES, () -> {
            for (Module module : ModuleRegistry.getModules()) {
                if (module.isEnabled()) {
                    module.onWorldRender(ms);
                }
            }
            EventSystem.manager.send(new RenderEvent.World(ms));
            Renderer.R3D.renderFadingBlocks(ms);
            Renderer.R3D.renderActions();
        });
        ms.pop();
        RenderSystem.restoreProjectionMatrix();
    }

    void clearViewBobbing(float tickDelta) {
        MatrixStack ms = Renderer.R3D.getEmptyMatrixStack();
        double d = this.getFov(camera, tickDelta, true);
        ms.peek().getPositionMatrix().multiply(this.getBasicProjectionMatrix(d));
        this.bobViewWhenHurt(ms, tickDelta);
        loadProjectionMatrix(ms.peek().getPositionMatrix());
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

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", shift = At.Shift.BEFORE), method = "render")
    void coffee_postHudRenderNoCheck(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        AccurateFrameRateCounter.globalInstance.recordFrame();
        MSAAFramebuffer.use(MSAAFramebuffer.MAX_SAMPLES, () -> {
            Utils.TickManager.render();
            for (Module module : ModuleRegistry.getModules()) {
                if (module.isEnabled()) {
                    module.onHudRender();
                }
            }

            NotificationRenderer.render();
        });
        //        Events.fireEvent(EventType.HUD_RENDER_NOMSAA, new NonCancellableEvent());
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;"), method = "updateTargetedEntity", require = 0)
    HitResult coffee_replaceHitResult(Entity instance, double maxDistance, float tickDelta, boolean includeFluids) {
        if (ModuleRegistry.getByClass(FreeLook.class).isEnabled() && !((boolean) FreeLook.instance().getEnableAA().getValue())) {
            Vec3d vec3d = instance.getCameraPosVec(tickDelta);
            Vec3d vec3d2 = Utils.Math.getRotationVector(Rotations.getClientPitch(), Rotations.getClientYaw());
            Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
            return instance.world.raycast(new RaycastContext(vec3d,
                vec3d3,
                RaycastContext.ShapeType.OUTLINE,
                includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE,
                instance));
        } else {
            return instance.raycast(maxDistance, tickDelta, includeFluids);
        }
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    public void coffee_overwriteFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        double zv = ModuleRegistry.getByClass(Zoom.class).getZoomValue(cir.getReturnValue());
        cir.setReturnValue(zv);
    }

    @Inject(method = "render", at = @At("RETURN"))
    void coffee_renderNotifs(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        HudNotificationRenderer.instance.render(Renderer.R3D.getEmptyMatrixStack());
    }

    @Inject(method = "render", at = @At("RETURN"))
    void coffee_afterScreenRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if (CoffeeMain.client.world == null || CoffeeMain.client.player == null) {
            return;
        }
        LSD byClass = ModuleRegistry.getByClass(LSD.class);
        if (byClass.isEnabled()) {
            byClass.draw();
        }
    }
}
