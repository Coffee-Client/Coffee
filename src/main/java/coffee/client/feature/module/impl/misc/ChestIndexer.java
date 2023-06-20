/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.misc;

import coffee.client.CoffeeMain;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.event.impl.PacketEvent;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Timer;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.x150.jmessenger.MessageSubscription;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChestIndexer extends Module {
    static final Color OUTLINE_COLOR = new Color(14, 166, 85, 255);
    static final Color FILL_COLOR = new Color(14, 166, 85, 100);
    static final Vec3d DIMENSIONS = new Vec3d(1, 1, 1);
    static Block[] ALLOW_LIST = { Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX,
        Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX,
        Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX,
        Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BARREL };
    public Map<BlockPos, Int2ObjectMap<ItemStack>> stacks = new HashMap<>();
    public Map<BlockPos, List<BlockPos>> childrenMap = new HashMap<>();
    public List<Entry> lastVisResults = new CopyOnWriteArrayList<>();
    Timer updateTimer = new Timer();
    BlockPos currentPosClicked;
    int currentSid = -1;

    public ChestIndexer() {
        super("ChestIndexer", "Indexes all chests you open, allows usage of .search", ModuleType.MISC);
    }

    @MessageSubscription
    void recv(PacketEvent.Received pe) {
        Packet<?> packet = pe.getPacket();
        if (packet instanceof OpenScreenS2CPacket os2) {
            if (this.currentPosClicked == null) {
                return;
            }
            this.currentSid = os2.getSyncId();
            this.stacks.putIfAbsent(this.currentPosClicked, new Int2ObjectArrayMap<>());
            this.stacks.get(this.currentPosClicked).clear();
        } else if (packet instanceof ScreenHandlerSlotUpdateS2CPacket e && e.getSyncId() == this.currentSid) {
            if (this.currentPosClicked == null) {
                return;
            }
            this.stacks.putIfAbsent(this.currentPosClicked, new Int2ObjectArrayMap<>());
            Int2ObjectMap<ItemStack> itemStackInt2ObjectMap = this.stacks.get(this.currentPosClicked);
            if (e.getItemStack().isEmpty()) {
                itemStackInt2ObjectMap.remove(e.getSlot());
            } else {
                itemStackInt2ObjectMap.put(e.getSlot(), e.getItemStack().copy());
            }
        } else if (packet instanceof InventoryS2CPacket e && e.getSyncId() == this.currentSid) {
            if (this.currentPosClicked == null) {
                return;
            }
            List<ItemStack> contents = e.getContents();
            this.stacks.putIfAbsent(this.currentPosClicked, new Int2ObjectArrayMap<>());
            Int2ObjectMap<ItemStack> itemStackInt2ObjectMap = this.stacks.get(this.currentPosClicked);
            itemStackInt2ObjectMap.clear();
            int contentLength = contents.size() - 9 * 4; // this one includes the actual inventory, so we remove it from the index
            for (int i = 0; i < contentLength; i++) {
                ItemStack copy = contents.get(i).copy();
                if (copy.isEmpty()) {
                    continue;
                }
                itemStackInt2ObjectMap.put(i, copy);
            }
        } else if (packet instanceof CloseScreenS2CPacket cs2 && cs2.getSyncId() == this.currentSid) {
            this.currentPosClicked = null;
            this.currentSid = -1;
        }
    }

    void cleanupNeighbours() {
        for (BlockPos blockPos : new ArrayList<>(childrenMap.keySet())) {
            List<BlockPos> recordedNeighbours = childrenMap.get(blockPos);
            recordedNeighbours.removeIf(blockPos2 -> !isValidChestNeighbor(blockPos, blockPos2));
            if (recordedNeighbours.isEmpty()) {
                childrenMap.remove(blockPos);
            }
        }
    }

    boolean isValidChestNeighbor(BlockPos owner, BlockPos bp) {
        BlockState bs = client.world.getBlockState(bp);
        BlockState bs1 = client.world.getBlockState(owner);
        return bs1.getBlock() == bs.getBlock();
    }

    @MessageSubscription
    void send(PacketEvent.Sent pe) {
        Packet<?> packet = pe.getPacket();
        if (packet instanceof PlayerInteractBlockC2SPacket p) {
            cleanupNeighbours();
            BlockPos lastPos = p.getBlockHitResult().getBlockPos();
            if (childrenMap.values().stream().anyMatch(blockPos -> blockPos.contains(lastPos))) {
                return; // already part of another chest
            }
            BlockState bs = client.world.getBlockState(lastPos);
            Block block = bs.getBlock();
            List<Block> blocks = Arrays.asList(ALLOW_LIST);
            if (blocks.contains(block)) {
                currentPosClicked = lastPos;
                if (bs.contains(ChestBlock.CHEST_TYPE)) {
                    ChestType chestType = bs.get(ChestBlock.CHEST_TYPE);
                    if (chestType == ChestType.SINGLE) {
                        return; // single chest anyway, doesn't matter
                    }
                    Direction direction = bs.get(ChestBlock.FACING);
                    float currentRot = direction.asRotation();
                    float neighbourOffset = switch (chestType) {
                        case RIGHT -> currentRot - 90; // we are the right chest, the left one is 90 degrees to the left
                        case LEFT -> currentRot + 90; // we are the left chest, the right one is 90 degrees to the right
                        default -> throw new IllegalStateException("This should never happen");
                    };
                    double v1 = Math.toRadians(neighbourOffset);
                    int x = (int) -Math.round(Math.sin(v1));
                    int z = (int) Math.round(Math.cos(v1));
                    BlockPos secondPos = lastPos.add(x, 0, z);
                    BlockState bs1 = client.world.getBlockState(secondPos);
                    if (bs1.getBlock() == block && bs1.get(ChestBlock.CHEST_TYPE) == chestType.getOpposite() && bs1.get(ChestBlock.FACING) == direction) {
                        stacks.remove(secondPos); // if the neighbour was already indexed, remove him and use this pos instead
                        childrenMap.computeIfAbsent(lastPos, blockPos -> new ArrayList<>()).add(secondPos);
                    } else {
                        CoffeeMain.LOGGER.warn(
                            "Lol pos {} has an invalid chest neighbor {} of type {}",
                            lastPos.toShortString(),
                            secondPos.toShortString(),
                            bs1.getBlock().getName().getString()
                        );
                    }
                }
            }
        } else if (packet instanceof CloseHandledScreenC2SPacket e && e.getSyncId() == this.currentSid) {
            this.currentPosClicked = null;
            this.currentSid = -1;
        } else if (packet instanceof ClickSlotC2SPacket cs && cs.getSyncId() == this.currentSid) {
            if (this.currentPosClicked == null) {
                return;
            }
            this.stacks.putIfAbsent(this.currentPosClicked, new Int2ObjectArrayMap<>());
            Int2ObjectMap<ItemStack> itemStackInt2ObjectMap = this.stacks.get(this.currentPosClicked);
            DefaultedList<Slot> slots = client.player.currentScreenHandler.slots;
            int contentLength = slots.size() - 9 * 4; // this one includes the actual inventory, so we remove it from the index
            for (int i = 0; i < contentLength; i++) {
                Slot slot = slots.get(i);
                ItemStack stack = slot.inventory.getStack(slot.id);
                if (stack.isEmpty()) {
                    itemStackInt2ObjectMap.remove(slot.id);
                } else {
                    itemStackInt2ObjectMap.put(slot.id, stack);
                }
            }
        }
    }

    @Override
    public void tick() {
        if (updateTimer.hasExpired(5_000)) {
            for (BlockPos blockPos : new ArrayList<>(stacks.keySet())) {
                if (!blockPos.isWithinDistance(client.player.getBlockPos(), 64)) {
                    continue; // don't update this one we have no fucking idea what's in it
                }
                BlockState bs = client.world.getBlockState(blockPos);
                if (!Arrays.asList(ALLOW_LIST).contains(bs.getBlock())) {
                    stacks.remove(blockPos);
                }
            }
            cleanupNeighbours();
            updateTimer.reset();
        }
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {
        stacks.clear();
    }

    @Override
    public String getContext() {
        return stacks.size() + " indexed";
    }

    public void showResult(BlockPos bp) {
        List<BlockPos> shitters = childrenMap.get(bp);
        if (shitters != null) {
            for (BlockPos shitter : shitters) {
                lastVisResults.add(new Entry(System.currentTimeMillis(), shitter));
            }
        }
        lastVisResults.add(new Entry(System.currentTimeMillis(), bp));
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        lastVisResults.removeIf(entry -> System.currentTimeMillis() - entry.e > 10_000); // 10 sec timeout
        for (Entry lastVisResult : lastVisResults) {
            Renderer.R3D.renderEdged(matrices, FILL_COLOR, OUTLINE_COLOR, Vec3d.of(lastVisResult.bp), DIMENSIONS);
        }
    }

    @Override
    public void onHudRender() {

    }

    record Entry(long e, BlockPos bp) {

    }
}
