/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.misc;

import cf.coffee.client.feature.config.annotation.Setting;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import cf.coffee.client.helper.event.EventListener;
import cf.coffee.client.helper.event.EventType;
import cf.coffee.client.helper.event.events.BlockRenderEvent;
import cf.coffee.client.helper.render.Renderer;
import cf.coffee.client.helper.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.CopyOnWriteArrayList;

public class Test extends Module {
    static final Block searchTerm = Blocks.NETHER_PORTAL;
    final CopyOnWriteArrayList<BlockPos> discovered = new CopyOnWriteArrayList<>();

    @Setting(name="Test1",description = "among us",min = 0,max = 69,precision = 1)
    double testSetting = 6d;

    public Test() {
        super("Test", "Testing stuff with the client, can be ignored", ModuleType.MISC);
        //        Events.registerEventHandlerClass(this);
    }

    @EventListener(type = EventType.BLOCK_RENDER)
    @SuppressWarnings("unused")
    void onBlockRender(BlockRenderEvent event) {
        if (!this.isEnabled()) return;
        BlockPos b = new BlockPos(event.getPosition());
        boolean listContains = discovered.stream().anyMatch(blockPos -> blockPos.equals(b));
        if (event.getBlockState().getBlock() == searchTerm) {
            if (!listContains) {
                discovered.add(b);
            }
        } else if (listContains) {
            discovered.removeIf(blockPos -> blockPos.equals(b));
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
        for (BlockPos bruh : discovered) {
            Renderer.R3D.renderEdged(matrices, Vec3d.of(bruh), new Vec3d(1, 1, 1), Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 100).darker(), Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 255));
        }
    }

    @Override
    public void onHudRender() {

    }

    @Override
    public void tick() {
        discovered.removeIf(blockPos -> client.world.getBlockState(blockPos).getBlock() != searchTerm);
        //        client.interactionManager.clickSlot(0, 0, 0, SlotActionType.QUICK_MOVE, client.player);
    }
}
