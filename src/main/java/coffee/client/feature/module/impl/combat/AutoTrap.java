/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.combat;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.render.Renderer;
import net.minecraft.block.Block;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.Arrays;
import java.util.Objects;

public class AutoTrap extends Module {
    static final double[][] buildOffsetsSmall = new double[][] { new double[] { 0, 2, 0 }, new double[] { 1, 1, 0 }, new double[] { 0, 1, 1 }, new double[] { -1, 1, 0 }, new double[] { 0, 1, -1 }, new double[] { 0, -1, 0 } };
    static final double[][] buildOffsetsBig = new double[][] {
            // begin bottom
            new double[] { -.5, -1, -.5 }, new double[] { -.5, -1, .5 }, new double[] { .5, -1, .5 }, new double[] { .5, -1, -.5 },

            // begin sides
            // -x
            new double[] { -1.5, 1, -.5 }, new double[] { -1.5, 1, .5 },

            // +x
            new double[] { 1.5, 1, -.5 }, new double[] { 1.5, 1, .5 },

            // -z
            new double[] { -.5, 1, -1.5 }, new double[] { .5, 1, -1.5 },

            // +z
            new double[] { -.5, 1, 1.5 }, new double[] { .5, 1, 1.5 },

            // begin top
            new double[] { -.5, 2, -.5 }, new double[] { -.5, 2, .5 }, new double[] { .5, 2, .5 }, new double[] { .5, 2, -.5 },

    };


    public AutoTrap() {
        super("AutoTrap", "Automatically traps everyone around you in a cage", ModuleType.COMBAT);
    }

    boolean isTrappedAlready(Entity entity) {
        Vec3d entityPos = entity.getPos();
        BlockPos bp = new BlockPos(entityPos);

        boolean smallMatches = Arrays.stream(buildOffsetsSmall).allMatch(ints -> {
            BlockPos a = bp.add(ints[0], ints[1], ints[2]);
            return Objects.requireNonNull(CoffeeMain.client.world).getBlockState(a).getMaterial().blocksMovement();
        });
        if (smallMatches) {
            return true;
        }
        double[][] possibleOffsetsHome = new double[][] { new double[] { 1, 0, 1 }, new double[] { 1, 0, 0 }, new double[] { 1, 0, -1 }, new double[] { 0, 0, -1 }, new double[] { -1, 0, -1 }, new double[] { -1, 0, 0 }, new double[] { -1, 0, 1 }, new double[] { 0, 0, 1 }, new double[] { 0, 0, 0 } };
        for (double[] ints : possibleOffsetsHome) {
            Vec3d potentialHome = entityPos.add(ints[0], ints[1], ints[2]);
            boolean matches = Arrays.stream(buildOffsetsBig).allMatch(ints1 -> {
                BlockPos a = new BlockPos(potentialHome.add(ints1[0], ints1[1], ints1[2]));
                return CoffeeMain.client.world.getBlockState(a).getMaterial().blocksMovement();
            });
            if (matches) {
                return true;
            }
        }
        return false;
    }

    boolean inHitRange(Entity attacker, Vec3d pos) {
        return attacker.getCameraPosVec(1f)
                .distanceTo(pos) <= Objects.requireNonNull(CoffeeMain.client.interactionManager)
                .getReachDistance() + .5;
    }

