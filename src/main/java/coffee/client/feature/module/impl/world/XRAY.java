/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.feature.module.impl.world;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.events.BlockRenderEvent;
import coffee.client.helper.event.events.ChunkRenderQueryEvent;
import com.google.common.collect.Lists;
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

    @EventListener(type = EventType.BLOCK_RENDER)
    void blockRender(BlockRenderEvent bre) {
        if (!blockApplicable(bre.getBlockState().getBlock())) {
            bre.setCancelled(true);
        }
    }

    @EventListener(type = EventType.SHOULD_RENDER_CHUNK)
    void shouldRenderChunk(ChunkRenderQueryEvent event) {
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
