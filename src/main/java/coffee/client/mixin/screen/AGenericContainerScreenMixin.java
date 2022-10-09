/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.screen;

import coffee.client.CoffeeMain;
import coffee.client.feature.gui.FastTickable;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.movement.InventoryWalk;
import coffee.client.helper.util.Rotations;
import coffee.client.mixin.IKeyBindingMixin;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(HandledScreen.class)
public abstract class AGenericContainerScreenMixin extends Screen implements FastTickable {
    @Shadow
    protected int x;
    @Shadow
    protected int y;
    float pitchOffset, yawOffset;
    float initialPitch, initialYaw;

    private AGenericContainerScreenMixin() {
        super(null);
    }

    boolean keyPressed(KeyBinding bind) {
        int code = ((IKeyBindingMixin) bind).getBoundKey().getCode();
        //        return bind.isPressed();
        return InputUtil.isKeyPressed(CoffeeMain.client.getWindow().getHandle(), code);
    }

    void setState(KeyBinding bind) {
        bind.setPressed(keyPressed(bind));
    }

    @Inject(method = "init", at = @At("RETURN"))
    void coffee_postInit(CallbackInfo ci) {
        initialPitch = CoffeeMain.client.player.getPitch();
        initialYaw = CoffeeMain.client.player.getYaw();
    }

    @Override
    public void onFastTick() {
        InventoryWalk iw = ModuleRegistry.getByClass(InventoryWalk.class);
        if (iw.isEnabled() && iw.mouseInInventory) {
            Rotations.lookAtPositionSmooth(MathHelper.clamp(initialPitch + pitchOffset, -90.0F, 90.0F), initialYaw + yawOffset, iw.getMSpeed());
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        float centerX = width / 2f;
        float centerY = height / 2f;
        yawOffset = (float) (mouseX - centerX) / 5f;
        pitchOffset = (float) (mouseY - centerY) / 5f;
        super.mouseMoved(mouseX, mouseY);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void coffee_preTick(CallbackInfo ci) {
        if (!ModuleRegistry.getByClass(InventoryWalk.class).isEnabled()) {
            return;
        }
        GameOptions go = CoffeeMain.client.options;
        setState(go.forwardKey);
        setState(go.rightKey);
        setState(go.backKey);
        setState(go.leftKey);

        setState(go.jumpKey);
        setState(go.sprintKey);
        setState(go.sneakKey);
    }

}
