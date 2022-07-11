/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.world;

import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Rotations;
import coffee.client.helper.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SurvivalNuker extends Module {

    final List<BlockPos> renders = new ArrayList<>();
    final Block[] WOOD = new Block[] { Blocks.ACACIA_LOG, Blocks.BIRCH_LOG, Blocks.DARK_OAK_LOG, Blocks.JUNGLE_LOG, Blocks.OAK_LOG, Blocks.SPRUCE_LOG,
            Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.STRIPPED_JUNGLE_LOG, Blocks.STRIPPED_OAK_LOG,
            Blocks.STRIPPED_SPRUCE_LOG };

    final DoubleSetting range = this.config.create(new DoubleSetting.Builder(4).name("Range")
            .description("How far to break blocks")
            .min(0)
            .max(4)
            .precision(1)
            .get());
    final DoubleSetting blocksPerTick = this.config.create(new DoubleSetting.Builder(1).name("Blocks per tick")
            .description("How many blocks to break per tick")
            .min(1)
            .max(20)
            .precision(0)
            .get());
    final DoubleSetting delay = this.config.create(new DoubleSetting.Builder(0).name("Delay")
            .description("How much to wait between ticks")
            .min(0)
            .max(20)
            .precision(0)
            .get());
    final BooleanSetting ignoreXray = this.config.create(new BooleanSetting.Builder(true).name("Ignore XRAY").description("Ignores XRAY blocks").get());
    final EnumSetting<Mode> mode = this.config.create(new EnumSetting.Builder<>(Mode.Everything).name("Mode").description("What to break").get());
    final BooleanSetting autoTool = this.config.create(new BooleanSetting.Builder(true).name("Auto tool")
            .description("Automatically picks the best tool for the block")
            .get());
    final EnumSetting<SortMode> mv = this.config.create(new EnumSetting.Builder<>(SortMode.OutIn).name("Sorting")
            .description("In which order to break the blocks")
            .get());
    final BooleanSetting ignoreUnbreakable = this.config.create(new BooleanSetting.Builder(true).name("Ignore unbreakable")
            .description("Ignores unbreakable blocks")
            .get());
    int delayPassed = 0;

    public SurvivalNuker() {
        super("SurvivalNuker", "Breaks a lot of blocks around you fast", ModuleType.WORLD);
    }

    boolean isBlockApplicable(Block b) {
        if (mode.getValue() == Mode.Everything) {
            return true;
        } else if (mode.getValue() == Mode.Torches) {
            return b == Blocks.TORCH || b == Blocks.WALL_TORCH || b == Blocks.SOUL_TORCH || b == Blocks.SOUL_WALL_TORCH;
        } else if (mode.getValue() == Mode.Fire) {
            return b == Blocks.FIRE || b == Blocks.SOUL_FIRE;
        } else if (mode.getValue() == Mode.Wood) {
            return Arrays.stream(WOOD).anyMatch(block -> block == b);
        } else if (mode.getValue() == Mode.Grass) {
            return b == Blocks.GRASS || b == Blocks.TALL_GRASS;
        }
        return false;
    }

    @Override
    public void tick() {
        if (client.player == null || client.world == null || client.interactionManager == null || client.getNetworkHandler() == null) {
            return;
        }
        if (delayPassed < delay.getValue()) {
            delayPassed++;
            return;
        }
        delayPassed = 0;
        BlockPos ppos1 = client.player.getBlockPos();
        int blocksBroken = 0;
        renders.clear();
        List<BlockPos> toHit = new ArrayList<>();
        for (double y = range.getValue(); y > -range.getValue() - 1; y--) {
            for (double x = -range.getValue(); x < range.getValue() + 1; x++) {
                for (double z = -range.getValue(); z < range.getValue() + 1; z++) {
                    BlockPos vp = new BlockPos(x, y, z);
                    BlockPos np = ppos1.add(vp);
                    Vec3d vp1 = Vec3d.of(np).add(.5, .5, .5);
                    if (vp1.distanceTo(client.player.getEyePos()) >= client.interactionManager.getReachDistance()) {
                        continue;
                    }
                    toHit.add(np);
                }
            }
        }
        toHit = toHit.stream().sorted(Comparator.comparingDouble(value1 -> {
            Vec3d value = Vec3d.of(value1).add(new Vec3d(.5, .5, .5));
            return switch (mv.getValue()) {
                case OutIn -> value.distanceTo(client.player.getPos()) * -1;
                case InOut -> value.distanceTo(client.player.getPos());
                case Strength -> client.world.getBlockState(value1).getBlock().getHardness();
                default -> 1;
            };
        })).collect(Collectors.toList());
        if (mv.getValue() == SortMode.Random) {
            Collections.shuffle(toHit);
        }
        for (BlockPos np : toHit) {
            if (blocksBroken >= blocksPerTick.getValue()) {
                break;
            }
            BlockState bs = client.world.getBlockState(np);
            boolean b = !ignoreXray.getValue() || !XRAY.blocks.contains(bs.getBlock());
            if (!bs.isAir() && bs.getBlock() != Blocks.WATER && bs.getBlock() != Blocks.LAVA && !isUnbreakable(bs.getBlock()) && b && client.world.getWorldBorder()
                    .contains(np) && isBlockApplicable(bs.getBlock())) {
                renders.add(np);
                if (autoTool.getValue()) {
                    AutoTool.pick(bs);
                }
                client.player.swingHand(Hand.MAIN_HAND);
                if (!client.player.getAbilities().creativeMode) {
                    client.interactionManager.updateBlockBreakingProgress(np, Direction.DOWN);
                } else {
                    client.interactionManager.attackBlock(np, Direction.DOWN);
                }
                Rotations.lookAtV3(new Vec3d(np.getX() + .5, np.getY() + .5, np.getZ() + .5));
                blocksBroken++;
            }
        }
    }

    boolean isUnbreakable(Block b) {
        if (!ignoreUnbreakable.getValue()) {
            return false;
        }
        return b.getHardness() == -1;
    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        for (BlockPos render : renders) {
            Vec3d vp = new Vec3d(render.getX(), render.getY(), render.getZ());
            //            Renderer.R3D.renderFilled(vp, new Vec3d(1, 1, 1), Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 50), matrices);
            Renderer.R3D.renderFadingBlock(
                    Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 255),
                    Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 100).darker(),
                    vp,
                    new Vec3d(1, 1, 1),
                    1000
            );
        }
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {
    }

    @Override
    public void onHudRender() {

    }

    public enum Mode {
        Everything, Torches, Fire, Wood, Grass
    }

    public enum SortMode {
        OutIn, InOut, Strength, Random
    }
}
