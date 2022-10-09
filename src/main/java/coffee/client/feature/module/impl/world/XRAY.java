/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.world;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.impl.ChunkRenderQuery;
import coffee.client.helper.event.impl.RenderEvent;
import com.google.common.collect.Lists;
import me.x150.jmessenger.MessageSubscription;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.OreBlock;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class XRAY extends Module {

    public static final List<Block> blocks = Lists.newArrayList();

    public XRAY() {
        super("XRAY", "Allows you to see ores through blocks", ModuleType.WORLD);
        Registry.BLOCK.forEach(block -> {
            if (blockApplicable(block)) {
                blocks.add(block);
            }
        });
    }

    boolean blockApplicable(Block block) {
        boolean c1 = block == Blocks.CHEST || block == Blocks.FURNACE || block == Blocks.END_GATEWAY || block == Blocks.COMMAND_BLOCK || block == Blocks.ANCIENT_DEBRIS;
        boolean c2 = block instanceof OreBlock || block instanceof RedstoneOreBlock;
        return c1 || c2;
    }

    @MessageSubscription
    void blockRender(RenderEvent.Block bre) {
        if (!blockApplicable(bre.getState().getBlock())) {
            bre.setCancelled(true);
        }
    }

    @MessageSubscription
    void shouldRenderChunk(ChunkRenderQuery event) {
        event.setShouldRender(true);
    }

    @Override
    public void tick() {

    }

    @Override
    public void enable() {
        CoffeeMain.client.worldRenderer.reload();
    }

    @Override
    public void disable() {
        CoffeeMain.client.worldRenderer.reload();
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
