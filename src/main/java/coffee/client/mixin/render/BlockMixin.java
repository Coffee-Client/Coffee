/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.mixin.render;

import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.impl.movement.BoingBoing;
import coffee.client.feature.module.impl.movement.Slippy;
import coffee.client.feature.module.impl.world.XRAY;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Block.class)
public abstract class BlockMixin extends AbstractBlock {

    public BlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private static void coffee_overwriteDrawingSide(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        if (Objects.requireNonNull(ModuleRegistry.getByClass(XRAY.class)).isEnabled()) {
            cir.setReturnValue(XRAY.blocks.contains(state.getBlock()));
        }
    }

    @Inject(method = "isTranslucent", at = @At("HEAD"), cancellable = true)
    public void coffee_setTranslucent(BlockState state, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (Objects.requireNonNull(ModuleRegistry.getByClass(XRAY.class)).isEnabled()) {
            cir.setReturnValue(!XRAY.blocks.contains(state.getBlock()));
        }
    }

    private BoingBoing getBoing() {
        return ModuleRegistry.getByClass(BoingBoing.class);
    }

    private Slippy getSlippy() {
        return ModuleRegistry.getByClass(Slippy.class);
    }

    private void bounce(Entity entity) {
        Vec3d vec3d = entity.getVelocity();
        if (vec3d.y < 0 || getBoing().bounceBack) {
            double d = getBoing().bounceMul;
            entity.setVelocity(vec3d.x, -vec3d.y * d, vec3d.z);
        } else {
            entity.setVelocity(vec3d.x, 0, vec3d.z);
        }
    }

    @Inject(method = "onEntityLand", at = @At("HEAD"), cancellable = true)
    void coffee_onEntityLandBounce(BlockView world, Entity entity, CallbackInfo ci) {
        if (getBoing().isEnabled() && entity.world.isClient()) {
            bounce(entity);
            ci.cancel();
        }
    }

    @Inject(method = "getSlipperiness", at = @At("HEAD"), cancellable = true)
    void coffee_replaceSlipperiness(CallbackInfoReturnable<Float> cir) {
        if (getSlippy().isEnabled()) {
            cir.setReturnValue((float) getSlippy().slipperiness);
        }
    }
}
