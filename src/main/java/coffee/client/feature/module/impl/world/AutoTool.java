/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.world;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.mixin.IClientPlayerInteractionManagerMixin;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class AutoTool extends Module {

    public AutoTool() {
        super("AutoTool", "Automatically selects the best tool for the job", ModuleType.WORLD);
    }

    public static void pick(BlockState state) {
        float best = 1f;
        int index = -1;
        int optAirIndex = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = Objects.requireNonNull(client.player).getInventory().getStack(i);
            if (stack.getItem() == Items.AIR) {
                optAirIndex = i;
            }
            float s = stack.getMiningSpeedMultiplier(state);
            if (s > best) {
                index = i;
            }
        }
        if (index != -1) {
            client.player.getInventory().selectedSlot = index;
        } else {
            if (optAirIndex != -1) {
                client.player.getInventory().selectedSlot = optAirIndex; // to prevent tools from getting damaged by accident, switch to air if we didn't find anything
            }
        }
    }

    @Override
    public void tick() {
        if (Objects.requireNonNull(client.interactionManager).isBreakingBlock() && !Objects.requireNonNull(
                ModuleRegistry.getByClass(SurvivalNuker.class)).isEnabled()) {
            BlockPos breaking = ((IClientPlayerInteractionManagerMixin) client.interactionManager).getCurrentBreakingPos();
            BlockState bs = Objects.requireNonNull(client.world).getBlockState(breaking);
            pick(bs);
        }
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }
}
