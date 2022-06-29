/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.world;

import coffee.client.feature.config.EnumSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Nuker extends Module {
    static final List<Block> instantBreaks = Util.make(() -> {
        ArrayList<Block> piss = new ArrayList<>();
        for (Block block : Registry.BLOCK) {
            if (block.getHardness() == 0f && block.getBlastResistance() == 0f) {
                piss.add(block);
            }
        }
        return piss;
    });
    final EnumSetting<Mode> modeSetting = this.config.create(new EnumSetting.Builder<>(Mode.Interaction).name("Mode").description("How to break block").get());

    public Nuker() {
        super("Nuker", "The nuke", ModuleType.WORLD);
    }

    void iterateOverRange(double range, Consumer<Vec3d> positionsOffset) {
        double halfRange = Math.ceil(range);
        for (double x = -halfRange; x <= halfRange + 1; x++) {
            for (double y = -halfRange; y <= halfRange + 1; y++) {
                for (double z = -halfRange; z <= halfRange + 1; z++) {
                    Vec3d posOff = new Vec3d(x - .5, y, z - .5);
                    Vec3d actual = client.player.getPos().add(posOff);
                    if (actual.distanceTo(client.player.getEyePos()) > range) {
                        continue;
                    }
                    positionsOffset.accept(actual);
                }
            }
        }
    }

    @Override
    public void tick() {
        switch (modeSetting.getValue()) {
            case Packet -> iterateOverRange(client.interactionManager.getReachDistance(), vec3d -> {
                BlockPos bp = new BlockPos(vec3d);
                BlockState bs = client.world.getBlockState(bp);
                if (bs.isAir()) {
                    return;
                }
                Block b = bs.getBlock();
                if (b == Blocks.WATER || b == Blocks.LAVA) {
                    return;
                }
                //                    BlockHitResult bhr = new BlockHitResult(vec3d, Direction.DOWN,bp,false);
                client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, bp, Direction.DOWN));
                client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, bp, Direction.DOWN));
                Renderer.R3D.renderFadingBlock(
                        Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 255),
                        Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 100).darker(),
                        Vec3d.of(bp),
                        new Vec3d(1, 1, 1),
                        1000
                );
                //                    client.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,bhr));
            });
            case Griefing -> iterateOverRange(client.interactionManager.getReachDistance(), vec3d -> {
                BlockPos bp = new BlockPos(vec3d);
                BlockState bs = client.world.getBlockState(bp);
                if (bs.isAir()) {
                    return;
                }
                if (instantBreaks.contains(bs.getBlock())) {
                    client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, bp, Direction.DOWN));
                    client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, bp, Direction.DOWN));
                    Renderer.R3D.renderFadingBlock(
                            Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 255),
                            Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 100).darker(),
                            Vec3d.of(bp),
                            new Vec3d(1, 1, 1),
                            1000
                    );
                }
            });
            case Interaction -> iterateOverRange(client.interactionManager.getReachDistance(), vec3d -> {
                BlockPos bp = new BlockPos(vec3d);
                BlockState bs = client.world.getBlockState(bp);
                if (bs.isAir()) {
                    return;
                }
                Block b = bs.getBlock();
                if (b == Blocks.WATER || b == Blocks.LAVA) {
                    return;
                }
                client.interactionManager.attackBlock(bp, Direction.DOWN);
                Renderer.R3D.renderFadingBlock(
                        Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 255),
                        Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 100).darker(),
                        Vec3d.of(bp),
                        new Vec3d(1, 1, 1),
                        1000
                );
            });
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

    public enum Mode {
        Packet, Interaction, Griefing
    }
}
