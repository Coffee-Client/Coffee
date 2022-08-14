/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.world;

import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleRegistry;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Rotations;
import coffee.client.helper.util.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Flattener extends Module {
    static final Color breakCol = new Color(31, 232, 148, 70);
    final List<RenderEntry> renders = new ArrayList<>();
    final double range = 8;
    final BooleanSetting makeSame = this.config.create(
            new BooleanSetting.Builder(false).name("Make same").description("Makes the floor the same material you're holding").get());
    final BooleanSetting asyncPlaceBreak = this.config.create(new BooleanSetting.Builder(true).name("Async place / break")
            .description("Does block breaking and placing at the same time")
            .get());
    final BooleanSetting breakSides = this.config.create(
            new BooleanSetting.Builder(true).name("Break sides").description("Clears the area 3 blocks up so you can walk into it").get());
    final DoubleSetting amountPerTick = this.config.create(new DoubleSetting.Builder(3).name("Amount per tick")
            .description("How many actions to do per tick")
            .min(1)
            .max(20)
            .precision(0)
            .get());
    Vec3d origin = null;
    int prevSlot = -1;
    boolean toBreakEmptyBefore = false;

    public Flattener() {
        super("Flattener", "Makes everything around you flat, good for making a floor or base", ModuleType.WORLD);
    }

    @Override
    public void tick() {
        Vec3d eyep = Objects.requireNonNull(client.player).getEyePos();
        double rangeMid = range / 2d;
        List<BlockPos> toPlace = new ArrayList<>();
        List<BlockPos> toBreak = new ArrayList<>();
        Block inHand = null;
        if (client.player.getInventory().getStack(prevSlot).getItem() instanceof BlockItem e) {
            inHand = e.getBlock();
        }
        for (double x = -rangeMid; x < rangeMid + 1; x++) {
            for (double z = -rangeMid; z < rangeMid + 1; z++) {
                Vec3d offset = eyep.add(x, 0, z);
                Vec3d actual = new Vec3d(offset.x + .5, origin.y - .5, offset.z + .5);
                if (actual.distanceTo(eyep) > Objects.requireNonNull(client.interactionManager).getReachDistance()) {
                    continue;
                }
                BlockPos c = new BlockPos(actual);
                BlockState state = Objects.requireNonNull(client.world).getBlockState(c);
                if (state.getMaterial().isReplaceable()) {
                    toPlace.add(c);
                }
                if (makeSame.getValue() && inHand != null && !state.isAir() && state.getBlock() != inHand && state.getBlock()
                        .getHardness() > 0) {
                    toBreak.add(c);
                }
                if (breakSides.getValue()) {
                    for (int y = 1; y < 4; y++) {
                        BlockState real = client.world.getBlockState(c.add(0, y, 0));
                        if (!real.isAir() && real.getBlock()
                                .getHardness() > 0 && real.getBlock() != Blocks.WATER && real.getBlock() != Blocks.LAVA) {
                            toBreak.add(c.add(0, y, 0));
                        }
                    }
                }

            }
        }
        toPlace.sort(Comparator.comparingDouble(value -> Vec3d.of(value).add(.5, .5, .5).distanceTo(eyep)));
        toBreak.sort(Comparator.comparingDouble(value -> Vec3d.of(value).add(.5, .5, .5).distanceTo(eyep)));
        renders.clear();
        if (!toBreak.isEmpty() && toBreakEmptyBefore) {
            prevSlot = client.player.getInventory().selectedSlot;
            toBreakEmptyBefore = false;
        }
        int done = 0;
        for (BlockPos blockPos : toBreak) {
            BlockState bs = Objects.requireNonNull(client.world).getBlockState(blockPos);
            if (ModuleRegistry.getByClass(AutoTool.class).isEnabled()) {
                AutoTool.pick(bs);
            }
            Rotations.lookAtV3(Vec3d.of(blockPos).add(.5, .5, .5));
            Objects.requireNonNull(client.interactionManager).updateBlockBreakingProgress(blockPos, Direction.DOWN);
            renders.add(new RenderEntry(blockPos, new Vec3d(1, 1, 1), breakCol));
            done++;
            if (done > amountPerTick.getValue()) {
                if (!asyncPlaceBreak.getValue()) {
                    return;
                } else {
                    break;
                }
            }
        }
        done = 0;
        if (!toBreakEmptyBefore) {
            toBreakEmptyBefore = true;
            client.player.getInventory().selectedSlot = prevSlot;
        }
        for (BlockPos blockPos : toPlace) {
            if (client.player.getInventory().getMainHandStack().getItem() instanceof BlockItem) {
                renders.add(new RenderEntry(blockPos.up(), new Vec3d(1, -0.01, 1), Utils.getCurrentRGB()));
                Vec3d actual = Vec3d.of(blockPos).add(.5, .5, .5);
                Rotations.lookAtV3(actual);
                Objects.requireNonNull(client.interactionManager)
                        .interactBlock(client.player, Hand.MAIN_HAND, new BlockHitResult(actual, Direction.DOWN, blockPos, false));
            }
            done++;
            if (done > amountPerTick.getValue()) {
                break;
            }
        }
    }

    @Override
    public void enable() {
        origin = Objects.requireNonNull(client.player).getPos();
        prevSlot = client.player.getInventory().selectedSlot;
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
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        for (RenderEntry render : renders) {
            Renderer.R3D.renderFilled(Vec3d.of(render.pos()), render.dimensions(), render.color(), matrices);
        }
    }

    @Override
    public void onHudRender() {

    }

    record RenderEntry(BlockPos pos, Vec3d dimensions, Color color) {

    }
}