    @Override
    public void onFastTick() {
        for (Entity player : Objects.requireNonNull(CoffeeMain.client.world).getPlayers()) {
            if (player.equals(CoffeeMain.client.player)) {
                continue;
            }
            if (isTrappedAlready(player)) {
                continue;
            }
            Vec3d pos = player.getPos();
            BlockPos bp = new BlockPos(pos);
            double eWidth = player.getWidth();
            BlockPos corner = new BlockPos(pos.subtract(eWidth / 2, 0, eWidth / 2));
            BlockPos otherCorner = new BlockPos(pos.add(eWidth / 2, 0, eWidth / 2));
            double[][] planToUse;
            if (corner.getX() == bp.getX() && corner.getZ() == bp.getZ() && otherCorner.getX() == bp.getX() && otherCorner.getZ() == bp.getZ()) {
                planToUse = buildOffsetsSmall;
            } else {
                planToUse = buildOffsetsBig;
            }

            double[][] filteredPlan = Arrays.stream(planToUse).filter(ints -> {
                Vec3d v = player.getPos().add(new Vec3d(ints[0], ints[1], ints[2]));
                return inHitRange(CoffeeMain.client.player, v.add(.5, .5, .5));
            }).toList().toArray(double[][]::new);

            int slot = -1;
            for (int i = 0; i < 9; i++) {
                ItemStack real = CoffeeMain.client.player.getInventory().getStack(i);
                if (real.getItem() instanceof BlockItem bi && Block.isShapeFullCube(bi.getBlock()
                        .getDefaultState()
                        .getOutlineShape(CoffeeMain.client.world, new BlockPos(0, 0, 0), ShapeContext.absent()))) {
                    slot = i;
                }
            }
            if (slot == -1) {
                break; // we got nothing to work with, cancel this time
            }

            int finalSlot = slot;
            // make sure we're in sync
            CoffeeMain.client.execute(() -> {
                int selSlot = CoffeeMain.client.player.getInventory().selectedSlot;
                CoffeeMain.client.player.getInventory().selectedSlot = finalSlot;
                for (double[] ints : filteredPlan) {
                    BlockPos current = new BlockPos(pos.add(ints[0], ints[1], ints[2]));
                    if (!CoffeeMain.client.world.getBlockState(current).isAir()) {
                        continue;
                    }
                    BlockHitResult bhr = new BlockHitResult(Vec3d.of(current), Direction.DOWN, current, false);

                    Objects.requireNonNull(CoffeeMain.client.interactionManager)
                            .interactBlock(CoffeeMain.client.player, CoffeeMain.client.world, Hand.MAIN_HAND, bhr);
                }
                CoffeeMain.client.player.getInventory().selectedSlot = selSlot;
            });
        }
    }

    @Override
    public void tick() {

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
        if (isDebuggerEnabled()) {
            for (Entity player : Objects.requireNonNull(CoffeeMain.client.world).getPlayers()) {
                if (player.equals(CoffeeMain.client.player)) {
                    continue;
                }
                if (isTrappedAlready(player)) {
                    continue;
                }
                Vec3d pos = player.getPos();
                BlockPos bp = new BlockPos(pos);
                double eWidth = player.getWidth();
                BlockPos corner = new BlockPos(pos.subtract(eWidth / 2, 0, eWidth / 2));
                BlockPos otherCorner = new BlockPos(pos.add(eWidth / 2, 0, eWidth / 2));
                double[][] planToUse;
                if (corner.getX() == bp.getX() && corner.getZ() == bp.getZ() && otherCorner.getX() == bp.getX() && otherCorner.getZ() == bp.getZ()) {
                    planToUse = buildOffsetsSmall;
                } else {
                    planToUse = buildOffsetsBig;
                }

                for (double[] ints : planToUse) {
                    BlockPos current = new BlockPos(pos.add(ints[0], ints[1], ints[2]));
                    Vec3d v3 = Vec3d.of(current);
                    if (!inHitRange(CoffeeMain.client.player, v3.add(.5, .5, .5))) {
                        Renderer.R3D.renderOutline(v3, new Vec3d(1, 1, 1), Color.RED, matrices);
                        continue;
                    }
                    if (!CoffeeMain.client.world.getBlockState(current).isAir()) {
                        Renderer.R3D.renderOutline(v3, new Vec3d(1, 1, 1), Color.BLUE, matrices);
                        continue;
                    }
                    Renderer.R3D.renderOutline(v3, new Vec3d(1, 1, 1), Color.GREEN, matrices);
                }
            }
        }
    }

    @Override
    public void onHudRender() {

    }
}
