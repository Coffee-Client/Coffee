/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.feature.module.impl.world;

import coffee.client.feature.config.DoubleSetting;
import coffee.client.feature.module.Module;
import coffee.client.feature.module.ModuleType;
import coffee.client.helper.Rotation;
import coffee.client.helper.event.EventListener;
import coffee.client.helper.event.EventType;
import coffee.client.helper.event.events.SneakQueryEvent;
import coffee.client.helper.render.Renderer;
import coffee.client.helper.util.Rotations;
import coffee.client.helper.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class Scaffold extends Module {
    final DoubleSetting extend = this.config.create(new DoubleSetting.Builder(0).name("Extend")
        .description("How many blocks to extend outwards")
        .min(0)
        .max(5)
        .precision(1)
        .get());

    public Scaffold() {
        super("Scaffold", "Places blocks below you as you walk", ModuleType.WORLD);
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
        //        client.options.sneakKey.setPressed(false);
    }

    @Override
    public void onHudRender() {

    }

    @EventListener(EventType.SNEAK_QUERY)
    void overwriteSneak(SneakQueryEvent sq) {
        sq.setSneaking(false);
    }

    @Override
    public void onFastTick() {
        Vec3d ppos = Objects.requireNonNull(client.player).getPos().add(0, -1, 0);
        BlockPos bp = new BlockPos(ppos);
        int selIndex = client.player.getInventory().selectedSlot;
        if (!(client.player.getInventory().getStack(selIndex).getItem() instanceof BlockItem)) {
            for (int i = 0; i < 9; i++) {
                ItemStack is = client.player.getInventory().getStack(i);
                if (is.getItem() == Items.AIR) {
                    continue;
                }
                if (is.getItem() instanceof BlockItem) {
                    selIndex = i;
                    break;
                }
            }
        }
        if (client.player.getInventory().getStack(selIndex).getItem() != Items.AIR) {
            boolean sneaking = client.options.sneakKey.isPressed();
            if (sneaking) {
                bp = bp.down();
            }
            // fucking multithreading moment
            int finalSelIndex = selIndex;
            BlockPos finalBp = bp;
            client.execute(() -> placeBlockWithSlot(finalSelIndex, finalBp));
            if (extend.getValue() != 0) {
                Vec3d dir1 = client.player.getVelocity().multiply(3);
                Vec3d dir = new Vec3d(MathHelper.clamp(dir1.getX(), -1, 1), 0, MathHelper.clamp(dir1.getZ(), -1, 1));
                Vec3d v = ppos;
                for (double i = 0; i < extend.getValue(); i += 0.5) {
                    v = v.add(dir);
                    if (v.distanceTo(client.player.getPos()) >= Objects.requireNonNull(client.interactionManager).getReachDistance()) {
                        break;
                    }
                    if (sneaking) {
                        v = v.add(0, -1, 0);
                    }
                    BlockPos bp1 = new BlockPos(v);
                    client.execute(() -> placeBlockWithSlot(finalSelIndex, bp1));
                }

            }
        }
    }

    void placeBlockWithSlot(int s, BlockPos bp) {
        BlockState st = Objects.requireNonNull(client.world).getBlockState(bp);
        if (!st.getMaterial().isReplaceable()) {
            return;
        }
        Rotation py = Rotations.getPitchYaw(new Vec3d(bp.getX() + .5, bp.getY() + .5, bp.getZ() + .5));
        Rotations.setClientPitch(py.getPitch());
        Rotations.setClientYaw(py.getYaw());
        int c = Objects.requireNonNull(client.player).getInventory().selectedSlot;
        client.player.getInventory().selectedSlot = s;
        BlockHitResult bhr = new BlockHitResult(new Vec3d(bp.getX(), bp.getY(), bp.getZ()), Direction.DOWN, bp, false);
        Renderer.R3D.renderFadingBlock(Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 255),
            Renderer.Util.modify(Utils.getCurrentRGB(), -1, -1, -1, 100).darker(),
            Vec3d.of(bp),
            new Vec3d(1, 1, 1),
            1000);
        Objects.requireNonNull(client.interactionManager).interactBlock(client.player, Hand.MAIN_HAND, bhr);
        client.player.getInventory().selectedSlot = c;
    }
}
