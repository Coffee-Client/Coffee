/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.events.BlockEntityRenderEvent;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChestHighlighter extends Module {
    final List<BlockPos> positions = new CopyOnWriteArrayList<>();

    public ChestHighlighter() {
        super("ChestHighlighter", "Shows all chests in the area", ModuleType.RENDER);
        //        Events.registerEventHandlerClass(this);
    }

    void addIfNotExisting(BlockPos p) {
        if (positions.stream().noneMatch(blockPos -> blockPos.equals(p))) {
            positions.add(p);
        }
    }

    @EventListener(value = EventType.BLOCK_ENTITY_RENDER)
    void r(BlockEntityRenderEvent be) {
        if (!this.isEnabled()) {
            return;
        }
        if (be.getBlockEntity() instanceof ChestBlockEntity) {
            addIfNotExisting(be.getBlockEntity().getPos());
        }
    }

    @Override
    public void tick() {
        positions.removeIf(blockPos -> !(client.world.getBlockState(blockPos).getBlock() instanceof ChestBlock));
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
        for (BlockPos position : positions) {
            Renderer.R3D.renderFadingBlock(Utils.getCurrentRGB(), Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 100).darker(), Vec3d.of(position), new Vec3d(1, 1, 1), 500);
        }
    }

    @Override
    public void onHudRender() {

    }
}
