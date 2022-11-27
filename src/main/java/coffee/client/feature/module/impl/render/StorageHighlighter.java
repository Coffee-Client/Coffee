/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.render;

import coffee.client.CoffeeMain;
import coffee.client.feature.config.BooleanSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.impl.RenderEvent;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Utils;
import me.x150.jmessenger.MessageSubscription;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StorageHighlighter extends Module {
    final List<BlockPos> positions = new CopyOnWriteArrayList<>();

    final Hashtable<Class, Color> colors = new Hashtable<>();
    private void loadColors() {
        colors.put(ChestBlock.class, new Color(0x003EAD));
        colors.put(ChestBlockEntity.class, new Color(0x003EAD));

        colors.put(TrappedChestBlock.class, new Color(0x003EAD));
        colors.put(TrappedChestBlockEntity.class, new Color(0x003EAD));

        colors.put(EnderChestBlock.class, new Color(0x9600AD));
        colors.put(EnderChestBlockEntity.class, new Color(0x9600AD));

        colors.put(BarrelBlock.class, new Color(0x00ADAD));
        colors.put(BarrelBlockEntity.class, new Color(0x00ADAD));

        colors.put(ShulkerBoxBlock.class, new Color(0xAD0000));
        colors.put(ShulkerBoxBlockEntity.class, new Color(0xAD0000));

        colors.put(HopperBlock.class, new Color(0xFF5107));
        colors.put(HopperBlockEntity.class, new Color(0xFF5107));

        colors.put(DropperBlock.class, new Color(0x5D9B0E));
        colors.put(DropperBlockEntity.class, new Color(0x5D9B0E));

        colors.put(DispenserBlock.class, new Color(0x5D9B0E));
        colors.put(DispenserBlockEntity.class, new Color(0x5D9B0E));

        colors.put(BrewingStandBlock.class, new Color(0xADAD00));
        colors.put(BrewingStandBlockEntity.class, new Color(0xADAD00));

        colors.put(FurnaceBlock.class, new Color(0x565656));
        colors.put(FurnaceBlockEntity.class, new Color(0x565656));

        colors.put(BlastFurnaceBlock.class, new Color(0x565656));
        colors.put(BlastFurnaceBlockEntity.class, new Color(0x565656));

        colors.put(SmokerBlock.class, new Color(0x565656));
        colors.put(SmokerBlockEntity.class, new Color(0x565656));
    }

    private Boolean isStorage(Block block) {
        return block instanceof ChestBlock
                || block instanceof TrappedChestBlock
                || block instanceof EnderChestBlock
                || block instanceof BarrelBlock
                || block instanceof ShulkerBoxBlock
                || block instanceof HopperBlock
                || block instanceof DropperBlock
                || block instanceof DispenserBlock
                || block instanceof BrewingStandBlock
                || block instanceof FurnaceBlock
                || block instanceof BlastFurnaceBlock
                || block instanceof SmokerBlock;
    }

    private Boolean isStorage(BlockEntity block) {
        return block instanceof ChestBlockEntity
                || block instanceof TrappedChestBlockEntity
                || block instanceof EnderChestBlockEntity
                || block instanceof BarrelBlockEntity
                || block instanceof ShulkerBoxBlockEntity
                || block instanceof HopperBlockEntity
                || block instanceof DropperBlockEntity
                || block instanceof DispenserBlockEntity
                || block instanceof BrewingStandBlockEntity
                || block instanceof FurnaceBlockEntity
                || block instanceof BlastFurnaceBlockEntity
                || block instanceof SmokerBlockEntity;
    }

    public StorageHighlighter() {
        super("StorageHighlighter", "Shows all the storage blocks in the area", ModuleType.RENDER);

        loadColors();
    }

    void addIfNotExisting(BlockPos p) {
        if (positions.stream().noneMatch(blockPos -> blockPos.equals(p))) {
            positions.add(p);
        }
    }

    @MessageSubscription
    void r(RenderEvent.BlockEntity be) {
        if (!this.isEnabled()) {
            return;
        }

//        if (be.getEntity() instanceof ChestBlockEntity) {
//            addIfNotExisting(be.getEntity().getPos());
//        }
        if (isStorage(be.getEntity())) {
            addIfNotExisting(be.getEntity().getPos());
        }
    }

    @MessageSubscription
    void r(RenderEvent.Block b) {
        if (!this.isEnabled()) {
            return;
        }

        if (isStorage(b.getState().getBlock()) && b.getPos() != null) {
            addIfNotExisting(b.getPos());
        }
    }

    @Override
    public void tick() {
        //positions.removeIf(blockPos -> !(client.world.getBlockState(blockPos).getBlock() instanceof ChestBlock));
        positions.removeIf(blockPos -> !isStorage(client.world.getBlockState(blockPos).getBlock()) && !isStorage(client.world.getBlockEntity(blockPos)));
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
            Block b = client.world.getBlockState(position).getBlock();
            BlockEntity be = client.world.getBlockEntity(position);
            Color c = isStorage(b)
                    ? colors.get(b.getClass())
                    : (be != null && isStorage(be))
                        ? colors.get(be.getClass())
                        : null;
            if (c != null) {
                Renderer.R3D.renderFadingBlock(c,
                        Renderer.Util.modify(c, -1, -1, -1, 100).darker(),
                        Vec3d.of(position),
                        new Vec3d(1, 1, 1),
                        500);
            }
        }
    }

    @Override
    public void onHudRender() {

    }
}
